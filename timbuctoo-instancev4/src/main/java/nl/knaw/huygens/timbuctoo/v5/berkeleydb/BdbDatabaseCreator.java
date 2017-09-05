package nl.knaw.huygens.timbuctoo.v5.berkeleydb;

import com.sleepycat.bind.EntryBinding;
import com.sleepycat.je.DatabaseConfig;
import nl.knaw.huygens.timbuctoo.v5.dataset.exceptions.DataStoreCreationException;

public interface BdbDatabaseCreator {
  <T> BdbWrapper<T> getDatabase(String userId, String dataSetId, String databaseName,
                         DatabaseConfig config, EntryBinding<T> binder) throws DataStoreCreationException;

  void removeDatabasesFor(String userId, String dataSetId);

  void start();

  void stop();
}
