package nl.knaw.huygens.timbuctoo.bulkupload;

import nl.knaw.huygens.timbuctoo.bulkupload.loaders.excel.XlsxLoader;
import nl.knaw.huygens.timbuctoo.bulkupload.loaders.excel.allsheetloader.AllSheetLoader;
import nl.knaw.huygens.timbuctoo.bulkupload.parsingstatemachine.Importer;
import nl.knaw.huygens.timbuctoo.bulkupload.savers.TinkerpopSaver;
import nl.knaw.huygens.timbuctoo.model.vre.Vres;
import nl.knaw.huygens.timbuctoo.security.AuthorizationException;
import nl.knaw.huygens.timbuctoo.security.AuthorizationUnavailableException;
import nl.knaw.huygens.timbuctoo.security.Authorizer;
import nl.knaw.huygens.timbuctoo.server.TinkerpopGraphManager;

import java.io.InputStream;

public class BulkUploadService {

  private final Vres vres;
  private final TinkerpopGraphManager graphwrapper;
  private final Authorizer authorizer;

  public BulkUploadService(Vres vres, TinkerpopGraphManager graphwrapper/*, Authorizer authorizer*/) {
    this.vres = vres;
    this.graphwrapper = graphwrapper;
    this.authorizer = null;//authorizer;
  }

  public String saveToDb(String vreName, InputStream wb)
    throws AuthorizationUnavailableException, AuthorizationException, InvalidExcelFileException {

    try (TinkerpopSaver saver = new TinkerpopSaver(vres, graphwrapper, vreName, 50_000)) {
      XlsxLoader loader = new AllSheetLoader();
      return loader.loadData(wb, new Importer(saver));
    }
  }

}
