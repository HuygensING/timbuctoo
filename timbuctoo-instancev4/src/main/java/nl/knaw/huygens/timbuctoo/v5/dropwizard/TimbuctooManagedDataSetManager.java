package nl.knaw.huygens.timbuctoo.v5.dropwizard;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.google.common.collect.ImmutableSet;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import io.dropwizard.lifecycle.Managed;
import nl.knaw.huygens.timbuctoo.rml.DataSource;
import nl.knaw.huygens.timbuctoo.server.endpoints.v2.bulkupload.JexlRowFactory;
import nl.knaw.huygens.timbuctoo.v5.datastores.DataSetManager;
import nl.knaw.huygens.timbuctoo.v5.datastores.DataStoreDataFetcherFactory;
import nl.knaw.huygens.timbuctoo.v5.datastores.dto.DataStores;
import nl.knaw.huygens.timbuctoo.v5.datastores.implementations.berkeleydb.BdbCollectionIndex;
import nl.knaw.huygens.timbuctoo.v5.datastores.implementations.berkeleydb.BdbJoinHandler;
import nl.knaw.huygens.timbuctoo.v5.datastores.implementations.berkeleydb.BdbTripleStore;
import nl.knaw.huygens.timbuctoo.v5.datastores.implementations.berkeleydb.TripleStoreDataSource;
import nl.knaw.huygens.timbuctoo.v5.datastores.implementations.json.JsonLogStorage;
import nl.knaw.huygens.timbuctoo.v5.datastores.implementations.json.JsonSchemaStore;
import nl.knaw.huygens.timbuctoo.v5.datastores.implementations.json.JsonTypeNameStore;
import nl.knaw.huygens.timbuctoo.v5.logprocessing.datastore.LogStorage;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Consumer;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Current datastores are:
 *  - local (no network overhead, no network failures)
 *  - cursor based (cheap iteration)
 *  - sharded per dataset (you can't scale one dataset horizontally, but you can scale across datasets)
 */
public class TimbuctooManagedDataSetManager implements Managed, DataSetManager {
  private final String databaseLocation;
  Map<String, DataStores> dataStoresMap = new HashMap<>();
  protected final EnvironmentConfig configuration;
  protected File dbHome;
  private List<Consumer<Set<String>>> subscriptions = new ArrayList<>();
  private static final Logger LOG = getLogger(TimbuctooManagedDataSetManager.class);

  public TimbuctooManagedDataSetManager(String databaseLocation) {
    this.databaseLocation = databaseLocation;
    configuration = new EnvironmentConfig(new Properties());
    configuration.setTransactional(true);
    configuration.setTxnNoSync(true);
    configuration.setAllowCreate(true);
    configuration.setSharedCache(true);
  }

  @Override
  public DataStores getDataStores(String dataSetId) throws IOException {
    if (dataStoresMap.containsKey(dataSetId)) {
      return dataStoresMap.get(dataSetId);
    } else {
      try {
        DataStores result = this.makeDataStores(dataSetId);
        dataStoresMap.put(dataSetId, result);
        return result;
      } catch (DatabaseException e) {
        throw new IOException(e);
      }
    }
  }

  @Override
  public void onDataSetsAvailable(Consumer<Set<String>> subscription) {
    subscriptions.add(subscription);
  }

  @Override
  public Set<String> getDataSets() {
    String[] subDirs = dbHome.list();
    ImmutableSet<String> dirs;
    if (subDirs == null) {
      dirs = ImmutableSet.of();
    } else {
      dirs = ImmutableSet.<String>builder().add(subDirs).build();
    }
    return dirs;
  }

  @Override
  public void removeDataSet(String dataSetId) throws IOException {
    DataStores removed = dataStoresMap.remove(dataSetId);
    if (removed != null) {
      try {
        dataStoresMap.get(dataSetId).close();
      } catch (Exception e) {
        LOG.warn("Closing dataSet failed, but we're going to remove it anyway.", e);
      }
    }
    File dataSetDir = new File(dbHome, dataSetId);
    if (dataSetDir.exists()) {
      Files.walkFileTree(dataSetDir.toPath(), new SimpleFileVisitor<Path>() {
        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attributes) throws IOException {
          Files.delete(file);
          return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
          Files.delete(dir);
          return FileVisitResult.CONTINUE;
        }
      });
    }
  }

  @Override
  public boolean exists(String dataSetId) {
    return new File(dbHome, dataSetId).isDirectory();
  }

  private DataStores makeDataStores(String dataSetName) throws DatabaseException, IOException {
    File dataHome = new File(dbHome, dataSetName);
    dataHome.mkdirs();
    File bdbHome = new File(dataHome, "bdb");
    bdbHome.mkdirs();
    Environment dataSetEnvironment = new Environment(bdbHome, configuration);
    ObjectMapper objectMapper =
      new ObjectMapper().configure(SerializationFeature.INDENT_OUTPUT, true).registerModule(new Jdk8Module());

    final BdbCollectionIndex collectionIndex = new BdbCollectionIndex(dataSetName, dataSetEnvironment, objectMapper);
    final BdbTripleStore tripleStore = new BdbTripleStore(dataSetName, dataSetEnvironment, objectMapper);

    final JsonTypeNameStore prefixStore = new JsonTypeNameStore(new File(dataHome, "prefixes.json"), objectMapper);
    final JsonSchemaStore schemaStore = new JsonSchemaStore(new File(dataHome, "schema.json"), objectMapper);
    File logDir = new File(dataHome, "logs");
    logDir.mkdirs();
    final LogStorage logStorage = new JsonLogStorage(
      new File(dataHome, "logs.json"),
      logDir,
      () -> URI.create("http://timbuctoo.com/" + UUID.randomUUID()),
      objectMapper
    );

    final DataStoreDataFetcherFactory fetchers = new DataStoreDataFetcherFactory(tripleStore, collectionIndex);
    BdbJoinHandler joinHandler = new BdbJoinHandler(dataSetEnvironment, dataSetName, objectMapper);

    final BiFunction<String, Map<String, String>, DataSource> tripleStoreDataSourceFactory =
      (collectionUri, customFields) -> new TripleStoreDataSource(
        tripleStore,
        collectionIndex,
        dataSetName,
        collectionUri,
        new JexlRowFactory(customFields, joinHandler)
      );

    return new DataStores(
      dataSetEnvironment,
      collectionIndex,
      prefixStore,
      tripleStore,
      tripleStoreDataSourceFactory,
      schemaStore,
      fetchers,
      fetchers,
      logStorage
    );
  }

  @Override
  public void start() throws Exception {
    dbHome = new File(databaseLocation);
    dbHome.mkdirs();
    if (!dbHome.isDirectory()) {
      throw new IllegalStateException("Database home at '" + dbHome.getAbsolutePath() + "' is not a directory");
    }
    Set<String> dirs = getDataSets();
    for (Consumer<Set<String>> subscription : subscriptions) {
      subscription.accept(dirs);
    }
  }

  @Override
  public void stop() throws Exception {
    for (DataStores dataStores : dataStoresMap.values()) {
      dataStores.close();
    }
  }
}
