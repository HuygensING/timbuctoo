package nl.knaw.huygens.timbuctoo.v5.berkeleydb;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.sleepycat.bind.EntryBinding;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Durability;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import nl.knaw.huygens.timbuctoo.v5.berkeleydb.exceptions.BdbDbCreationException;
import nl.knaw.huygens.timbuctoo.v5.berkeleydb.isclean.IsCleanHandler;
import nl.knaw.huygens.timbuctoo.v5.filehelper.FileHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

public class BdbPersistentEnvironmentCreator implements BdbEnvironmentCreator {
  private static final Logger LOG = LoggerFactory.getLogger(BdbPersistentEnvironmentCreator.class);
  protected final EnvironmentConfig configuration;
  private final String databaseLocation;
  Map<String, Environment> environmentMap = new HashMap<>();
  Map<String, Database> databases = new HashMap<>();
  private FileHelper fileHelper;

  @JsonCreator
  public BdbPersistentEnvironmentCreator(@JsonProperty("databaseLocation") String databaseLocation) {
    this.databaseLocation = databaseLocation;
    configuration = new EnvironmentConfig(new Properties());
    configuration.setTransactional(true);
    configuration.setDurability(Durability.COMMIT_NO_SYNC);
    configuration.setAllowCreate(true);
    configuration.setSharedCache(true);
  }

  @Override
  public <KeyT, ValueT> BdbWrapper<KeyT, ValueT> getDatabase(String userId, String dataSetId, String databaseName,
                                                             boolean allowDuplicates, EntryBinding<KeyT> keyBinder,
                                                             EntryBinding<ValueT> valueBinder,
                                                             IsCleanHandler<KeyT, ValueT> cleanHandler)
    throws BdbDbCreationException {
    DatabaseConfig config = new DatabaseConfig();
    config.setAllowCreate(true);
    config.setDeferredWrite(true);
    config.setSortedDuplicates(allowDuplicates);

    String environmentKey = environmentKey(userId, dataSetId);
    String databaseKey = environmentKey + "_" + databaseName;
    if (!databases.containsKey(databaseKey)) {
      if (!environmentMap.containsKey(environmentKey)) {
        try {
          File dbDir = fileHelper.pathInDataSet(userId, dataSetId, "databases");
          Environment dataSetEnvironment = new Environment(dbDir, configuration);
          environmentMap.put(environmentKey, dataSetEnvironment);
        } catch (DatabaseException e) {
          throw new BdbDbCreationException(e);
        }
      }
      try {
        databases.put(databaseKey, environmentMap.get(environmentKey).openDatabase(null, databaseName, config));
      } catch (DatabaseException e) {
        throw new BdbDbCreationException(e);
      }
    }
    return new BdbWrapper<>(
      environmentMap.get(environmentKey),
      databases.get(databaseKey),
      config,
      keyBinder,
      valueBinder,
      cleanHandler
    );
  }

  private String environmentKey(String userId, String dataSetId) {
    return userId + "_" + dataSetId;
  }

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

  public String getDatabaseLocation() {
    return databaseLocation;
  }

  @Override
  public void start() {
    File dbHome = new File(databaseLocation);
    dbHome.mkdirs();
    if (!dbHome.isDirectory()) {
      throw new IllegalStateException("Database home at '" + dbHome.getAbsolutePath() + "' is not a directory");
    }
    fileHelper = new FileHelper(dbHome);
  }

  @Override
  public void stop() {

  }

}
