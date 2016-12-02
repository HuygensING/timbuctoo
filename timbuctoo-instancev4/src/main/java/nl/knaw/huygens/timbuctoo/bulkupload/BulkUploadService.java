package nl.knaw.huygens.timbuctoo.bulkupload;

import nl.knaw.huygens.timbuctoo.bulkupload.loaders.Loader;
import nl.knaw.huygens.timbuctoo.bulkupload.loaders.dataperfect.DataPerfectLoader;
import nl.knaw.huygens.timbuctoo.bulkupload.loaders.excel.allsheetloader.AllSheetLoader;
import nl.knaw.huygens.timbuctoo.bulkupload.parsingstatemachine.Importer;
import nl.knaw.huygens.timbuctoo.bulkupload.parsingstatemachine.ResultReporter;
import nl.knaw.huygens.timbuctoo.bulkupload.parsingstatemachine.StateMachine;
import nl.knaw.huygens.timbuctoo.bulkupload.savers.TinkerpopSaver;
import nl.knaw.huygens.timbuctoo.model.vre.Vres;
import nl.knaw.huygens.timbuctoo.server.TinkerPopGraphManager;

import java.io.IOException;
import java.util.function.Consumer;

public class BulkUploadService {

  private final Vres vres;
  private final TinkerPopGraphManager graphwrapper;

  public BulkUploadService(Vres vres, TinkerPopGraphManager graphwrapper/*, Authorizer authorizer*/) {
    this.vres = vres;
    this.graphwrapper = graphwrapper;
  }

  public void saveToDb(String vreName, byte[] file, String fileName, Consumer<String> statusUpdate)
    throws InvalidFileException, IOException {

    try (TinkerpopSaver saver = new TinkerpopSaver(vres, graphwrapper, vreName, 50_000)) {
      Loader loader;
      if (fileName.endsWith(".xlsx")) {
        loader = new AllSheetLoader();
      } else {
        loader = new DataPerfectLoader();
      }
      loader.loadData(file, new Importer(new StateMachine(saver), new ResultReporter(statusUpdate)));
    }
  }

}
