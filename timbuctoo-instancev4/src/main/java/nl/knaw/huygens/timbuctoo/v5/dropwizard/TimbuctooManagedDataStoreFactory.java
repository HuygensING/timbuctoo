package nl.knaw.huygens.timbuctoo.v5.dropwizard;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import io.dropwizard.lifecycle.Managed;
import nl.knaw.huygens.timbuctoo.v5.datastores.DataStoreDataFetcherFactory;
import nl.knaw.huygens.timbuctoo.v5.datastores.DataStoreFactory;
import nl.knaw.huygens.timbuctoo.v5.datastores.dto.DataStores;
import nl.knaw.huygens.timbuctoo.v5.datastores.exceptions.DataStoreCreationException;
import nl.knaw.huygens.timbuctoo.v5.datastores.implementations.berkeleydb.BdbCollectionIndex;
import nl.knaw.huygens.timbuctoo.v5.datastores.implementations.berkeleydb.BdbTripleStore;
import nl.knaw.huygens.timbuctoo.v5.datastores.implementations.json.HardCodedTypeNameStore;
import nl.knaw.huygens.timbuctoo.v5.datastores.implementations.json.JsonSchemaStore;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Current datastores are:
 *  - local (no network overhead, no network failures)
 *  - cursor based (cheap iteration)
 *  - sharded per dataset (you can't scale one dataset horizontally, but you can scale across datasets)
 */
public class TimbuctooManagedDataStoreFactory implements Managed, DataStoreFactory {
  private final String databaseLocation;
  Map<String, DataStores> dataStoresMap = new HashMap<>();
  protected final EnvironmentConfig configuration;
  protected File dbHome;

  @JsonCreator
  public TimbuctooManagedDataStoreFactory(@JsonProperty("databaseLocation") String databaseLocation) {
    this.databaseLocation = databaseLocation;
    configuration = new EnvironmentConfig(new Properties());
    configuration.setTransactional(true);
    configuration.setTxnNoSync(true);
    configuration.setAllowCreate(true);
    configuration.setSharedCache(true);
  }

  @Override
  public DataStores getDataStores(String userId, String dataSetId) throws DataStoreCreationException {
    String dataSetName = userId + "_" + dataSetId;
    if (dataStoresMap.containsKey(dataSetName)) {
      return dataStoresMap.get(dataSetName);
    } else {
      try {
        DataStores result = this.makeDataStores(dataSetName);
        dataStoresMap.put(dataSetName, result);
        return result;
      } catch (DatabaseException | IOException e) {
        throw new DataStoreCreationException(e);
      }
    }
  }

  private DataStores makeDataStores(String dataSetName) throws DatabaseException, IOException {
    Environment dataSetEnvironment = new Environment(new File(dbHome, dataSetName), configuration);
    final BdbCollectionIndex collectionIndex = new BdbCollectionIndex(dataSetName, dataSetEnvironment);
    final HardCodedTypeNameStore prefixStore = new HardCodedTypeNameStore(dataSetName);
    final BdbTripleStore tripleStore = new BdbTripleStore(dataSetName, dataSetEnvironment);
    final JsonSchemaStore schemaStore = new JsonSchemaStore(tripleStore, dataSetEnvironment.getHome());
    final DataStoreDataFetcherFactory fetchers = new DataStoreDataFetcherFactory(tripleStore, collectionIndex);
    return new DataStores(collectionIndex, prefixStore, tripleStore, schemaStore, fetchers, fetchers);
  }

  @Override
  public void start() throws Exception {
    dbHome = new File(databaseLocation);
    dbHome.mkdirs();
    if (!dbHome.isDirectory()) {
      throw new IllegalStateException("Database home at '" + dbHome.getAbsolutePath() + "' is not a directory");
    }
  }

  @Override
  public void stop() throws Exception {
    for (DataStores dataStores : dataStoresMap.values()) {
      dataStores.close();
    }
  }
}
