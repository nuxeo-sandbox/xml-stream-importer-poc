package org.nuxeo.dst.importer;

import java.io.File;
import java.io.IOException;

import org.nuxeo.dst.importer.service.XMLImporterService;
import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.xml.sax.SAXException;

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

    @Param(name = "xml")
    protected String xml;

    @Param(name = "manco")
    protected String manco;

    @OperationMethod
    public DocumentModel run() throws IllegalAccessException, SAXException, IOException {
        importerService.doImport(new File(xml));

        return null;
    }
}
