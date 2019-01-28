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
import static org.nuxeo.dst.importer.common.Constants.FILE_CONTENT;

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
import org.nuxeo.dst.importer.exceptions.MissingFieldException;

@XmlRootElement(name = Correspondence.SCHEMA)
@PropertyClass(schema = Correspondence.SCHEMA)
public class Correspondence implements Documentable, CorrespondenceCommon {

    private static final Log log = LogFactory.getLog(Correspondence.class);

    @Property(value = DC_TITLE, required = true)
    private String title;

    @Nullable
    @Property(value = DC_DESCRIPTION)
    private String description;

    @Property(value = "path", skip = true, required = true)
    private String path;

    @Nullable
    @Property(SEDOL_PROP)
    private String sedol;

    @Nullable
    @Property(ACCESS_DATE_PROP)
    protected Date accessDate;

    @Nullable
    @Property(value = AGENT_ID_PROP)
    private String[] agentIds;

    @Nullable
    @Property(value = BRAND_PROP)
    private String brand;

    @Nullable
    @Property(value = BUSINESS_ASSOCIATE_ID_PROP)
    private String[] businessAssociateID;

    @Nullable
    @Property(BUSINESS_OWNER_PROP)
    private String businessOwner;

    @Nullable
    @Property(value = CONFIG_ADMIN_ONLY_PROP)
    private String configAdminOnly = "false";

    @Nullable
    @Property(ENTRY_TYPE_PROP)
    private String entryType;

    @Nullable
    @Property(DELIVERY_METHOD)
    private String deliveryMethod;

    @Property(value = DESIGN_PROP, required = true)
    private String designId;

    @Nullable
    @Property(value = DIRECTION_PROP)
    private String direction = "OutBound";

    @Nullable
    @Property(EFFECTIVE_DATE_PROP)
    protected Date effectiveDate;

    @Nullable
    @Property(EVENT_ID_PROP)
    protected String eventId;

    @Nullable
    @PropertyClass(schema = "externalData")
    private External external;

    @Nullable
    @PropertyClass(schema = "fundInformation")
    private FundInformation fundInformation;

    @Nullable
    @PropertyClass(schema = GENERATION_METHOD_PROP)
    private String generationMethod;

    @Property(value = HIDDEN_PROP, required = true)
    private String hidden = "false";

    @Nullable
    @Property(HIDDEN_REASON_PROP)
    private String hiddenReason;

    @Nullable
    @Property(IMPORT_BATCH_ID_PROP)
    private String importBatchID;

    @Nullable
    @Property(INGEST_METHOD_PROP)
    private String ingestMethod = "Manual";

    @Nullable
    @Property(IS_DUPLICATE_PROP)
    private String isDuplicate = "Manual";

    @Nullable
    @Property(LAST_STATE_CHANGE_DATE_PROP)
    private String lastStateChangeDate;

    @Nullable
    @Property(LEGAL_OWNER_PROP)
    private List<LegalOwner> legalOwners;

    @Nullable
    @Property(value = MANCO_PROP)
    private String manco;

    @Nullable
    @Property(OWNER_TYPE_ENTRY_PROP)
    private String ownerEntryType;

    @Nullable
    @Property(PULL_CODE_PROP)
    private String pullCode;

    @Nullable
    @PropertyClass(schema = "recipient")
    private Recipient recipient;

    @Nullable
    @Property(SCHEME_ID_PROP)
    private String schemeID;

    @Nullable
    @Property(value = FILE_CONTENT)
    private String content;


    public Correspondence() {
    }

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
    public Map<String, Serializable> getProperties() throws IllegalAccessException, MissingFieldException {
        Map<String, Serializable> props = new HashMap<>();

        putValues(props, this);

        return props;
    }

    protected void putValues(Map<String, Serializable> props, Object object) throws IllegalAccessException, MissingFieldException {
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
                props.put(annotation.value(), (Serializable) value);
            }
        }
    }

    @SuppressWarnings("unchecked")
    protected Object getFieldValue(Field field, Property annotation, Object ref) throws IllegalAccessException, MissingFieldException {
        Object value = field.get(ref);
        if (annotation.required() && value == null) {
            throw new MissingFieldException(annotation.value() + " cannot be empty", field.getName());
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

    public String getContent() {
        return content;
    }

    @XmlElement(name = "content")
    public void setContent(String content) {
        this.content = content;
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

    public String getSedol() {
        return sedol;
    }

    @XmlElement(name = "SEDOL")
    public void setSedol(String sedol) {
        this.sedol = sedol;
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

    @XmlJavaTypeAdapter(DateAdapter.class)
    @XmlElement(name = "accessDate")
    public void setAccessDate(Date accessDate) {
        this.accessDate = accessDate;
    }

    @XmlElement(name = "brand")
    public void setBrand(String brand) {
        this.brand = brand;
    }

    @XmlElement(name = "businessAssociateID")
    public void setBusinessAssociateID(String[] businessAssociateID) {
        this.businessAssociateID = businessAssociateID;
    }

    @XmlElement(name = "configAdminOnly")
    public void setConfigAdminOnly(String configAdminOnly) {
        this.configAdminOnly = configAdminOnly;
    }

    @XmlElement(name = "deliveryMethod")
    public void setDeliveryMethod(String deliveryMethod) {
        this.deliveryMethod = deliveryMethod;
    }

    @XmlElement(name = "direction")
    public void setDirection(String direction) {
        this.direction = direction;
    }

    @XmlJavaTypeAdapter(DateAdapter.class)
    @XmlElement(name = "effectiveDate")
    public void setEffectiveDate(Date effectiveDate) {
        this.effectiveDate = effectiveDate;
    }

    @XmlElement(name = "eventId")
    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    @XmlElement(name = "fundInformation")
    public void setFundInformation(FundInformation fundInformation) {
        this.fundInformation = fundInformation;
    }

    @XmlElement(name = "generationMethod")
    public void setGenerationMethod(String generationMethod) {
        this.generationMethod = generationMethod;
    }

    @XmlElement(name = "importBatchID")
    public void setImportBatchID(String importBatchID) {
        this.importBatchID = importBatchID;
    }

    @XmlElement(name = "isDuplicate")
    public void setIsDuplicate(String isDuplicate) {
        this.isDuplicate = isDuplicate;
    }

    @XmlElement(name = "lastStateChangeDate")
    public void setLastStateChangeDate(String lastStateChangeDate) {
        this.lastStateChangeDate = lastStateChangeDate;
    }

    @XmlElement(name = "pullCode")
    public void setPullCode(String pullCode) {
        this.pullCode = pullCode;
    }

    @XmlElement(name = "recipient")
    public void setRecipient(Recipient recipient) {
        this.recipient = recipient;
    }

    @XmlElement(name = "schemeID")
    public void setSchemeID(String schemeID) {
        this.schemeID = schemeID;
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

    @XmlRootElement(name = "fundInformation")
    @PropertyClass(schema = "fundInformation", parent = "correspondence")
    protected static class FundInformation {

        @Nullable
        @Property(FUND_INFO_NAME_PROP)
        protected String fundName;

        @Nullable
        @Property(FUND_INFO_DISTRIBUTION_DATE)
        protected Date distributionDate;

        public String getFundName() {
            return fundName;
        }

        @XmlElement(name = "fundName")
        public void setFundName(String fundName) {
            this.fundName = fundName;
        }

        public Date getDistributionDate() {
            return distributionDate;
        }

        @XmlElement(name = "distributionDate")
        @XmlJavaTypeAdapter(DateAdapter.class)
        public void setDistributionDate(Date distributionDate) {
            this.distributionDate = distributionDate;
        }
    }

    @XmlRootElement(name = "recipient")
    @PropertyClass(schema = "recipient", parent = "correspondence")
    protected static class Recipient {

        @Nullable
        @Property(RECIPIENT_NAME_PROP)
        protected String name;

        @Nullable
        @Property(RECIPIENT_ADDRESS_PROP)
        protected String address;

        @Nullable
        @Property(RECIPIENT_EMAIL_PROP)
        protected String email;

        @Nullable
        @Property(RECIPIENT_FAX_PROP)
        protected String fax;

        @Nullable
        @Property(RECIPIENT_PHONE_PROP)
        protected String phone;

        @Nullable
        @Property(RECIPIENT_POST_CODE_PROP)
        protected String postCode;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getAddress() {
            return address;
        }

        @XmlElement(name = "address")
        public void setAddress(String address) {
            this.address = address;
        }

        public String getEmail() {
            return email;
        }

        @XmlElement(name = "email")
        public void setEmail(String email) {
            this.email = email;
        }

        public String getFax() {
            return fax;
        }

        @XmlElement(name = "fax")
        public void setFax(String fax) {
            this.fax = fax;
        }

        public String getPhone() {
            return phone;
        }

        @XmlElement(name = "phone")
        public void setPhone(String phone) {
            this.phone = phone;
        }

        public String getPostCode() {
            return postCode;
        }

        @XmlElement(name = "postCode")
        public void setPostCode(String postCode) {
            this.postCode = postCode;
        }
    }
}
