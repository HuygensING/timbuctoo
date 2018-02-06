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
  @JsonProperty("fileName")
  private final String fileName;

  public TabularRdfCreator(Loader loader, Consumer<String> importStatusConsumer, String fileToken, String fileName) {
    this.loader = loader;
    this.importStatusConsumer = importStatusConsumer;
    this.fileToken = fileToken;
    this.fileName = fileName;
  }

  @JsonCreator
  public TabularRdfCreator(@JsonProperty("loader") Loader loader, @JsonProperty("fileToken") String fileToken,
                           @JsonProperty("fileName") String fileName) {
    this(loader, s -> {
    }, fileToken, fileName);
  }


  @Override
  public void sendQuads(RdfSerializer saver, DataSet dataSet) throws LogStorageFailedException {

    try (CachedFile file = dataSet.getImportManager().getFile(fileToken)) {
      final RawUploadRdfSaver rawUploadRdfSaver = new RawUploadRdfSaver(
        dataSet.getMetadata(),
        file.getFile().getName(),
        file.getMimeType(),
        saver,
        fileName
      );

      loader.loadData(
        Lists.newArrayList(tuple(fileName, file.getFile())),
        new Importer(new StateMachine<>(rawUploadRdfSaver), new ResultReporter(importStatusConsumer))
      );
    } catch (Exception e) {
      throw new LogStorageFailedException(e);
    }
  }
}
