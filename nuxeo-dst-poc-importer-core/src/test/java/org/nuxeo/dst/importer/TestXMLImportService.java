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
package org.nuxeo.dst.importer;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.util.List;
import javax.xml.bind.JAXBException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.common.utils.FileUtils;
import org.nuxeo.directory.test.DirectoryFeature;
import org.nuxeo.dst.importer.model.Documentable;
import org.nuxeo.dst.importer.service.XMLImporterService;
import org.nuxeo.ecm.platform.test.PlatformFeature;
import org.nuxeo.runtime.api.Framework;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;
import org.nuxeo.runtime.test.runner.LocalDeploy;

@RunWith(FeaturesRunner.class)
@Features({PlatformFeature.class, DirectoryFeature.class})
@Deploy({"org.nuxeo.dst.importer.nuxeo-dst-poc-importer-core"})
@LocalDeploy({"org.nuxeo.dst.importer.nuxeo-dst-poc-importer-core:test-config-desc-contrib.xml"})
public class TestXMLImportService {

    @Test
    public void shouldContributeService() {
        XMLImporterService service = Framework.getService(XMLImporterService.class);
        assertNotNull(service);
    }

    @Test
    public void shouldImport() throws JAXBException {
        File xml = FileUtils.getResourceFileFromContext("test-correspondence0.xml");
        XMLImporterService service = Framework.getService(XMLImporterService.class);
        List<? extends Documentable> parsed = service.parse(xml);
        assertThat(parsed).isNotEmpty();
        assertThat(parsed).hasSize(4);

        String documentPath = parsed.get(0).getDocumentPath();
    }
}
