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

import static org.nuxeo.dst.importer.Correspondence.getAnnotation;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.nuxeo.dst.importer.Correspondence;
import org.nuxeo.dst.importer.Documentable;
import org.nuxeo.dst.importer.Property;
import org.nuxeo.dst.importer.PropertyClass;
import org.nuxeo.ecm.core.api.NuxeoException;
import org.nuxeo.runtime.model.ComponentInstance;
import org.nuxeo.runtime.model.DefaultComponent;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class XMLImporterComponent extends DefaultComponent implements XMLImporterService {

    private static final Log log = LogFactory.getLog(XMLImporterComponent.class);

    private static final DocumentBuilder documentBuilder;

    public static final String CONFIGURATION_EP = "configuration";

    protected XMLImporterDescriptor descriptor;

    static {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try {
            documentBuilder = factory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            log.error(e);
            throw new NuxeoException(e);
        }
    }

    @Override
    public void registerContribution(Object contribution, String extensionPoint, ComponentInstance contributor) {
        if (CONFIGURATION_EP.equals(extensionPoint) && contribution instanceof XMLImporterDescriptor) {
            descriptor = (XMLImporterDescriptor) contribution;
        }
    }

    @Override
    public void doImport(File xml) throws IOException, SAXException, IllegalAccessException {
        Class adapterClass = descriptor.getAdapterClass();

        PropertyClass pc = getAnnotation(adapterClass);

        List<Documentable> docs = new LinkedList<>();

        Document document = documentBuilder.parse(xml);
        NodeList elements = document.getElementsByTagName(pc.schema());
        for (int i = 0; i < elements.getLength(); i++) {
            Element item = (Element) elements.item(i);
            Field[] fields = adapterClass.getDeclaredFields();
            Correspondence correspondence = new Correspondence();
            for (Field field : fields) {
                if (!field.isAnnotationPresent(Property.class)) {
                    continue;
                }
                Property prop = field.getAnnotation(Property.class);
                Node node = item.getElementsByTagName(prop.value()).item(0);
                if (node == null) {
                    continue;
                }

                String value = node.getChildNodes().item(0).getNodeValue();
                Object converted = convert(value, field.getType());
                correspondence.setFieldValue(field, converted);
            }

            docs.add(correspondence);
        }



        assert !docs.isEmpty();
    }

    protected Object convert(String value, Class type) {
        if (type.equals(String.class)) {
            return value;
        } else if (type.equals(Integer.class)) {
            return Integer.valueOf(value);
        } else if (type.equals(Boolean.class)) {
            return Boolean.valueOf(value);
        } else {
            return null;
        }
    }
}
