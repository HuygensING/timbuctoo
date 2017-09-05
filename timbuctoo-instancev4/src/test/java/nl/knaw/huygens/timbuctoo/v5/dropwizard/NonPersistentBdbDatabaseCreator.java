package nl.knaw.huygens.timbuctoo.v5.dropwizard;

import com.google.common.collect.Maps;
import com.google.common.io.Files;
import com.sleepycat.bind.EntryBinding;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import nl.knaw.huygens.timbuctoo.v5.berkeleydb.BdbDatabaseCreator;
import nl.knaw.huygens.timbuctoo.v5.berkeleydb.BdbWrapper;
import nl.knaw.huygens.timbuctoo.v5.dataset.exceptions.DataStoreCreationException;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import static org.slf4j.LoggerFactory.getLogger;

public class NonPersistentBdbDatabaseCreator implements BdbDatabaseCreator {

  protected final EnvironmentConfig configuration;
  protected final File dbHome;
  private final Map<String, Database> databases;
  private static final Logger LOG = getLogger(NonPersistentBdbDatabaseCreator.class);
  private Map<String, Environment> environmentMap;

  public NonPersistentBdbDatabaseCreator() {
    configuration = new EnvironmentConfig(new Properties());
    configuration.setTransactional(true);
    configuration.setAllowCreate(true);
    configuration.setSharedCache(true);
    dbHome = Files.createTempDir();
    databases = Maps.newHashMap();
    environmentMap = Maps.newHashMap();
  }

  @Override
  public <T> BdbWrapper<T> getDatabase(String userId, String dataSetId, String databaseName,
                                       DatabaseConfig config, EntryBinding<T> binder)
    throws DataStoreCreationException {
    try {
      String environmentKey = environmentKey(userId, dataSetId);
      File envHome = new File(dbHome, environmentKey);
      envHome.mkdirs();
      Environment dataSetEnvironment = new Environment(envHome, configuration);
      Database database = dataSetEnvironment.openDatabase(null, databaseName, config);
      databases.put(environmentKey + "_" + databaseName, database);
      environmentMap.put(environmentKey, dataSetEnvironment);
      return new BdbWrapper<>(dataSetEnvironment, database, config, binder);
    } catch (DatabaseException e) {
      throw new DataStoreCreationException(e);
    }
  }

  private String environmentKey(String userId, String dataSetId) {
    return userId + "_" + dataSetId;
  }

  @Override
  public void removeDatabasesFor(String userId, String dataSetId) {
    String environmentKey = environmentKey(userId, dataSetId);

    List<String> dbsToRemove = databases.keySet().stream()
                                        .filter(dbName -> dbName.startsWith(environmentKey))
                                        .collect(Collectors.toList());

    for (String dbToRemove : dbsToRemove) {
      databases.get(dbToRemove).close();
      databases.remove(dbToRemove);
    }

    if (environmentMap.containsKey(environmentKey)) {
      environmentMap.get(environmentKey).close();
      environmentMap.remove(environmentKey);
    }
  }

  @Override
  public void start() {

  }

  @Override
  public void stop() {
    try {
      this.close();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }


  public void close() throws DatabaseException, IOException {
    for (Database database : databases.values()) {
      database.close();
    }
    boolean wasDeleted = false;
    int tries = 0;
    while (!wasDeleted) {
      try {
        FileUtils.cleanDirectory(dbHome);
        wasDeleted = true;
      } catch (IOException e) {
        tries++;
        if (tries >= 10) {
          wasDeleted = true;
        } else {
          try {
            Thread.sleep(1);
          } catch (InterruptedException e1) {
            LOG.error("Trying to clean up and delete directory, but it failed the first time around and then the " +
              "thread was interrupted");
            wasDeleted = true;
          }
        }
      }
    }
  }
}
