package nl.knaw.huygens.timbuctoo.experimental.bulkupload;

import nl.knaw.huygens.timbuctoo.experimental.bulkupload.loaders.excel.XlsxLoader;
import nl.knaw.huygens.timbuctoo.experimental.bulkupload.loaders.excel.allsheetloader.AllSheetLoader;
import nl.knaw.huygens.timbuctoo.experimental.bulkupload.parsingstatemachine.Importer;
import nl.knaw.huygens.timbuctoo.experimental.bulkupload.savers.TinkerpopSaver;
import nl.knaw.huygens.timbuctoo.security.AuthorizationException;
import nl.knaw.huygens.timbuctoo.security.AuthorizationUnavailableException;
import nl.knaw.huygens.timbuctoo.security.Authorizer;
import nl.knaw.huygens.timbuctoo.server.TinkerpopGraphManager;

import java.io.InputStream;

public class BulkUploadService {

  private final TinkerpopGraphManager graphwrapper;
  private final Authorizer authorizer;

  public BulkUploadService(TinkerpopGraphManager graphwrapper/*, Authorizer authorizer*/) {
    this.graphwrapper = graphwrapper;
    this.authorizer = null;//authorizer;
  }

  //FIXME: add authorizer on admin
  public String saveToDb(String vreName, InputStream wb/*, String userId*/)
    throws AuthorizationUnavailableException, AuthorizationException, InvalidExcelFileException {
    //
    //for (Collection collection : vre.getCollections().values()) {
    //  if (!authorizer.authorizationFor(collection, userId).isAllowedToWrite()) {
    //    throw new AuthorizationException(
    //      "You cannot use bulkupload because you are not allowed to edit " + collection.getCollectionName()
    //    );
    //  }
    //}

    try (TinkerpopSaver saver = new TinkerpopSaver(graphwrapper, vreName, 50_000)) {
      XlsxLoader loader = new AllSheetLoader();
      return loader.loadData(wb, new Importer(saver));
    }
  }

}
