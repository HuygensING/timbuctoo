package nl.knaw.huygens.timbuctoo.v5.bulkupload;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Lists;
import nl.knaw.huygens.timbuctoo.bulkupload.loaders.Loader;
import nl.knaw.huygens.timbuctoo.bulkupload.parsingstatemachine.Importer;
import nl.knaw.huygens.timbuctoo.bulkupload.parsingstatemachine.ResultReporter;
import nl.knaw.huygens.timbuctoo.bulkupload.parsingstatemachine.StateMachine;
import nl.knaw.huygens.timbuctoo.v5.dataset.ImportManager;
import nl.knaw.huygens.timbuctoo.v5.dataset.PlainRdfCreator;
import nl.knaw.huygens.timbuctoo.v5.dataset.dto.DataSet;
import nl.knaw.huygens.timbuctoo.v5.dataset.dto.DataSetMetaData;
import nl.knaw.huygens.timbuctoo.v5.filestorage.dto.CachedFile;
import nl.knaw.huygens.timbuctoo.v5.filestorage.exceptions.LogStorageFailedException;
import nl.knaw.huygens.timbuctoo.v5.rdfio.RdfSerializer;

import java.util.function.Consumer;

import static nl.knaw.huygens.timbuctoo.util.Tuple.tuple;

public class TabularRdfCreator implements PlainRdfCreator {
  @JsonProperty("loader")
  private final Loader loader;
  @JsonIgnore
  private final Consumer<String> importStatusConsumer; // TODO hoe gaan we deze reconstrueren na deserialisatie
  @JsonProperty("fileToken")
  private final String fileToken;

  public TabularRdfCreator(Loader loader, Consumer<String> importStatusConsumer, String fileToken) {
    this.loader = loader;
    this.importStatusConsumer = importStatusConsumer;
    this.fileToken = fileToken;
  }

  @JsonCreator
  public TabularRdfCreator(@JsonProperty("loader") Loader loader, @JsonProperty("fileToken") String fileToken) {
    this(loader, s -> {
    }, fileToken);
  }


  @Override
  public void sendQuads(RdfSerializer saver, DataSet dataSet) throws LogStorageFailedException {

    try (CachedFile file = dataSet.getImportManager().getFile(fileToken)) {
      loader.loadData(Lists.newArrayList(tuple(file.getName(), file.getFile())),
        new Importer(
          new StateMachine<>(
            new RawUploadRdfSaver(dataSet.getMetadata(), file.getFile().getName(), file.getMimeType(), saver)
          ),
          new ResultReporter(importStatusConsumer)
        )
      );
    } catch (Exception e) {
      throw new LogStorageFailedException(e);
    }
  }
}
