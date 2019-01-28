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

public interface CorrespondenceCommon {

    String SCHEMA = "correspondence";

    String HIDDEN_PROP = SCHEMA + ":hidden";

    String HIDDEN_REASON_PROP = SCHEMA + ":hiddenReason";

    String INGEST_METHOD_PROP = SCHEMA + ":ingestMethod";

    String CONFIG_ADMIN_ONLY_PROP = SCHEMA + ":configAdminOnly";

    String MANCO_PROP = SCHEMA + ":manco";

    String DESIGN_PROP = SCHEMA + ":design";

    String BUSINESS_OWNER_PROP = SCHEMA + ":businessOwner";

    String ENTRY_TYPE_PROP = SCHEMA + ":correspondenceTypeEntry";

    String AGENT_ID_PROP = SCHEMA + ":agentID";

    String LEGAL_OWNER_PROP = SCHEMA + ":legalOwner";

    String SEDOL_PROP = SCHEMA + ":SEDOL";

    String OWNER_TYPE_ENTRY_PROP = SCHEMA + ":ownerTypeEntry";

    String ACCESS_DATE_PROP = SCHEMA + ":accessDate";

    String BRAND_PROP = SCHEMA + ":brand";

    String BUSINESS_ASSOCIATE_ID_PROP = SCHEMA + "businessAssociateID";

    String DELIVERY_METHOD = SCHEMA + ":deliveryMethod";

    String DIRECTION_PROP = SCHEMA + ":direction";

    String EFFECTIVE_DATE_PROP = SCHEMA + ":effectiveDate";

    String EVENT_ID_PROP = SCHEMA + ":eventId";

    String GENERATION_METHOD_PROP = SCHEMA + ":generationMethod";

    String IMPORT_BATCH_ID_PROP = SCHEMA + ":importBatchID";

    String IS_DUPLICATE_PROP = SCHEMA + ":isDuplicate";

    String LAST_STATE_CHANGE_DATE_PROP = SCHEMA + ":lastStateChangeDate";

    String PULL_CODE_PROP = SCHEMA + ":pullCode";

    String RECIPIENT_NAME_PROP = SCHEMA + ":recipient/name";

    String RECIPIENT_EMAIL_PROP = SCHEMA + ":recipient/email";

    String RECIPIENT_ADDRESS_PROP = SCHEMA + ":recipient/address";

    String RECIPIENT_FAX_PROP = SCHEMA + ":recipient/fax";

    String RECIPIENT_PHONE_PROP = SCHEMA + ":recipient/phone";

    String RECIPIENT_POST_CODE_PROP = SCHEMA + ":recipient/postCode";

    String SCHEME_ID_PROP = SCHEMA + ":schemeID";

    String FUND_INFO_NAME_PROP = SCHEMA + ":fundInformation/fundName";

    String FUND_INFO_DISTRIBUTION_DATE = SCHEMA + ":fundInformation/distributionDate";

    String EXTERNAL_SOURCE_SYSTEM_PROP = SCHEMA + ":externalData/sourceSystem";

    String EXTERNAL_CREATE_DATE_PROP = SCHEMA + ":externalData/createdDate";
}
