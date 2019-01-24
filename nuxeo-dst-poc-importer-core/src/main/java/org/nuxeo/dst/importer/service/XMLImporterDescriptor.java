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

import java.io.Serializable;

import org.nuxeo.common.xmap.annotation.XNode;
import org.nuxeo.common.xmap.annotation.XObject;
import org.nuxeo.dst.importer.data.Wrapper;

@XObject("configuration")
public class XMLImporterDescriptor implements Serializable {

    private static final long serialVersionUID = 1L;

    @XNode("@wrapperClass")
    protected Class<Wrapper> wrapper;

    @XNode("@parent")
    protected String parent;


    public Class<Wrapper> getWrapperClass() {
        return wrapper;
    }

    public String getParent() {
        return parent;
    }
}
