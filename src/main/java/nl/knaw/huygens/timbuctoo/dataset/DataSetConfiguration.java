package nl.knaw.huygens.timbuctoo.dataset;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import nl.knaw.huygens.timbuctoo.berkeleydb.BdbPersistentEnvironmentCreator;
import nl.knaw.huygens.timbuctoo.dataset.dto.Metadata;
import nl.knaw.huygens.timbuctoo.datastorage.DataStorage;
import nl.knaw.huygens.timbuctoo.security.PermissionFetcher;
import nl.knaw.huygens.timbuctoo.util.TimbuctooRdfIdHelper;
import org.immutables.value.Value;

import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;

@Value.Immutable
public interface DataSetConfiguration {
  @JsonCreator
  static DataSetConfiguration create(@JsonProperty("dataStorage") DataStorage dataStorage) {
    return ImmutableDataSetConfiguration.builder()
                                        .dataStorage(dataStorage)
                                        .build();
  }

  DataStorage getDataStorage();

  default DataSetRepository createRepository(ExecutorService executorService, PermissionFetcher permissionFetcher,
                                             BdbPersistentEnvironmentCreator databases,
                                             Metadata metadata,
                                             TimbuctooRdfIdHelper rdfIdHelper,
                                             Consumer<String> onUpdated, boolean publicByDefault) {
    return new DataSetRepository(
      executorService,
      permissionFetcher,
      databases,
      metadata,
      rdfIdHelper,
      onUpdated,
      publicByDefault,
      getDataStorage()
    );
  }
}
