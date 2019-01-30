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
package org.nuxeo.dst.importer.service;

import java.io.File;
import java.util.List;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.nuxeo.dst.importer.model.Documentable;
import org.nuxeo.dst.importer.model.Wrapper;
import org.nuxeo.runtime.model.ComponentInstance;
import org.nuxeo.runtime.model.DefaultComponent;

public class XMLImporterComponent extends DefaultComponent implements XMLImporterService {

    private static final Log log = LogFactory.getLog(XMLImporterComponent.class);

    public static final String CONFIGURATION_EP = "configuration";

    protected XMLImporterDescriptor descriptor;

    @Override
    public void registerContribution(Object contribution, String extensionPoint, ComponentInstance contributor) {
        if (CONFIGURATION_EP.equals(extensionPoint) && contribution instanceof XMLImporterDescriptor) {
            descriptor = (XMLImporterDescriptor) contribution;
        }
    }

    @Override
    public List<? extends Documentable> parse(File xml) throws JAXBException {
        Class<Wrapper> wrapper = descriptor.getWrapperClass();
        JAXBContext ctx = JAXBContext.newInstance(wrapper);
        Unmarshaller unmarshaller = ctx.createUnmarshaller();
        Wrapper unmarshal = wrapper.cast(unmarshaller.unmarshal(xml));

        return unmarshal.getList();
    }

    @Override
    public String getParent() {
        return descriptor.getParent();
    }
}
