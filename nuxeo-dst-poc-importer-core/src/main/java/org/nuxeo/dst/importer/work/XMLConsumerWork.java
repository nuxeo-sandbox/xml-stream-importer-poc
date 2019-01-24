/*
 * (C) Copyright 2006-2019 Nuxeo (http://nuxeo.com/) and others.
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
import static org.nuxeo.dst.importer.work.XMLProducerWork.XML_IMPORTER_CATEGORY;

import java.io.Externalizable;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Map;
import java.util.Objects;

import org.apache.avro.Schema;
import org.apache.avro.message.RawMessageDecoder;
import org.apache.avro.reflect.ReflectData;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.dst.importer.data.Documentable;
import org.nuxeo.dst.importer.service.NotificationService;
import org.nuxeo.dst.importer.service.XMLImporterService;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.Blobs;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.NuxeoException;
import org.nuxeo.ecm.core.work.AbstractWork;
import org.nuxeo.lib.stream.computation.Record;
import org.nuxeo.lib.stream.log.LogManager;
import org.nuxeo.lib.stream.log.LogRecord;
import org.nuxeo.lib.stream.log.LogTailer;
import org.nuxeo.runtime.api.Framework;
import org.nuxeo.runtime.stream.StreamService;

public class XMLConsumerWork extends AbstractWork {

    public static final Log log = LogFactory.getLog(XMLConsumerWork.class);

    public static final String XML_STREAM_CONSUMER_WORK = "XMLConsumerWork";

    public static final int DEFAULT_COMMIT_SIZE = 128;

    private String manco;

    private String logName;

    private Class importClass;

    public XMLConsumerWork(String repo, String user, String manco, Class theClass, String logName) {
        super();
        Objects.requireNonNull(repo);
        Objects.requireNonNull(manco);

        this.manco = manco;
        this.importClass = theClass;
        this.logName = logName;
        repositoryName = repo;
        originatingUsername = user;
    }

    @Override
    public void work() {
        StreamService ss = Framework.getService(StreamService.class);
        LogManager logger = ss.getLogManager("default");
        Schema schema = ReflectData.get().getSchema(importClass);
        consume(logger, schema);
    }

    @Override
    public String getTitle() {
        return XML_STREAM_CONSUMER_WORK;
    }

    @Override
    public String getCategory() {
        return XML_IMPORTER_CATEGORY;
    }

    protected void consume(LogManager logger, Schema schema) {
        XMLImporterService importerService = Framework.getService(XMLImporterService.class);
        RawMessageDecoder<Object> decoder = new RawMessageDecoder<>(ReflectData.get(), schema);
        LogTailer<Externalizable> tailer = logger.createTailer(id, logName);
        tailer.toLastCommitted();

        Exception finalEx = null;
        try {
            openUserSession();

            NotificationService ns = Framework.getService(NotificationService.class);

            int counter = 0;
            LogRecord<Externalizable> record;

            String path = Paths.get(importerService.getParent(), manco).toString();
            do {
                record = tailer.read(Duration.ofSeconds(5));
                if (record == null) {
                    continue;
                }

                Record message = (Record) record.message();
                Documentable decoded = (Documentable) decoder.decode(message.data);

                DocumentModel doc = session.createDocumentModel(path, decoded.getName(), decoded.getType());
                propagateProperties(ns, decoded, doc);

                try {
                    session.createDocument(doc);
                } catch (NuxeoException e) {
                    log.error("An error occurred during import; Continuing the process", e);
                    ns.send(101, "An error occurred during import; Continuing the process");
                }

                counter++;
                if (counter >= DEFAULT_COMMIT_SIZE) {
                    session.save();
                    tailer.commit();
                    counter = 0;
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

    protected void propagateProperties(NotificationService ns, Documentable decoded, DocumentModel doc) throws IllegalAccessException, IOException {
        Map<String, Serializable> props = decoded.getProperties();
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
                    ns.send(102, "Binary at " + value + "does not exist");
                    continue;
                }

                Blob blob = Blobs.createBlob(bin);
                doc.setPropertyValue(name, (Serializable) blob);
            }
            else {
                doc.setPropertyValue(name, value);
            }
        }
    }

    protected void propagateComplex(Map<String, Serializable> props, DocumentModel doc) {
        for (Map.Entry<String, Serializable> entry : props.entrySet()) {
            String name = entry.getKey();
            Serializable value = entry.getValue();
            doc.setPropertyValue(name, value);
        }
    }
}
