package org.nuxeo.dst.importer;

import static org.assertj.core.api.Java6Assertions.assertThat;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.common.utils.FileUtils;
import org.nuxeo.ecm.automation.AutomationService;
import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.automation.OperationException;
import org.nuxeo.ecm.automation.test.AutomationFeature;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.test.DefaultRepositoryInit;
import org.nuxeo.ecm.core.test.annotations.Granularity;
import org.nuxeo.ecm.core.test.annotations.RepositoryConfig;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;
import org.nuxeo.runtime.test.runner.LocalDeploy;

@RunWith(FeaturesRunner.class)
@Features(AutomationFeature.class)
@RepositoryConfig(init = DefaultRepositoryInit.class, cleanup = Granularity.METHOD)
@Deploy("org.nuxeo.dst.importer.nuxeo-dst-poc-importer-core")
@LocalDeploy({"org.nuxeo.dst.importer.nuxeo-dst-poc-importer-core:test-config-desc-contrib.xml"})
public class TestStartXMLBulkImport {

    @Inject
    protected CoreSession session;

    @Inject
    protected AutomationService automationService;

    @Test
    public void shouldCallWithParameters() throws OperationException {
        OperationContext ctx = new OperationContext(session);
        Map<String, Object> params = new HashMap<>();
        params.put("manco", "None");

        File xml = FileUtils.getResourceFileFromContext("test-correspondence0.xml");
        params.put("xml", xml.getAbsolutePath());
        DocumentModel doc = (DocumentModel) automationService.run(ctx, StartXMLBulkImport.ID, params);
        assertThat(doc).isNull();
    }
}
