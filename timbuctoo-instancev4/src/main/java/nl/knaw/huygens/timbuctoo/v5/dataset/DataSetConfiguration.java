package nl.knaw.huygens.timbuctoo.v5.dataset;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import nl.knaw.huygens.timbuctoo.v5.filestorage.FileStorageFactory;
import nl.knaw.huygens.timbuctoo.v5.rdfio.RdfIoFactory;
import org.immutables.value.Value;

@Value.Immutable
public interface DataSetConfiguration {

  String getDataSetMetadataLocation();

  FileStorageFactory getFileStorage();

  RdfIoFactory getRdfIo();

  @JsonCreator
  static DataSetConfiguration create(@JsonProperty("dataSetMetadataLocation") String dataSetMetadataLocation,
                                     @JsonProperty("fileStorage") FileStorageFactory fileStorageFactory,
                                     @JsonProperty("rdfIo") RdfIoFactory rdfIoFactory) {
    return ImmutableDataSetConfiguration.builder()
      .dataSetMetadataLocation(dataSetMetadataLocation)
      .fileStorage(fileStorageFactory)
      .rdfIo(rdfIoFactory)
      .build();
  }
}
