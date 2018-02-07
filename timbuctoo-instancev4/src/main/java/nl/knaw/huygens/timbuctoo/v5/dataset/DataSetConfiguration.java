package nl.knaw.huygens.timbuctoo.v5.dataset;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import nl.knaw.huygens.timbuctoo.v5.berkeleydb.BdbPersistentEnvironmentCreator;
import nl.knaw.huygens.timbuctoo.v5.filestorage.FileStorageFactory;
import nl.knaw.huygens.timbuctoo.v5.rdfio.RdfIoFactory;
import nl.knaw.huygens.timbuctoo.v5.security.PermissionFetcher;
import nl.knaw.huygens.timbuctoo.v5.util.TimbuctooRdfIdHelper;
import org.immutables.value.Value;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;

@Value.Immutable
public interface DataSetConfiguration {

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

  String getDataSetMetadataLocation();

  FileStorageFactory getFileStorage();

  RdfIoFactory getRdfIo();

  default DataSetRepository createRepository(ExecutorService executorService, PermissionFetcher permissionFetcher,
                                             BdbPersistentEnvironmentCreator databases,
                                             TimbuctooRdfIdHelper rdfIdHelper,
                                             Consumer<String> onUpdated, boolean publicByDefault) throws IOException {
    return new DataSetRepository(
      executorService,
      permissionFetcher,
      this,
      databases,
      rdfIdHelper,
      onUpdated,
      publicByDefault
    );
  }
}
