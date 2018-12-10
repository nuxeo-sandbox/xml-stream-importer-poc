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

import static org.nuxeo.dst.importer.Correspondence.EXTERNAL_CREATE_DATE_PROP;
import static org.nuxeo.dst.importer.Correspondence.EXTERNAL_SOURCE_SYSTEM_PROP;

import java.util.Date;

import org.apache.avro.reflect.Nullable;

@PropertyClass(schema = "externalData", parent = "correspondence")
public class External {

    @Nullable
    @Property(EXTERNAL_SOURCE_SYSTEM_PROP)
    private String sourceSystem;

    @Nullable
    @Property(EXTERNAL_CREATE_DATE_PROP)
    private Date createDate;

    public String getSourceSystem() {
        return sourceSystem;
    }

    public void setSourceSystem(String sourceSystem) {
        this.sourceSystem = sourceSystem;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }
}
