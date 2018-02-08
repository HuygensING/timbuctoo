package nl.knaw.huygens.timbuctoo.v5.dropwizard;

import com.google.common.collect.Maps;
import com.google.common.io.Files;
import com.sleepycat.bind.EntryBinding;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import nl.knaw.huygens.timbuctoo.v5.berkeleydb.BdbEnvironmentCreator;
import nl.knaw.huygens.timbuctoo.v5.berkeleydb.BdbStoreProvider;
import nl.knaw.huygens.timbuctoo.v5.berkeleydb.BdbWrapper;
import nl.knaw.huygens.timbuctoo.v5.berkeleydb.exceptions.BdbDbCreationException;
import nl.knaw.huygens.timbuctoo.v5.berkeleydb.isclean.IsCleanHandler;
import nl.knaw.huygens.timbuctoo.v5.dataset.StoreProvider;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import static org.slf4j.LoggerFactory.getLogger;

public class BdbNonPersistentEnvironmentCreator implements BdbEnvironmentCreator {

  protected final EnvironmentConfig configuration;
  protected final File dbHome;
  private final Map<String, Database> databases;
  private static final Logger LOG = getLogger(BdbNonPersistentEnvironmentCreator.class);
  private Map<String, Environment> environmentMap;

  public BdbNonPersistentEnvironmentCreator() {
    configuration = new EnvironmentConfig(new Properties());
    configuration.setTransactional(true);
    configuration.setAllowCreate(true);
    configuration.setSharedCache(true);
    dbHome = Files.createTempDir();
    databases = Maps.newHashMap();
    environmentMap = Maps.newHashMap();
  }

  @Override
  public StoreProvider createStoreProvider(String userId, String dataSetId) {
    return new BdbStoreProvider(userId, dataSetId, this);
  }

  @Override
  public <KeyT, ValueT> BdbWrapper<KeyT, ValueT> getDatabase(String userId, String dataSetId, String databaseName,
                                                             boolean allowDuplicates, EntryBinding<KeyT> keyBinder,
                                                             EntryBinding<ValueT> valueBinder,
                                                             IsCleanHandler<KeyT, ValueT> isCleanHandler)
    throws BdbDbCreationException {
    try {
      DatabaseConfig config = new DatabaseConfig();
      config.setAllowCreate(true);
      config.setDeferredWrite(true);
      config.setSortedDuplicates(allowDuplicates);

      String environmentKey = environmentKey(userId, dataSetId);
      File envHome = new File(dbHome, environmentKey);
      envHome.mkdirs();
      Environment dataSetEnvironment = new Environment(envHome, configuration);
      Database database = dataSetEnvironment.openDatabase(null, databaseName, config);
      databases.put(environmentKey + "_" + databaseName, database);
      environmentMap.put(environmentKey, dataSetEnvironment);
      return new BdbWrapper<>(dataSetEnvironment, database, config, keyBinder, valueBinder, isCleanHandler);
    } catch (DatabaseException e) {
      throw new BdbDbCreationException(e);
    }
  }

  private String environmentKey(String userId, String dataSetId) {
    return userId + "_" + dataSetId;
  }

  @Override
  public void closeEnvironment(String ownerId, String dataSetId) {
    String environmentKey = environmentKey(ownerId, dataSetId);

    Set<String> keys = new HashSet<>(databases.keySet());
    for (String key : keys) {
      if (key.startsWith(environmentKey)) {
        try {
          databases.remove(key).close();
        } catch (Throwable t) {
          LOG.error("Could not close '" + key + "'", t);
        }
      }
    }

    if (environmentMap.containsKey(environmentKey)) {
      environmentMap.remove(environmentKey).close();
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
