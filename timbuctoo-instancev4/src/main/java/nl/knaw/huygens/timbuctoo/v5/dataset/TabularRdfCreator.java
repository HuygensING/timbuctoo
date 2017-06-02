package nl.knaw.huygens.timbuctoo.v5.dataset;

import com.google.common.collect.Lists;
import nl.knaw.huygens.timbuctoo.bulkupload.loaders.Loader;
import nl.knaw.huygens.timbuctoo.bulkupload.parsingstatemachine.Importer;
import nl.knaw.huygens.timbuctoo.bulkupload.parsingstatemachine.ResultReporter;
import nl.knaw.huygens.timbuctoo.bulkupload.parsingstatemachine.StateMachine;
import nl.knaw.huygens.timbuctoo.bulkupload.savers.RdfSaver;
import nl.knaw.huygens.timbuctoo.v5.filestorage.dto.CachedFile;
import nl.knaw.huygens.timbuctoo.v5.filestorage.exceptions.LogStorageFailedException;
import nl.knaw.huygens.timbuctoo.v5.rdfio.RdfSerializer;

import java.util.function.Consumer;

import static nl.knaw.huygens.timbuctoo.util.Tuple.tuple;

public class TabularRdfCreator implements RdfCreator {
  private final DataSet dataSet;
  private final Loader loader;
  private final String dataSetId;
  private final Consumer<String> importStatusConsumer; // TODO hoe gaan we deze reconstrueren na deserialisatie
  private final String fileToken;

  public TabularRdfCreator(DataSet dataSet, Loader loader, String dataSetId, Consumer<String> importStatusConsumer,
                           String fileToken) {
    this.dataSet = dataSet;
    this.loader = loader;
    this.dataSetId = dataSetId;
    this.importStatusConsumer = importStatusConsumer;
    this.fileToken = fileToken;
  }

  @Override
  public void sendQuads(RdfSerializer saver) throws LogStorageFailedException {

    try (CachedFile file = dataSet.getFile(fileToken)) {
      loader.loadData(Lists.newArrayList(tuple(file.getName(), file.getFile())),
        new Importer(
          new StateMachine<>(new RdfSaver(dataSetId, file.getName(), saver)),
          new ResultReporter(importStatusConsumer)
        )
      );
    } catch (Exception e) {
      throw new LogStorageFailedException(e);
    }
  }
}
