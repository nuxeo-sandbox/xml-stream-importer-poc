package org.nuxeo.dst.importer;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.nuxeo.dst.importer.data.CorrespondenceCommon.AGENT_ID_PROP;
import static org.nuxeo.dst.importer.data.CorrespondenceCommon.DESIGN_PROP;
import static org.nuxeo.dst.importer.data.CorrespondenceCommon.EXTERNAL_SOURCE_SYSTEM_PROP;
import static org.nuxeo.dst.importer.data.CorrespondenceCommon.LEGAL_OWNER_PROP;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.common.utils.FileUtils;
import org.nuxeo.dst.importer.operations.StartXMLBulkImport;
import org.nuxeo.ecm.automation.AutomationService;
import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.automation.OperationException;
import org.nuxeo.ecm.automation.test.AutomationFeature;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.PathRef;
import org.nuxeo.ecm.core.test.DefaultRepositoryInit;
import org.nuxeo.ecm.core.test.annotations.Granularity;
import org.nuxeo.ecm.core.test.annotations.RepositoryConfig;
import org.nuxeo.ecm.core.work.api.WorkManager;
import org.nuxeo.runtime.api.Framework;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;
import org.nuxeo.runtime.test.runner.LocalDeploy;
import org.nuxeo.runtime.transaction.TransactionHelper;

@RunWith(FeaturesRunner.class)
@Features(AutomationFeature.class)
@Deploy({"org.nuxeo.dst.importer.nuxeo-dst-poc-importer-core", "org.nuxeo.runtime.stream"})
@LocalDeploy({
        "org.nuxeo.dst.importer.nuxeo-dst-poc-importer-core:test-config-desc-contrib.xml",
        "org.nuxeo.dst.importer.nuxeo-dst-poc-importer-core:test-doc-type-contrib.xml",
        "org.nuxeo.dst.importer.nuxeo-dst-poc-importer-core:test-stream-contrib.xml"})
@RepositoryConfig(init = DefaultRepositoryInit.class, cleanup = Granularity.METHOD)
public class TestStartXMLBulkImport {

    @Inject
    protected CoreSession session;

    @Inject
    protected AutomationService automationService;

    @Test
    public void shouldCallWithParameters() throws OperationException, InterruptedException {
        Framework.getProperties().put("nuxeo.importer.callback.url", "http://localhost");

        String mancoTest = "mancoTest";
        DocumentModel parent = session.createDocumentModel("/", mancoTest, "Folder");
        parent.setPropertyValue("dc:title", mancoTest);
        parent = session.createDocument(parent);
        assertNotNull(parent);

        session.save();

        TransactionHelper.commitOrRollbackTransaction();
        TransactionHelper.startTransaction();

        OperationContext ctx = new OperationContext(session);
        Map<String, Object> params = new HashMap<>();
        params.put("manco", mancoTest);

        File xml = FileUtils.getResourceFileFromContext("test-correspondence0.xml");
        params.put("location", xml.getAbsolutePath());
        DocumentModel doc = (DocumentModel) automationService.run(ctx, StartXMLBulkImport.ID, params);
        assertThat(doc).isNull();

        WorkManager workManager = Framework.getService(WorkManager.class);
        workManager.awaitCompletion(5, TimeUnit.MINUTES);
        assertThat(workManager).isNotNull();

        DocumentModel doc0 = session.getDocument(new PathRef("/" + mancoTest + "/NewCorrespondence0"));
        assertNotNull(doc0);

        String design = (String) doc0.getPropertyValue(DESIGN_PROP);
        assertEquals("Awesome", design);

        @SuppressWarnings("unchecked")
        List<Object> owners = (List<Object>) doc0.getPropertyValue(LEGAL_OWNER_PROP);
        assertThat(owners).hasSize(2);

        DocumentModel doc1 = session.getDocument(new PathRef("/" + mancoTest + "/NewCorrespondence1"));
        assertNotNull(doc1);

        String[] ids = (String[]) doc1.getPropertyValue(AGENT_ID_PROP);
        assertThat(ids).contains("test1", "test2");

        String srcSys = (String) doc1.getPropertyValue(EXTERNAL_SOURCE_SYSTEM_PROP);
        assertEquals("Git", srcSys);
    }

    @Test
    public void shouldCommitOnMultiple() throws InterruptedException, OperationException {
        shouldCallWithParameters();
        shouldCallWithParameters();
    }
}
