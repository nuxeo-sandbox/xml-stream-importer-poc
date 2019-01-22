/*
 * (C) Copyright 2006-2018 Nuxeo (http://nuxeo.com/) and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 * Contributors:
 *     anechaev
 */
package org.nuxeo.dst.importer.work;

import static org.nuxeo.dst.importer.common.Constants.FILE_CONTENT;

import java.io.Externalizable;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.xml.bind.JAXBException;

import org.apache.avro.Schema;
import org.apache.avro.message.RawMessageDecoder;
import org.apache.avro.message.RawMessageEncoder;
import org.apache.avro.reflect.ReflectData;
import org.apache.commons.lang.StringUtils;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.nuxeo.dst.importer.data.Documentable;
import org.nuxeo.dst.importer.service.XMLImporterService;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.Blobs;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.NuxeoException;
import org.nuxeo.ecm.core.work.AbstractWork;
import org.nuxeo.lib.stream.computation.Record;
import org.nuxeo.lib.stream.log.LogAppender;
import org.nuxeo.lib.stream.log.LogManager;
import org.nuxeo.lib.stream.log.LogOffset;
import org.nuxeo.lib.stream.log.LogRecord;
import org.nuxeo.lib.stream.log.LogTailer;
import org.nuxeo.runtime.api.Framework;
import org.nuxeo.runtime.stream.StreamService;

import com.fasterxml.jackson.databind.ObjectMapper;

public class XMLImportWork extends AbstractWork {

    private static final Log log = LogFactory.getLog(XMLImportWork.class);

    public static final String XML_STREAM_IMPORTER_WORK = "XMLImportWork";

    private final String manco;

    private final String filePath;

    public XMLImportWork(String repo, String user, String manco, String filePath) {
        super();

        Objects.requireNonNull(repo);
        Objects.requireNonNull(manco);
        Objects.requireNonNull(filePath);

        this.manco = manco;
        this.filePath = filePath;
        repositoryName = repo;
        originatingUsername = user;
    }

    @Override
    public void work() {
        File file = new File(filePath);
        if (!file.exists()) {
            sendNotification(404, filePath + " does not exist");
            throw new NuxeoException(filePath + " does not exist");
        }

        XMLImporterService importerService = Framework.getService(XMLImporterService.class);
        List<? extends Documentable> docs;
        try {
            docs = importerService.parse(file);
            if (docs.isEmpty()) {
                log.info("nothing to process");
                sendNotification(400, "nothing to process");
                return;
            }
        } catch (JAXBException e) {
            log.error(e);
            sendNotification(400, e.getMessage());
            throw new NuxeoException(e);
        }

        StreamService ss = Framework.getService(StreamService.class);
        LogManager logger = ss.getLogManager("default");

        logger.createIfNotExists(id + "_" + manco, 2);

        LogAppender<Record> appender = logger.getAppender(id + "_" + manco);

        Class<? extends Documentable> aClass = docs.get(0).getClass();
        Schema schema = ReflectData.get().getSchema(aClass);
        RawMessageEncoder<Object> encoder = new RawMessageEncoder<>(ReflectData.get(), schema);


        for (Documentable doc : docs) {
            doc.setDocumentPath(manco);

            try {
                ByteBuffer buf = encoder.encode(doc);
                LogOffset offset = appender.append(doc.getName(), Record.of(doc.getName(), buf.array()));
            } catch (IOException e) {
                log.error(e);
            }
        }

        consume(logger, schema);
    }

    private void consume(LogManager logger, Schema schema) {
        XMLImporterService importerService = Framework.getService(XMLImporterService.class);
        RawMessageDecoder<Object> decoder = new RawMessageDecoder<>(ReflectData.get(), schema);
        LogTailer<Externalizable> tailer = logger.createTailer(id, id + "_" + manco);
        tailer.toLastCommitted();

        Exception finalEx = null;
        try {
            openUserSession();
            LogRecord<Externalizable> record;
            do {
                record = tailer.read(Duration.ofSeconds(5));
                if (record == null) {
                    continue;
                }

                Record message = (Record) record.message();
                Documentable decoded = (Documentable) decoder.decode(message.data);

                Map<String, Serializable> props = decoded.getProperties();
                DocumentModel doc = session.createDocumentModel(importerService.getParent() + "/" + manco, decoded.getName(), decoded.getType());
                for (Map.Entry<String, Serializable> entry : props.entrySet()) {
                    String name = entry.getKey();
                    Serializable value = entry.getValue();
                    if (value instanceof Map) {
                        @SuppressWarnings("unchecked")
                        Map<String, Serializable> map = (Map<String, Serializable>) value;
                        propagateComplex(map, doc);
                    } else if (FILE_CONTENT.equals(name)) {
                        File bin = new File((String) value);
                        if (!bin.exists()) {
                            log.error("Binary at " + value + "does not exist");
                            sendNotification(102, "Binary at " + value + "does not exist");
                            continue;
                        }

                        Blob blob = Blobs.createBlob(bin);
                        doc.setPropertyValue(name, (Serializable) blob);
                    }
                    else {
                        doc.setPropertyValue(name, value);
                    }
                }

                try {
                    session.createDocument(doc);
                } catch (NuxeoException e) {
                    log.error("An error occurred during import; Continuing the process", e);
                    sendNotification(101, "An error occurred during import; Continuing the process");
                }
            } while (record != null);

        } catch (InterruptedException | IllegalAccessException | IOException e) {
            log.error(e);
            finalEx = e;
        } finally {
            boolean ok = finalEx == null;
            session.save();
            cleanUp(ok, finalEx);
            tailer.commit();
            tailer.close();
        }
    }

    @Override
    public String getTitle() {
        return XML_STREAM_IMPORTER_WORK;
    }

    protected void propagateComplex(Map<String, Serializable> props, DocumentModel doc) {
        for (Map.Entry<String, Serializable> entry : props.entrySet()) {
            String name = entry.getKey();
            Serializable value = entry.getValue();
            doc.setPropertyValue(name, value);
        }
    }

    protected void sendNotification(int code, String message) {

        Response response = new Response(code, message);

        String callback = Framework.getProperties().getProperty("nuxeo.importer.callback.url");
        if (StringUtils.isEmpty(callback)) {
            log.error("Could not send a notification to an external API. `nuxeo.importer.callback.url` is empty.");
            return;
        }

        try {
            URL url = new URL(callback);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod(HttpMethod.POST.getName());
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");

            send(response, conn);
        } catch (IOException e) {
            log.error("Could not send a notification to an external API", e);
        }

    }

    private void send(Response response, HttpURLConnection conn) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        try (OutputStream os = conn.getOutputStream()) {
            byte[] outStr = mapper.writeValueAsString(response).getBytes();
            conn.setFixedLengthStreamingMode(outStr.length);
            os.write(outStr);
        }
    }

    public static class Response {

        private int code;

        private String message;

        public Response() {

        }

        public Response(int code, String message) {
            this.code = code;
            this.message = message;
        }

        public int getCode() {
            return code;
        }

        public void setCode(int code) {
            this.code = code;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
}
