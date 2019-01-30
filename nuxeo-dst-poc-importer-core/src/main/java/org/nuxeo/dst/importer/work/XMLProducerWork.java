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

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import javax.xml.bind.JAXBException;

import org.apache.avro.Schema;
import org.apache.avro.message.RawMessageEncoder;
import org.apache.avro.reflect.ReflectData;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.nuxeo.dst.importer.model.Documentable;
import org.nuxeo.dst.importer.service.NotificationService;
import org.nuxeo.dst.importer.service.XMLImporterService;
import org.nuxeo.ecm.core.api.NuxeoException;
import org.nuxeo.ecm.core.api.PathRef;
import org.nuxeo.ecm.core.work.AbstractWork;
import org.nuxeo.ecm.core.work.api.WorkManager;
import org.nuxeo.lib.stream.computation.Record;
import org.nuxeo.lib.stream.log.LogAppender;
import org.nuxeo.lib.stream.log.LogManager;
import org.nuxeo.lib.stream.log.LogOffset;
import org.nuxeo.runtime.api.Framework;
import org.nuxeo.runtime.stream.StreamService;

public class XMLProducerWork extends AbstractWork {

    private static final Log log = LogFactory.getLog(XMLProducerWork.class);

    public static final String LOG_MANAGER_NAME_PROP = "nuxeo.importer.log.name";

    public static final String XML_STREAM_PRODUCER_WORK = "XMLProducerWork";

    public static final String XML_IMPORTER_CATEGORY = "XMLImporter";

    private final String manco;

    private final String filePath;

    public XMLProducerWork(String repo, String user, String manco, String filePath) {
        super(manco + "_" + Math.abs(RANDOM.nextInt()));

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
        NotificationService ns = Framework.getService(NotificationService.class);

        File file = new File(filePath);
        if (!file.exists()) {
            ns.send(404, filePath + " does not exist");
            throw new NuxeoException(filePath + " does not exist");
        }

        XMLImporterService importerService = Framework.getService(XMLImporterService.class);
        String mancoPath = Paths.get(importerService.getParent(), manco).toString();

        checkManco(ns, mancoPath);

        List<? extends Documentable> docs;
        try {
            docs = importerService.parse(file);
            if (docs.isEmpty()) {
                log.info("nothing to process");
                ns.send(400, "nothing to process");
                return;
            }
        } catch (JAXBException e) {
            log.error(e);
            ns.send(400, e.getMessage());
            throw new NuxeoException(e);
        }

        String logName = id + "_" + manco;

        LogAppender<Record> appender;
        try {
            StreamService ss = Framework.getService(StreamService.class);
            String managerName = Framework.getProperty(LOG_MANAGER_NAME_PROP, "default");
            LogManager logger = ss.getLogManager(managerName);

            logger.createIfNotExists(logName, 2);
            appender = logger.getAppender(logName);
        } catch (Exception e) {
            ns.send(500, "Cannot connect to Nuxeo Stream");
            throw new NuxeoException(e);
        }

        Class<? extends Documentable> aClass = docs.get(0).getClass();
        Schema schema = ReflectData.get().getSchema(aClass);

        RawMessageEncoder<Object> encoder = new RawMessageEncoder<>(ReflectData.get(), schema);

        for (Documentable doc : docs) {
            doc.setDocumentPath(mancoPath);

            try {
                ByteBuffer buf = encoder.encode(doc);
                LogOffset offset = appender.append(doc.getName(), Record.of(doc.getName(), buf.array()));
            } catch (IOException e) {
                log.error(e);
                ns.send(400, e.getMessage()); // Collecting the errors
            }
        }

        WorkManager wm = Framework.getService(WorkManager.class);
        XMLConsumerWork work = new XMLConsumerWork(repositoryName, originatingUsername, manco, aClass, logName);
        wm.schedule(work);
    }

    @Override
    public String getTitle() {
        return XML_STREAM_PRODUCER_WORK;
    }

    @Override
    public String getCategory() {
        return XML_IMPORTER_CATEGORY;
    }

    protected void checkManco(NotificationService ns, String mancoPath) {
        try {
            openSystemSession();
            if (!session.exists(new PathRef(mancoPath))) {
                ns.send(404, manco + " does not exist");
                throw new NuxeoException(manco + " does not exist");
            }
        } finally {
            cleanUp(true,  null);
        }
    }
}
