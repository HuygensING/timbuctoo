package nl.knaw.huygens.timbuctoo.v5.dataset;

import com.fasterxml.jackson.core.type.TypeReference;
import nl.knaw.huygens.timbuctoo.security.VreAuthorizationCrud;
import nl.knaw.huygens.timbuctoo.security.exceptions.AuthorizationCreationException;
import nl.knaw.huygens.timbuctoo.util.Tuple;
import nl.knaw.huygens.timbuctoo.v5.bdb.BdbDatabaseCreator;
import nl.knaw.huygens.timbuctoo.v5.bdbdatafetchers.DataFetcherFactoryFactory;
import nl.knaw.huygens.timbuctoo.v5.bdbdatafetchers.DataStoreDataFetcherFactory;
import nl.knaw.huygens.timbuctoo.v5.datastores.exceptions.DataStoreCreationException;
import nl.knaw.huygens.timbuctoo.v5.datastores.implementations.json.JsonSchemaStore;
import nl.knaw.huygens.timbuctoo.v5.datastores.implementations.json.JsonTypeNameStore;
import nl.knaw.huygens.timbuctoo.v5.datastores.jsonfilebackeddata.JsonFileBackedData;
import nl.knaw.huygens.timbuctoo.v5.datastores.prefixstore.TypeNameStore;
import nl.knaw.huygens.timbuctoo.v5.datastores.prefixstore.TypeNameStoreFactory;
import nl.knaw.huygens.timbuctoo.v5.datastores.schema.SchemaStore;
import nl.knaw.huygens.timbuctoo.v5.datastores.schema.SchemaStoreFactory;
import nl.knaw.huygens.timbuctoo.v5.filestorage.implementations.filesystem.DataSetPathHelper;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.DataFetcherFactory;
import nl.knaw.huygens.timbuctoo.v5.rml.DataSourceStore;
import nl.knaw.huygens.timbuctoo.v5.rml.RdfDataSourceFactory;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * - stores all configuration parameters so it can inject them in the dataset constructor
 * - makes CreateDataSet a singleton
 * - keeps track of all created dataSets across restarts (stores them in a file)
 */
public class DataSetFactory implements DataFetcherFactoryFactory, SchemaStoreFactory, TypeNameStoreFactory {

  private final ExecutorService executorService;
  private final VreAuthorizationCrud vreAuthorizationCrud;
  private final DataSetConfiguration configuration;
  private final BdbDatabaseCreator dbFactory;
  private final Map<String, Map<String, DataSet>> dataSetMap;
  private final JsonFileBackedData<Map<String, Set<String>>> storedDataSets;
  private final HashMap<UUID, StringBuffer> statusMap;
  private final DataSetPathHelper dataSetPathHelper;

  public DataSetFactory(ExecutorService executorService, VreAuthorizationCrud vreAuthorizationCrud,
                        DataSetConfiguration configuration, BdbDatabaseCreator dbFactory) throws IOException {
    this.executorService = executorService;
    this.vreAuthorizationCrud = vreAuthorizationCrud;
    this.configuration = configuration;
    this.dbFactory = dbFactory;
    dataSetMap = new HashMap<>();
    dataSetPathHelper = new DataSetPathHelper(configuration.getDataSetMetadataLocation());
    storedDataSets = JsonFileBackedData.getOrCreate(
      new File(configuration.getDataSetMetadataLocation(), "dataSets.json"),
      HashMap::new,
      new TypeReference<Map<String, Set<String>>>() {}
    );
    statusMap = new HashMap<>();
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

  public ImportManager createDataSet(String userId, String dataSetId) throws DataStoreCreationException {
    return make(userId, dataSetId).importManager;

  }

  public RdfDataSourceFactory createDataSource(String userId, String dataSetId) throws DataStoreCreationException {
    return make(userId, dataSetId).dataSource;
  }

  private DataSet make(String userId, String dataSetId) throws DataStoreCreationException {
    String authorizationKey = userId + "_" + dataSetId;
    synchronized (dataSetMap) {
      Map<String, DataSet> userDataSets = dataSetMap.computeIfAbsent(userId, key -> new HashMap<>());
      if (!userDataSets.containsKey(dataSetId)) {
        DataSet dataSet = createNewDataSet(userId, dataSetId, authorizationKey);
        userDataSets.put(dataSetId, dataSet);
        try {
          storedDataSets.updateData(dataSets -> {
            dataSets.computeIfAbsent(userId, key -> new HashSet<>()).add(dataSetId);
            return dataSets;
          });
        } catch (IOException e) {
          throw new DataStoreCreationException(e);
        }
      }
      return userDataSets.get(dataSetId);
    }
  }

  private DataSet createNewDataSet(String userId, String dataSetId, String authorizationKey)
    throws DataStoreCreationException {
    try {
      vreAuthorizationCrud.createAuthorization(authorizationKey, userId, "ADMIN");
      ImportManager importManager = new ImportManager(
        dataSetPathHelper.fileInDataSet(userId, dataSetId, "log.json"),
        configuration.getFileStorage().makeFileStorage(userId, dataSetId),
        configuration.getFileStorage().makeFileStorage(userId, dataSetId),
        configuration.getFileStorage().makeLogStorage(userId, dataSetId),
        executorService,
        configuration.getRdfIo()
      );

      DataSet dataSet = new DataSet();
      dataSet.dataFetcherFactory = new DataStoreDataFetcherFactory(
        userId,
        dataSetId,
        importManager,
        dbFactory
      );
      dataSet.typeNameStore = new JsonTypeNameStore(
        dataSetPathHelper.fileInDataSet(userId, dataSetId, "prefixes.json"),
        importManager
      );
      dataSet.schemaStore = new JsonSchemaStore(
        importManager,
        dataSetPathHelper.fileInDataSet(userId, dataSetId, "schema.json")
      );
      dataSet.importManager = importManager;
      dataSet.dataSource = new RdfDataSourceFactory(
        new DataSourceStore(userId, dataSetId, dbFactory, importManager)
      );
      return dataSet;
    } catch (AuthorizationCreationException | IOException e) {
      throw new DataStoreCreationException(e);
    }
  }

  public boolean dataSetExists(String ownerId, String dataSet) {
    return dataSetMap.containsKey(ownerId) && dataSetMap.get(ownerId).containsKey(dataSet);
  }

  public Map<String, Set<String>> getDataSets() {
    return storedDataSets.getData();
  }

  public Optional<String> getStatus(UUID uuid) {
    return statusMap.containsKey(uuid) ? Optional.of(statusMap.get(uuid).toString()) : Optional.empty();
  }

  public Tuple<UUID, RdfCreator> registerRdfCreator(Function<Consumer<String>, RdfCreator> rdfCreatorBuilder) {
    StringBuffer stringBuffer = new StringBuffer();
    UUID uuid = UUID.randomUUID();
    statusMap.put(uuid, stringBuffer);

    RdfCreator rdfCreator = rdfCreatorBuilder.apply((str) -> {
      stringBuffer.setLength(0);
      stringBuffer.append(str);
    });

    return Tuple.tuple(uuid, rdfCreator);
  }

  public void removeDataSet(String ownerId, String dataSetName) throws IOException {
    dbFactory.removeDatabasesFor(ownerId, dataSetName);
    // remove from datasets.json
    storedDataSets.updateData(dataSets -> {
      dataSets.get(ownerId).remove(dataSetName);
      return dataSets;
    });
    dataSetMap.get(ownerId).remove(dataSetName);

    // remove folder
    FileUtils.deleteDirectory(dataSetPathHelper.dataSetPath(ownerId, dataSetName));
  }

  private class DataSet {
    private DataFetcherFactory dataFetcherFactory;
    private SchemaStore schemaStore;
    private TypeNameStore typeNameStore;
    private ImportManager importManager;
    private RdfDataSourceFactory dataSource;
  }
}
