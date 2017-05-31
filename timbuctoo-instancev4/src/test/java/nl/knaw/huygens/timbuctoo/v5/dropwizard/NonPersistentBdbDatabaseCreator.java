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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class NonPersistentBdbDatabaseCreator implements BdbDatabaseCreator {

  protected final EnvironmentConfig configuration;
  protected final File dbHome;
  private final List<Database> databases;

  public NonPersistentBdbDatabaseCreator() {
    configuration = new EnvironmentConfig(new Properties());
    configuration.setTransactional(true);
    configuration.setTxnNoSync(true);
    configuration.setAllowCreate(true);
    configuration.setSharedCache(true);
    dbHome = Files.createTempDir();
    databases = new ArrayList<>();
  }

  @Override
  public Tuple<Environment, Database> getDatabase(String userId, String dataSetId, String databaseName,
                                                  DatabaseConfig config) throws DataStoreCreationException {
    try {
      config.setTemporary(true);
      Environment dataSetEnvironment = new Environment(new File(dbHome, userId + "_" + dataSetId), configuration);
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
    FileUtils.cleanDirectory(dbHome);
  }
}
