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
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.DataFetcherFactory;
import nl.knaw.huygens.timbuctoo.v5.rml.DataSourceStore;
import nl.knaw.huygens.timbuctoo.v5.rml.RdfDataSourceFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;
import java.util.function.Function;

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
  private final HashMap<UUID, StringBuffer> statusMap;

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
      HashMap::new,
      new TypeReference<Map<String, List<String>>>() {}
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

  public DataSet createDataSet(String userId, String dataSetId) throws DataStoreCreationException {
    return make(userId, dataSetId).dataSet;

  }

  public RdfDataSourceFactory createDataSource(String userId, String dataSetId) throws DataStoreCreationException {
    return make(userId, dataSetId).dataSource;
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
          result.typeNameStore = new JsonTypeNameStore(
            new File(metaDataLocation, userId + "_" + dataSetId + "-prefixes.json"),
            dataSet
          );
          result.schemaStore = new JsonSchemaStore(metaDataLocation, userId, dataSetId, dataSet);
          result.dataSet = dataSet;
          result.dataSource = new RdfDataSourceFactory(new DataSourceStore(userId, dataSetId, dbFactory, dataSet));
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

  public boolean dataSetExists(String ownerId, String dataSet) {
    return dataSetMap.containsKey(ownerId) && dataSetMap.get(ownerId).containsKey(dataSet);
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

  private class DataStores {
    public DataFetcherFactory dataFetcherFactory;
    public SchemaStore schemaStore;
    public TypeNameStore typeNameStore;
    public DataSet dataSet;
    public RdfDataSourceFactory dataSource;
  }
}
