package nl.knaw.huygens.timbuctoo.v5.berkeleydb;

import com.sleepycat.je.DatabaseConfig;
import nl.knaw.huygens.timbuctoo.v5.dataset.exceptions.DataStoreCreationException;

public interface BdbDatabaseCreator {
  BdbWrapper getDatabase(String userId, String dataSetId, String databaseName,
                                           DatabaseConfig config)
    throws DataStoreCreationException;

  void removeDatabasesFor(String userId, String dataSetId);

  void start();

  void stop();
}
