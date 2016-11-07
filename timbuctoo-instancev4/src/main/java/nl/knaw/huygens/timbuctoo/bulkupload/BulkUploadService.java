package nl.knaw.huygens.timbuctoo.bulkupload;

import nl.knaw.huygens.timbuctoo.bulkupload.loaders.BulkLoader;
import nl.knaw.huygens.timbuctoo.bulkupload.loaders.dataperfect.DataPerfectLoader;
import nl.knaw.huygens.timbuctoo.bulkupload.loaders.excel.allsheetloader.AllSheetLoader;
import nl.knaw.huygens.timbuctoo.bulkupload.parsingstatemachine.Importer;
import nl.knaw.huygens.timbuctoo.bulkupload.savers.TinkerpopSaver;
import nl.knaw.huygens.timbuctoo.model.vre.Vres;
import nl.knaw.huygens.timbuctoo.server.TinkerpopGraphManager;

import java.io.IOException;
import java.util.function.Consumer;

public class BulkUploadService {

  private final Vres vres;
  private final TinkerpopGraphManager graphwrapper;

  public BulkUploadService(Vres vres, TinkerpopGraphManager graphwrapper/*, Authorizer authorizer*/) {
    this.vres = vres;
    this.graphwrapper = graphwrapper;
  }

  public void saveToDb(String vreName, byte[] file, String fileName, Consumer<String> statusUpdate)
    throws InvalidFileException, IOException {

    try (TinkerpopSaver saver = new TinkerpopSaver(vres, graphwrapper, vreName, 50_000)) {
      BulkLoader loader;
      if (fileName.endsWith(".xlsx")) {
        loader = new AllSheetLoader();
      } else {
        loader = new DataPerfectLoader();
      }
      loader.loadData(file, new Importer(saver), statusUpdate);
    }
  }

}
