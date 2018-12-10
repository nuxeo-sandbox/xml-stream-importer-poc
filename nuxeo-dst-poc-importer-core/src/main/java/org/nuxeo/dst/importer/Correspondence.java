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

import static org.nuxeo.dst.importer.Constants.DC_DESCRIPTION;
import static org.nuxeo.dst.importer.Constants.DC_TITLE;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.avro.reflect.Nullable;
import org.apache.commons.lang.StringUtils;
import org.nuxeo.ecm.core.api.NuxeoException;

@XmlRootElement(name = Correspondence.SCHEMA)
@PropertyClass(schema = Correspondence.SCHEMA)
public class Correspondence implements Documentable {

    public static final String SCHEMA = "correspondence";

    public static final String HIDDEN_PROP = SCHEMA + ":hidden";

    public static final String HIDDEN_REASON_PROP = SCHEMA + ":hiddenReason";

    public static final String INGEST_METHOD_PROP = SCHEMA + ":ingestMethod";

    public static final String CONFIG_ADMIN_ONLY_PROP = SCHEMA + ":configAdminOnly";

    public static final String MANCO_PROP = SCHEMA + ":manco";

    public static final String DESIGN_PROP = SCHEMA + ":design";

    public static final String BUSINESS_OWNER_PROP = SCHEMA + ":businessOwner";

    public static final String ENTRY_TYPE_PROP = SCHEMA + ":correspondenceTypeEntry";

    public static final String AGENT_ID_PROP = SCHEMA + ":agentID";

    public static final String LEGAL_OWNER_PROP = SCHEMA + ":legalOwner";

    public static final String OWNER_TYPE_ENTRY_PROP = SCHEMA + ":ownerTypeEntry";

    public static final String EXTERNAL_SOURCE_SYSTEM_PROP = SCHEMA + ":sourceSystem";

    public static final String EXTERNAL_CREATE_DATE_PROP = SCHEMA + ":createdDate";

    @XmlAttribute(name = "title")
    @Property(value = DC_TITLE, required = true)
    private String title;

    @Property(value = "path", skip = true, required = true)
    private String path;

    @Nullable
    @Property(DC_DESCRIPTION)
    private String description;

    @Property(value = DESIGN_PROP, required = true)
    private String designId;

    @Nullable
    @Property(MANCO_PROP)
    private String manco;

    @Nullable
    @Property(INGEST_METHOD_PROP)
    private String ingestMethod;

    @Nullable
    @Property(AGENT_ID_PROP)
    private String[] agentIds;

    @Nullable
    @Property(LEGAL_OWNER_PROP)
    private List<String> legalOwners;

    @Nullable
    @Property(BUSINESS_OWNER_PROP)
    private String businessOwner;

    @Nullable
    @Property(ENTRY_TYPE_PROP)
    private String entryType;

    @Nullable
    @Property(OWNER_TYPE_ENTRY_PROP)
    private String ownerEntryType;

    @Property(value = HIDDEN_PROP, required = true)
    private String hidden;

    @Nullable
    @Property(HIDDEN_REASON_PROP)
    private String hiddenReason;

    @Nullable
    @PropertyClass(schema = "externalData")
    private External external;

    @Override
    public String getType() {
        return "correspondence";
    }

    @Override
    public String getDocumentPath() {
        return getPath();
    }

    @Override
    public String getName() {
        return getTitle();
    }

    public void setFieldValue(Field field, Object value) {
        try {
            this.getClass().getDeclaredField(field.getName()).set(this, value);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Map<String, Serializable> getProperties() throws IllegalAccessException {
        Map<String, Serializable> props = new HashMap<>();

        putValues(props, getClass());

        return props;
    }

    protected void putValues(Map<String, Serializable> props, Class clazz) throws IllegalAccessException {
        PropertyClass pc = getAnnotation(clazz);
        Field[] fields = clazz.getDeclaredFields();

        for (Field field : fields) {
            Object value = field.get(this);
            if (value != null && field.isAnnotationPresent(PropertyClass.class)) {
                putValues(props, value.getClass());
                continue;
            }

            if (!field.isAnnotationPresent(Property.class)) {
                continue;
            }

            Property annotation = field.getAnnotation(Property.class);
            if (annotation.skip()) {
                continue;
            }

            value = getFieldValue(field, annotation);

            props.put(buildPropertyPath(pc, annotation), (Serializable) value);
        }
    }

    protected String buildPropertyPath(PropertyClass pc, Property annotation) {
        if (StringUtils.isEmpty(pc.parent())) {
            return pc.schema() + ":" + annotation.value();
        }

        return pc.parent() + ":" + pc.schema() + "/" + annotation.value();
    }

    protected Object getFieldValue(Field field, Property annotation) throws IllegalAccessException {
        Object value = field.get(this);
        if (annotation.required() && value == null) {
            throw new AvroDocumentException(annotation.value() + " cannot be empty");
        }
        return value;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDesignId() {
        return designId;
    }

    public void setDesignId(String designId) {
        this.designId = designId;
    }

    public String getManco() {
        return manco;
    }

    public void setManco(String manco) {
        this.manco = manco;
    }

    public String getIngestMethod() {
        return ingestMethod;
    }

    public void setIngestMethod(String ingestMethod) {
        this.ingestMethod = ingestMethod;
    }

    public String[] getAgentIds() {
        return agentIds;
    }

    public void setAgentIds(String[] agentIds) {
        this.agentIds = agentIds;
    }

    public List<String> getLegalOwners() {
        return legalOwners;
    }

    public void setLegalOwners(List<String> legalOwners) {
        this.legalOwners = legalOwners;
    }

    public String getBusinessOwner() {
        return businessOwner;
    }

    public void setBusinessOwner(String businessOwner) {
        this.businessOwner = businessOwner;
    }

    public String getEntryType() {
        return entryType;
    }

    public void setEntryType(String entryType) {
        this.entryType = entryType;
    }

    public String getOwnerEntryType() {
        return ownerEntryType;
    }

    public void setOwnerEntryType(String ownerEntryType) {
        this.ownerEntryType = ownerEntryType;
    }

    public String getHidden() {
        return hidden;
    }

    public void setHidden(String hidden) {
        this.hidden = hidden;
    }

    public String getHiddenReason() {
        return hiddenReason;
    }

    public void setHiddenReason(String hiddenReason) {
        this.hiddenReason = hiddenReason;
    }

    public External getExternal() {
        return external;
    }

    public void setExternal(External external) {
        this.external = external;
    }

    public static PropertyClass getAnnotation(Class adapterClass) {
        if (!adapterClass.isAnnotationPresent(PropertyClass.class)) {
            throw new NuxeoException(adapterClass.getCanonicalName() + " is not annotated with " + PropertyClass.class.getCanonicalName());
        }

        return (PropertyClass) adapterClass.getAnnotation(PropertyClass.class);
    }
}
