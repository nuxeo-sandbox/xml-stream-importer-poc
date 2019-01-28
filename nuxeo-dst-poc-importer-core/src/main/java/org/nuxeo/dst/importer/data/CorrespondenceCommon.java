/*
 * (C) Copyright 2006-2019 Nuxeo (http://nuxeo.com/) and others.
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

public abstract class CorrespondenceCommon {

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

    public static final String SEDOL_PROP = SCHEMA + ":SEDOL";

    public static final String OWNER_TYPE_ENTRY_PROP = SCHEMA + ":ownerTypeEntry";

    public static final String ACCESS_DATE_PROP = SCHEMA + ":accessDate";

    public static final String BRAND_PROP = SCHEMA + ":brand";

    public static final String BUSINESS_ASSOCIATE_ID_PROP = SCHEMA + "businessAssociateID";

    public static final String DELIVERY_METHOD = SCHEMA + ":deliveryMethod";

    public static final String DIRECTION_PROP = SCHEMA + ":direction";

    public static final String EFFECTIVE_DATE_PROP = SCHEMA + ":effectiveDate";

    public static final String EVENT_ID_PROP = SCHEMA + ":eventId";

    public static final String GENERATION_METHOD_PROP = SCHEMA + ":generationMethod";

    public static final String IMPORT_BATCH_ID_PROP = SCHEMA + ":importBatchID";

    public static final String IS_DUPLICATE_PROP = SCHEMA + ":isDuplicate";

    public static final String LAST_STATE_CHANGE_DATE_PROP = SCHEMA + ":lastStateChangeDate";

    public static final String PULL_CODE_PROP = SCHEMA + ":pullCode";

    public static final String RECIPIENT_NAME_PROP = SCHEMA + ":recipient/name";

    public static final String RECIPIENT_EMAIL_PROP = SCHEMA + ":recipient/email";

    public static final String RECIPIENT_ADDRESS_PROP = SCHEMA + ":recipient/address";

    public static final String RECIPIENT_FAX_PROP = SCHEMA + ":recipient/fax";

    public static final String RECIPIENT_PHONE_PROP = SCHEMA + ":recipient/phone";

    public static final String RECIPIENT_POST_CODE_PROP = SCHEMA + ":recipient/postCode";

    public static final String SCHEME_ID_PROP = SCHEMA + ":schemeID";

    public static final String FUND_INFO_NAME_PROP = SCHEMA + ":fundInformation/fundName";

    public static final String FUND_INFO_DISTRIBUTION_DATE = SCHEMA + ":fundInformation/distributionDate";

    public static final String EXTERNAL_SOURCE_SYSTEM_PROP = SCHEMA + ":externalData/sourceSystem";

    public static final String EXTERNAL_CREATE_DATE_PROP = SCHEMA + ":externalData/createdDate";
}
