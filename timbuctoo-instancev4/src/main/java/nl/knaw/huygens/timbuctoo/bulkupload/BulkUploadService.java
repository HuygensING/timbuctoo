package nl.knaw.huygens.timbuctoo.bulkupload;

import nl.knaw.huygens.timbuctoo.bulkupload.loaders.Loader;
import nl.knaw.huygens.timbuctoo.bulkupload.parsingstatemachine.Importer;
import nl.knaw.huygens.timbuctoo.bulkupload.parsingstatemachine.ResultReporter;
import nl.knaw.huygens.timbuctoo.bulkupload.parsingstatemachine.StateMachine;
import nl.knaw.huygens.timbuctoo.bulkupload.savers.RdfSaver;
import nl.knaw.huygens.timbuctoo.model.vre.Vres;
import nl.knaw.huygens.timbuctoo.server.TinkerPopGraphManager;
import nl.knaw.huygens.timbuctoo.util.Tuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;

import static java.util.stream.Collectors.joining;

public class BulkUploadService {
  public static final Logger LOG = LoggerFactory.getLogger(BulkUploadService.class);
  private final Vres vres;
  private final TinkerPopGraphManager graphwrapper;
  private final int maxVertices;

  public BulkUploadService(Vres vres, TinkerPopGraphManager graphwrapper, int maxVerticesPerTransaction) {
    this.vres = vres;
    this.graphwrapper = graphwrapper;
    this.maxVertices = maxVerticesPerTransaction;
  }

  public void saveToDb(Loader loader, List<Tuple<String, File>> tempFiles,
                       Consumer<String> statusUpdate, RdfSaver rdfSaver) throws IOException, InvalidFileException {

    String fileNamesDisplay;
    if (tempFiles.size() == 1) {
      fileNamesDisplay = tempFiles.get(0).getLeft();
    } else {
      fileNamesDisplay = "multiple files: " + tempFiles.stream().map(Tuple::getLeft).collect(joining(", "));
    }

    try {
      loader.loadData(tempFiles, new Importer(new StateMachine<>(rdfSaver), new ResultReporter(statusUpdate)));
    } catch (IOException | InvalidFileException e) {
      throw e;
    }
  }
}
