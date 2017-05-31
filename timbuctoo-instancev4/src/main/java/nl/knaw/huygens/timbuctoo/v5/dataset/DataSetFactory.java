package nl.knaw.huygens.timbuctoo.v5.dataset;

import com.fasterxml.jackson.core.type.TypeReference;
import nl.knaw.huygens.timbuctoo.security.VreAuthorizationCrud;
import nl.knaw.huygens.timbuctoo.security.exceptions.AuthorizationCreationException;
import nl.knaw.huygens.timbuctoo.v5.bdbdatafetchers.DataFetcherFactoryFactory;
import nl.knaw.huygens.timbuctoo.v5.bdbdatafetchers.DataStoreDataFetcherFactory;
import nl.knaw.huygens.timbuctoo.v5.datastores.exceptions.DataStoreCreationException;
import nl.knaw.huygens.timbuctoo.v5.datastores.implementations.json.HardCodedTypeNameStore;
import nl.knaw.huygens.timbuctoo.v5.datastores.implementations.json.JsonSchemaStore;
import nl.knaw.huygens.timbuctoo.v5.datastores.jsonfilebackeddata.JsonFileBackedData;
import nl.knaw.huygens.timbuctoo.v5.datastores.prefixstore.TypeNameStore;
import nl.knaw.huygens.timbuctoo.v5.datastores.prefixstore.TypeNameStoreFactory;
import nl.knaw.huygens.timbuctoo.v5.datastores.schema.SchemaStore;
import nl.knaw.huygens.timbuctoo.v5.datastores.schema.SchemaStoreFactory;
import nl.knaw.huygens.timbuctoo.v5.dropwizard.BdbDatabaseCreator;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.DataFetcherFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

/**
 * - stores all configuration parameters so it can inject them in the dataset constructor
 * - makes DataSets a singleton
 * - keeps track of all created dataSets across restarts (stores them in a file)
 */
public class DataSetFactory implements DataFetcherFactoryFactory, SchemaStoreFactory, TypeNameStoreFactory {

  private final ExecutorService executorService;
  private final VreAuthorizationCrud vreAuthorizationCrud;
  private final DataSetConfiguration configuration;
  private final BdbDatabaseCreator dbFactory;
  private final Map<String, Map<String, DataStores>> dataSetMap;
  private final JsonFileBackedData<Map<String, List<String>>> storedDataSets;

  public DataSetFactory(ExecutorService executorService, VreAuthorizationCrud vreAuthorizationCrud,
                        DataSetConfiguration configuration, BdbDatabaseCreator dbFactory) throws IOException {
    this.executorService = executorService;
    this.vreAuthorizationCrud = vreAuthorizationCrud;
    this.configuration = configuration;
    this.dbFactory = dbFactory;
    dataSetMap = new HashMap<>();
    new File(configuration.getDataSetMetadataLocation()).mkdirs();
    storedDataSets = JsonFileBackedData.getOrCreate(
      new File(configuration.getDataSetMetadataLocation(), "dataSets.json"),
      new HashMap<>(),
      new TypeReference<Map<String, List<String>>>() {}
    );
  }

  @Override
  public DataFetcherFactory createDataFetcherFactory(String userId, String dataSetId)
    throws DataStoreCreationException {
    return make(userId, dataSetId).dataFetcherFactory;
  }

  @Override
  public SchemaStore createSchemaStore(String userId, String dataSetId) throws DataStoreCreationException {
    return make(userId, dataSetId).schemaStore;
  }

  @Override
  public TypeNameStore createTypeNameStore(String userId, String dataSetId) throws DataStoreCreationException {
    return make(userId, dataSetId).typeNameStore;
  }

  public DataSet createDataSet(String userId, String dataSetId) throws DataStoreCreationException {
    return make(userId, dataSetId).dataSet;

  }

  private DataStores make(String userId, String dataSetId) throws DataStoreCreationException {
    String authorizationKey = userId + "_" + dataSetId;
    synchronized (dataSetMap) {
      Map<String, DataStores> userDataSets = dataSetMap.computeIfAbsent(userId, key -> new HashMap<>());
      if (!userDataSets.containsKey(dataSetId)) {
        try {
          File metaDataLocation = new File(configuration.getDataSetMetadataLocation());
          vreAuthorizationCrud.createAuthorization(authorizationKey, userId, "ADMIN");
          DataSet dataSet = new DataSet(
            new File(metaDataLocation, userId + "_" + dataSetId + "-log.json"),
            configuration.getFileStorage().makeFileStorage(userId, dataSetId),
            configuration.getFileStorage().makeFileStorage(userId, dataSetId),
            configuration.getFileStorage().makeLogStorage(userId, dataSetId),
            executorService,
            configuration.getRdfIo()
          );

          DataStores result = new DataStores();
          result.dataFetcherFactory = new DataStoreDataFetcherFactory(
            userId,
            dataSetId,
            dataSet,
            dbFactory
          );
          result.typeNameStore = new HardCodedTypeNameStore(userId + "_" + dataSetId);
          result.schemaStore = new JsonSchemaStore(metaDataLocation, userId, dataSetId, dataSet);
          result.dataSet = dataSet;
          userDataSets.put(dataSetId, result);
          storedDataSets.updateData(dataSets -> {
            dataSets.computeIfAbsent(userId, key -> new ArrayList<>()).add(dataSetId);
            return dataSets;
          });
        } catch (AuthorizationCreationException | IOException e) {
          throw new DataStoreCreationException(e);
        }
      }
      return userDataSets.get(dataSetId);
    }
  }


  private class DataStores {
    public DataFetcherFactory dataFetcherFactory;
    public SchemaStore schemaStore;
    public TypeNameStore typeNameStore;
    public DataSet dataSet;
  }
}
