package nl.knaw.huygens.timbuctoo.v5.dropwizard;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import io.dropwizard.lifecycle.Managed;
import nl.knaw.huygens.timbuctoo.util.Tuple;
import nl.knaw.huygens.timbuctoo.v5.datastores.exceptions.DataStoreCreationException;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class BdbDatabaseFactory implements Managed, BdbDatabaseCreator {
  private final String databaseLocation;
  Map<String, Environment> environmentMap = new HashMap<>();
  Map<String, Database> databases = new HashMap<>();
  protected final EnvironmentConfig configuration;
  protected File dbHome;

  @JsonCreator
  public BdbDatabaseFactory(@JsonProperty("databaseLocation") String databaseLocation) {
    this.databaseLocation = databaseLocation;
    configuration = new EnvironmentConfig(new Properties());
    configuration.setTransactional(true);
    configuration.setTxnNoSync(true);
    configuration.setAllowCreate(true);
    configuration.setSharedCache(true);
  }

  @Override
  public Tuple<Environment, Database> getDatabase(String userId, String dataSetId, String databaseName,
                                                  DatabaseConfig config)
    throws DataStoreCreationException {
    String environmentKey = userId + "_" + dataSetId;
    String databaseKey = environmentKey + "_" + databaseName;
    if (!databases.containsKey(databaseKey)) {
      if (!environmentMap.containsKey(environmentKey)) {
        try {
          File dbDir = new File(dbHome, environmentKey);
          dbDir.mkdirs();
          Environment dataSetEnvironment = new Environment(dbDir, configuration);
          environmentMap.put(environmentKey, dataSetEnvironment);
        } catch (DatabaseException e) {
          throw new DataStoreCreationException(e);
        }
      }
      try {
        databases.put(databaseKey, environmentMap.get(environmentKey).openDatabase(null, databaseName, config));
      } catch (DatabaseException e) {
        throw new DataStoreCreationException(e);
      }
    }
    return Tuple.tuple(environmentMap.get(environmentKey), databases.get(databaseKey));
  }

  public void start() throws Exception {
    dbHome = new File(databaseLocation);
    dbHome.mkdirs();
    if (!dbHome.isDirectory()) {
      throw new IllegalStateException("Database home at '" + dbHome.getAbsolutePath() + "' is not a directory");
    }
  }

  public void stop() throws Exception {
    for (Database database : databases.values()) {
      database.close();
    }
  }
}
