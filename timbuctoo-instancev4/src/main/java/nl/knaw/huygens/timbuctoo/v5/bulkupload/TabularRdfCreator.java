package nl.knaw.huygens.timbuctoo.v5.bulkupload;

import com.google.common.collect.Lists;
import nl.knaw.huygens.timbuctoo.bulkupload.loaders.Loader;
import nl.knaw.huygens.timbuctoo.bulkupload.parsingstatemachine.Importer;
import nl.knaw.huygens.timbuctoo.bulkupload.parsingstatemachine.ResultReporter;
import nl.knaw.huygens.timbuctoo.bulkupload.parsingstatemachine.StateMachine;
import nl.knaw.huygens.timbuctoo.v5.dataset.ImportManager;
import nl.knaw.huygens.timbuctoo.v5.dataset.PlainRdfCreator;
import nl.knaw.huygens.timbuctoo.v5.dataset.dto.PromotedDataSet;
import nl.knaw.huygens.timbuctoo.v5.filestorage.dto.CachedFile;
import nl.knaw.huygens.timbuctoo.v5.filestorage.exceptions.LogStorageFailedException;
import nl.knaw.huygens.timbuctoo.v5.rdfio.RdfSerializer;

import java.util.function.Consumer;

import static nl.knaw.huygens.timbuctoo.util.Tuple.tuple;

public class TabularRdfCreator implements PlainRdfCreator {
  private final ImportManager importManager;
  private final Loader loader;
  private final PromotedDataSet dataSet;
  private final Consumer<String> importStatusConsumer; // TODO hoe gaan we deze reconstrueren na deserialisatie
  private final String fileToken;

  public TabularRdfCreator(ImportManager importManager, Loader loader, PromotedDataSet dataSet,
                           Consumer<String> importStatusConsumer, String fileToken) {
    this.importManager = importManager;
    this.loader = loader;
    this.dataSet = dataSet;
    this.importStatusConsumer = importStatusConsumer;
    this.fileToken = fileToken;
  }

  @Override
  public void sendQuads(RdfSerializer saver) throws LogStorageFailedException {

    try (CachedFile file = importManager.getFile(fileToken)) {
      loader.loadData(Lists.newArrayList(tuple(file.getName(), file.getFile())),
        new Importer(
          new StateMachine<>(
            new RawUploadRdfSaver(dataSet, file.getFile().getName(), file.getMimeType(), saver)
          ),
          new ResultReporter(importStatusConsumer)
        )
      );
      saver.close();
    } catch (Exception e) {
      throw new LogStorageFailedException(e);
    }
  }
}
