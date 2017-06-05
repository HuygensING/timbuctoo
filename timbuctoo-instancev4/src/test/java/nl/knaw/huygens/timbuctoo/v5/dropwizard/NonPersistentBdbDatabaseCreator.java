package nl.knaw.huygens.timbuctoo.v5.dropwizard;

import com.google.common.io.Files;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import nl.knaw.huygens.timbuctoo.util.Tuple;
import nl.knaw.huygens.timbuctoo.v5.datastores.exceptions.DataStoreCreationException;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static org.slf4j.LoggerFactory.getLogger;

public class NonPersistentBdbDatabaseCreator implements BdbDatabaseCreator {

  protected final EnvironmentConfig configuration;
  protected final File dbHome;
  private final List<Database> databases;
  private static final Logger LOG = getLogger(NonPersistentBdbDatabaseCreator.class);

  public NonPersistentBdbDatabaseCreator() {
    configuration = new EnvironmentConfig(new Properties());
    configuration.setTransactional(true);
    configuration.setAllowCreate(true);
    configuration.setSharedCache(true);
    dbHome = Files.createTempDir();
    databases = new ArrayList<>();
  }

  @Override
  public Tuple<Environment, Database> getDatabase(String userId, String dataSetId, String databaseName,
                                                  DatabaseConfig config) throws DataStoreCreationException {
    try {
      File envHome = new File(dbHome, userId + "_" + dataSetId);
      envHome.mkdirs();
      Environment dataSetEnvironment = new Environment(envHome, configuration);
      Database database = dataSetEnvironment.openDatabase(null, databaseName, config);
      databases.add(database);
      return Tuple.tuple(dataSetEnvironment, database);
    } catch (DatabaseException e) {
      throw new DataStoreCreationException(e);
    }
  }

  public void close() throws DatabaseException, IOException {
    for (Database database : databases) {
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
