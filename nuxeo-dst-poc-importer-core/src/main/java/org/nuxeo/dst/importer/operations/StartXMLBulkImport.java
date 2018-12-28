package org.nuxeo.dst.importer.operations;

import javax.xml.bind.JAXBException;

import org.nuxeo.dst.importer.service.XMLImporterService;
import org.nuxeo.dst.importer.work.XMLImportWork;
import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.work.api.WorkManager;
import org.nuxeo.runtime.api.Framework;

/**
 *
 */
@Operation(id=StartXMLBulkImport.ID, category=Constants.CAT_DOCUMENT, label="Start XML Bulk import", description="Describe here what your operation does.")
public class StartXMLBulkImport {

    public static final String ID = "Document.StartXMLBulkImport";

    @Context
    protected CoreSession session;

    @Context
    protected XMLImporterService importerService;

    // TODO: change to `location`
    @Param(name = "xml")
    protected String xml;

    @Param(name = "manco")
    protected String manco;

//    @Param(name = "uuid")
//    protected String uuid;

    @OperationMethod
    public DocumentModel run() throws JAXBException {
        // TODO: create uuid to track import
        WorkManager wm = Framework.getService(WorkManager.class);
        XMLImportWork work = new XMLImportWork(session.getRepositoryName(), session.getPrincipal().getName(), manco, xml);
        wm.schedule(work);

        return null;
    }
}
