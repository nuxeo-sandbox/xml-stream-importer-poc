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
package org.nuxeo.dst.importer.data;

import static org.nuxeo.dst.importer.common.Constants.DC_DESCRIPTION;
import static org.nuxeo.dst.importer.common.Constants.DC_TITLE;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.avro.reflect.Nullable;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.nuxeo.dst.importer.annotations.Property;
import org.nuxeo.dst.importer.annotations.PropertyClass;
import org.nuxeo.dst.importer.common.DateAdapter;
import org.nuxeo.dst.importer.exceptions.AvroDocumentException;
import org.nuxeo.ecm.core.api.NuxeoException;

@XmlRootElement(name = Correspondence.SCHEMA)
@PropertyClass(schema = Correspondence.SCHEMA)
public class Correspondence implements Documentable {

    private static final Log log = LogFactory.getLog(Correspondence.class);

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

    public static final String EXTERNAL_SOURCE_SYSTEM_PROP = SCHEMA + ":externalData/sourceSystem";

    public static final String EXTERNAL_CREATE_DATE_PROP = SCHEMA + ":externalData/createdDate";

    @Property(value = DC_TITLE, xmlValue = "title", required = true)
    private String title;

    @Property(value = "path", skip = true, required = true)
    private String path;

    @Nullable
    @Property(value = DC_DESCRIPTION, xmlValue = "description")
    private String description;

    @Property(value = DESIGN_PROP, xmlValue = "design", required = true)
    private String designId;

    @Nullable
    @Property(value = MANCO_PROP, xmlValue = "manco")
    private String manco;

    @Nullable
    @Property(INGEST_METHOD_PROP)
    private String ingestMethod;

    @Nullable
    @Property(value = AGENT_ID_PROP, xmlValue = "agentIds")
    private String[] agentIds;

    @Nullable
    @Property(LEGAL_OWNER_PROP)
    private List<LegalOwner> legalOwners;

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
    public void setDocumentPath(String path) {
        setPath(path);
    }

    @Override
    public String getName() {
        return getTitle();
    }

    public void setFieldValue(Field field, Object value) {
        try {
            this.getClass().getDeclaredField(field.getName()).set(this, value);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            log.error(e);
        }
    }

    @Override
    public Map<String, Serializable> getProperties() throws IllegalAccessException {
        Map<String, Serializable> props = new HashMap<>();

        putValues(props, this);

        return props;
    }

    protected void putValues(Map<String, Serializable> props, Object object) throws IllegalAccessException {
        PropertyClass pc = getAnnotation(object.getClass());
        Field[] fields = object.getClass().getDeclaredFields();

        for (Field field : fields) {
            Object value = field.get(object);
            if (value != null && field.isAnnotationPresent(PropertyClass.class)) {
                putValues(props, value);
                continue;
            }

            if (!field.isAnnotationPresent(Property.class)) {
                continue;
            }

            Property annotation = field.getAnnotation(Property.class);
            if (annotation.skip()) {
                continue;
            }

            value = getFieldValue(field, annotation, object);
            if (value != null) {
                props.put(buildPropertyPath(pc, annotation), (Serializable) value);
            }
        }
    }

    protected String buildPropertyPath(PropertyClass pc, Property annotation) {
//        if (StringUtils.isEmpty(pc.parent())) {
        return annotation.value();
//        }

//        return pc.parent() + ":" + pc.schema() + "/" + annotation.value();

    }

    protected Object getFieldValue(Field field, Property annotation, Object ref) throws IllegalAccessException {
        Object value = field.get(ref);
        if (annotation.required() && value == null) {
            throw new AvroDocumentException(annotation.value() + " cannot be empty");
        }
        if (value != null && annotation.value().contains("legalOwner")) {
            value = ((List<LegalOwner>) value).stream()
                    .map(lo -> lo.ownerProps)
                    .collect(Collectors.toList());
        }

        return value;
    }

    public String getTitle() {
        return title;
    }

    @XmlElement(name = "title")
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

    @XmlElement(name = "description")
    public void setDescription(String description) {
        this.description = description;
    }

    public String getDesignId() {
        return designId;
    }

    @XmlElement(name = "design")
    public void setDesignId(String designId) {
        this.designId = designId;
    }

    public String getManco() {
        return manco;
    }

    @XmlElement(name = "manco")
    public void setManco(String manco) {
        this.manco = manco;
    }

    public String getIngestMethod() {
        return ingestMethod;
    }

    @XmlElement(name = "ingestMethod")
    public void setIngestMethod(String ingestMethod) {
        this.ingestMethod = ingestMethod;
    }

    public String[] getAgentIds() {
        return agentIds;
    }

    @XmlElementWrapper(name = "agentIds")
    @XmlElement(name = "value")
    public void setAgentIds(String[] agentIds) {
        this.agentIds = agentIds;
    }

    public List<LegalOwner> getLegalOwners() {
        return legalOwners;
    }

    //    @XmlElementWrapper(name = "legalOwners")
    @XmlElement(name = "legalOwner")
    public void setLegalOwners(List<LegalOwner> legalOwners) {
        this.legalOwners = legalOwners;
    }

    public String getBusinessOwner() {
        return businessOwner;
    }

    @XmlElement(name = "businessOwner")
    public void setBusinessOwner(String businessOwner) {
        this.businessOwner = businessOwner;
    }

    public String getEntryType() {
        return entryType;
    }

    @XmlElement(name = "entryType")
    public void setEntryType(String entryType) {
        this.entryType = entryType;
    }

    public String getOwnerEntryType() {
        return ownerEntryType;
    }

    @XmlElement(name = "ownerEntryType")
    public void setOwnerEntryType(String ownerEntryType) {
        this.ownerEntryType = ownerEntryType;
    }

    public String getHidden() {
        return hidden;
    }

    @XmlElement(name = "hidden")
    public void setHidden(String hidden) {
        this.hidden = hidden;
    }

    public String getHiddenReason() {
        return hiddenReason;
    }

    @XmlElement(name = "hiddenReason")
    public void setHiddenReason(String hiddenReason) {
        this.hiddenReason = hiddenReason;
    }

    public External getExternal() {
        return external;
    }

    @XmlElement(name = "external")
    public void setExternal(External external) {
        this.external = external;
    }

    private static PropertyClass getAnnotation(Class adapterClass) {
        if (!adapterClass.isAnnotationPresent(PropertyClass.class)) {
            throw new NuxeoException(adapterClass.getCanonicalName() + " is not annotated with " + PropertyClass.class.getCanonicalName());
        }

        return (PropertyClass) adapterClass.getAnnotation(PropertyClass.class);
    }

    @XmlRootElement(name = "legalOwner")
    @PropertyClass(schema = "legalOwner", parent = "correspondence")
    protected static class LegalOwner {

        @Nullable
//        @Property(LEGAL_OWNER_PROP + "/0/designation")
        protected String designation;

        @Nullable
//        @Property(LEGAL_OWNER_PROP + "/0/legalOwnerID")
        protected String legalOwnerID;

        @Nullable
        @Property(LEGAL_OWNER_PROP)
        protected Map<String, String> ownerProps = new HashMap<>();

        public String getDesignation() {
            return designation;
        }

        @XmlElement(name = "designation")
        public void setDesignation(String designation) {
            ownerProps.put("designation", designation);
            this.designation = designation;
        }

        public String getLegalOwnerID() {
            return legalOwnerID;
        }

        @XmlElement(name = "legalOwnerID")
        public void setLegalOwnerID(String legalOwnerID) {
            ownerProps.put("legalOwnerID", legalOwnerID);
            this.legalOwnerID = legalOwnerID;
        }
    }


    @XmlRootElement(name = "external")
    @PropertyClass(schema = "externalData", parent = "correspondence")
    protected static class External {

        @Nullable
        @Property(EXTERNAL_SOURCE_SYSTEM_PROP)
        protected String sourceSystem;

        @Nullable
        @Property(EXTERNAL_CREATE_DATE_PROP)
        protected Date createDate;

        public String getSourceSystem() {
            return sourceSystem;
        }

        @XmlElement(name = "sourceSystem")
        public void setSourceSystem(String sourceSystem) {
            this.sourceSystem = sourceSystem;
        }

        public Date getCreateDate() {
            return createDate;
        }

        @XmlElement(name = "createDate")
        @XmlJavaTypeAdapter(DateAdapter.class)
        public void setCreateDate(Date createDate) {
            this.createDate = createDate;
        }
    }
}
