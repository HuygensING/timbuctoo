package nl.knaw.huygens.timbuctoo.v5.dataset;

import com.google.common.collect.Lists;
import nl.knaw.huygens.timbuctoo.bulkupload.loaders.Loader;
import nl.knaw.huygens.timbuctoo.bulkupload.parsingstatemachine.Importer;
import nl.knaw.huygens.timbuctoo.bulkupload.parsingstatemachine.ResultReporter;
import nl.knaw.huygens.timbuctoo.bulkupload.parsingstatemachine.StateMachine;
import nl.knaw.huygens.timbuctoo.bulkupload.savers.RawUploadRdfSaver;
import nl.knaw.huygens.timbuctoo.v5.filestorage.dto.CachedFile;
import nl.knaw.huygens.timbuctoo.v5.filestorage.exceptions.LogStorageFailedException;
import nl.knaw.huygens.timbuctoo.v5.rdfio.RdfSerializer;
import nl.knaw.huygens.timbuctoo.v5.util.TimbuctooRdfIdHelper;

import java.util.function.Consumer;

import static nl.knaw.huygens.timbuctoo.util.Tuple.tuple;

public class TabularRdfCreator implements RdfCreator {
  private final ImportManager importManager;
  private final Loader loader;
  private final String ownerId;
  private final String dataSetId;
  private final Consumer<String> importStatusConsumer; // TODO hoe gaan we deze reconstrueren na deserialisatie
  private final String fileToken;
  private final TimbuctooRdfIdHelper rdfIdHelper;

  public TabularRdfCreator(ImportManager importManager, Loader loader, String ownerId, String dataSetId,
                           Consumer<String> importStatusConsumer, String fileToken, TimbuctooRdfIdHelper rdfIdHelper) {
    this.importManager = importManager;
    this.loader = loader;
    this.ownerId = ownerId;
    this.dataSetId = dataSetId;
    this.importStatusConsumer = importStatusConsumer;
    this.fileToken = fileToken;
    this.rdfIdHelper = rdfIdHelper;
  }

  @Override
  public void sendQuads(RdfSerializer saver) throws LogStorageFailedException {

    try (CachedFile file = importManager.getFile(fileToken)) {
      loader.loadData(Lists.newArrayList(tuple(file.getName(), file.getFile())),
        new Importer(
          new StateMachine<>(new RawUploadRdfSaver(ownerId, dataSetId, file.getName(), file.getMimeType(), saver,
            rdfIdHelper)),
          new ResultReporter(importStatusConsumer)
        )
      );
    } catch (Exception e) {
      throw new LogStorageFailedException(e);
    }
  }
}
