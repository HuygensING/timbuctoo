package nl.knaw.huygens.timbuctoo.v5.dataset;

import com.fasterxml.jackson.core.type.TypeReference;
import nl.knaw.huygens.timbuctoo.security.VreAuthorizationCrud;
import nl.knaw.huygens.timbuctoo.security.exceptions.AuthorizationCreationException;
import nl.knaw.huygens.timbuctoo.security.exceptions.AuthorizationUnavailableException;
import nl.knaw.huygens.timbuctoo.util.Tuple;
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
import java.util.stream.Collectors;

/**
 * - stores all configuration parameters so it can inject them in the dataset constructor
 * - makes CreateDataSet a singleton
 * - keeps track of all created dataSets across restarts (stores them in a file)
 */
public class DataSetFactory implements DataFetcherFactoryFactory, SchemaStoreFactory, TypeNameStoreFactory {

  private final ExecutorService executorService;
  private final VreAuthorizationCrud vreAuthorizationCrud;
  private final DataSetConfiguration configuration;
  private final DataStoreFactory dataStoreFactory;
  private final Map<String, Map<String, DataSet>> dataSetMap;
  private final JsonFileBackedData<Map<String, Set<PromotedDataSet>>> storedDataSets;
  private final HashMap<UUID, StringBuffer> statusMap;
  private final DataSetPathHelper dataSetPathHelper;


  public DataSetFactory(ExecutorService executorService, VreAuthorizationCrud vreAuthorizationCrud,
                        DataSetConfiguration configuration,
                        DataStoreFactory dataStoreFactory) throws IOException {
    this.executorService = executorService;
    this.vreAuthorizationCrud = vreAuthorizationCrud;
    this.configuration = configuration;
    this.dataStoreFactory = dataStoreFactory;

    dataSetMap = new HashMap<>();
    dataSetPathHelper = new DataSetPathHelper(configuration.getDataSetMetadataLocation());
    storedDataSets = JsonFileBackedData.getOrCreate(
      new File(configuration.getDataSetMetadataLocation(), "dataSets.json"),
      HashMap::new,
      new TypeReference<Map<String, Set<PromotedDataSet>>>() {
      }
    );
    statusMap = new HashMap<>();
  }

  @Override
  public DataFetcherFactory createDataFetcherFactory(String userId, String dataSetId)
    throws DataStoreCreationException {
    return make(userId, dataSetId).createDataFetcherFactory();
  }

  @Override
  public SchemaStore createSchemaStore(String userId, String dataSetId) throws DataStoreCreationException {
    return make(userId, dataSetId).schemaStore;
  }

  @Override
  public TypeNameStore createTypeNameStore(String userId, String dataSetId) throws DataStoreCreationException {
    return make(userId, dataSetId).typeNameStore;
  }

  public ImportManager createImportManager(String userId, String dataSetId) throws DataStoreCreationException {
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
          PromotedDataSet promotedDataSet = PromotedDataSet.create(dataSetId, false);
          storedDataSets.updateData(dataSets -> {
            dataSets.computeIfAbsent(userId, key -> new HashSet<>()).add(promotedDataSet);
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
      QuadStore quadStore = dataStoreFactory.createQuadStore(importManager, userId, dataSetId);
      CollectionIndex collectionIndex = dataStoreFactory.createCollectionIndex(importManager, userId, dataSetId);
      dataSet.quadStore = quadStore;
      dataSet.collectionIndex = collectionIndex;
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
        dataStoreFactory.createDataSourceStore(importManager, userId, dataSetId)
      );
      return dataSet;
    } catch (AuthorizationCreationException | IOException e) {
      throw new DataStoreCreationException(e);
    }
  }

  public boolean dataSetExists(String ownerId, String dataSet) {
    return dataSetMap.containsKey(ownerId) && dataSetMap.get(ownerId).containsKey(dataSet);
  }

  public Map<String, Set<PromotedDataSet>> getDataSets() {
    return storedDataSets.getData();
  }

  public Map<String, Set<PromotedDataSet>> getPromotedDataSets() {
    Map<String, Set<PromotedDataSet>> dataSets = storedDataSets.getData();
    Map<String, Set<PromotedDataSet>> promotedDataSets = new HashMap<>();

    for (Map.Entry<String, Set<PromotedDataSet>> userDataSets : dataSets.entrySet()) {
      Set<PromotedDataSet> mappedUserSets = userDataSets.getValue()
                                                        .stream()
                                                        .filter(dataSet -> dataSet.isPromoted())
                                                        .collect(Collectors.toSet());
      promotedDataSets.put(userDataSets.getKey(), mappedUserSets);
    }
    return promotedDataSets;
  }

  public Map<String, Set<PromotedDataSet>> getDataSetsWithWriteAccess(String userId) {
    Map<String, Set<PromotedDataSet>> dataSets = storedDataSets.getData();
    Map<String, Set<PromotedDataSet>> promotedDataSets = new HashMap<>();

    for (Map.Entry<String, Set<PromotedDataSet>> userDataSets : dataSets.entrySet()) {
      Set<PromotedDataSet> mappedUserSets = userDataSets.getValue()
                                                        .stream()
                                                        .filter(dataSet ->
                                                        {
                                                          try {
                                                            return
                                                              vreAuthorizationCrud
                                                                .getAuthorization(
                                                                  userDataSets.getKey() + "_" + dataSet.getName(),
                                                                  userId)
                                                                .isPresent();
                                                          } catch (AuthorizationUnavailableException e) {
                                                            return false;
                                                          }
                                                        })
                                                        .collect(Collectors.toSet());

      promotedDataSets.put(userDataSets.getKey(), mappedUserSets);
    }
    return promotedDataSets;
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
    dataStoreFactory.removeDataStoresFor(ownerId, dataSetName);
    // remove from datasets.json
    storedDataSets.updateData(dataSets -> {
      Set<PromotedDataSet>
        dataSetsToKeep = dataSets.get(ownerId).stream().filter(dataSet -> !dataSet.getName().equals(dataSetName))
                                 .collect(Collectors.toSet());
      dataSets.put(ownerId, dataSetsToKeep);
      return dataSets;
    });
    dataSetMap.get(ownerId).remove(dataSetName);

    // remove folder
    FileUtils.deleteDirectory(dataSetPathHelper.dataSetPath(ownerId, dataSetName));
  }

  public void stop() {
    // TODO let data set close all its stores
    dataStoreFactory.stop();
  }

  public void start() {
    dataStoreFactory.start();
  }

  private class DataSet {
    private SchemaStore schemaStore;
    private TypeNameStore typeNameStore;
    private ImportManager importManager;
    private RdfDataSourceFactory dataSource;
    private QuadStore quadStore;
    private CollectionIndex collectionIndex;

    public DataFetcherFactory createDataFetcherFactory() {
      return new DataStoreDataFetcherFactory(quadStore, collectionIndex);
    }
  }
}
