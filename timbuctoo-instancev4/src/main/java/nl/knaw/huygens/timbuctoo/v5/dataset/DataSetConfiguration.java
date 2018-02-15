package nl.knaw.huygens.timbuctoo.v5.dataset;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import nl.knaw.huygens.timbuctoo.v5.berkeleydb.BdbPersistentEnvironmentCreator;
import nl.knaw.huygens.timbuctoo.v5.datastorage.DataStorage;
import nl.knaw.huygens.timbuctoo.v5.security.PermissionFetcher;
import nl.knaw.huygens.timbuctoo.v5.util.TimbuctooRdfIdHelper;
import org.immutables.value.Value;

import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;

@Value.Immutable
public interface DataSetConfiguration {

  @JsonCreator
  static DataSetConfiguration create(@JsonProperty("dataSetMetadataLocation") String dataSetMetadataLocation,
                                     @JsonProperty("dataStorage") DataStorage dataStorage) {
    return ImmutableDataSetConfiguration.builder()
                                        .dataSetMetadataLocation(dataSetMetadataLocation)
                                        .dataStorage(dataStorage)
                                        .build();
  }

  String getDataSetMetadataLocation();

  DataStorage getDataStorage();

  default DataSetRepository createRepository(ExecutorService executorService, PermissionFetcher permissionFetcher,
                                             BdbPersistentEnvironmentCreator databases,
                                             TimbuctooRdfIdHelper rdfIdHelper,
                                             Consumer<String> onUpdated, boolean publicByDefault) {
    return new DataSetRepository(
      executorService,
      permissionFetcher,
      this,
      databases,
      rdfIdHelper,
      onUpdated,
      publicByDefault,
      getDataStorage()
    );
  }
}
