package nl.knaw.huygens.timbuctoo.v5.berkeleydb;

import com.sleepycat.bind.EntryBinding;
import com.sleepycat.je.Database;
import com.sleepycat.je.Environment;
import nl.knaw.huygens.timbuctoo.v5.berkeleydb.exceptions.BdbDbCreationException;
import nl.knaw.huygens.timbuctoo.v5.berkeleydb.isclean.IsCleanHandler;

import java.io.IOException;
import java.util.List;

public interface BdbEnvironmentCreator {
  <KeyT, ValueT> BdbWrapper<KeyT, ValueT> getDatabase(String userId, String dataSetName, String databaseName,
                                                      boolean allowDuplicates, EntryBinding<KeyT> keyBinder,
                                                      EntryBinding<ValueT> valueBinder,
                                                      IsCleanHandler<KeyT, ValueT> cleanHandler)
    throws BdbDbCreationException;

  /**
   * Closes and remove all the databases for a data set
   */
  void closeEnvironment(String ownerId, String dataSetId);

  void start();

  void stop();

  List<String> getUnavailableDatabases(String ownerId, String dataSetName);

  void closeDatabase(String ownerId, String dataSetId, String dataStore);

  void removeDatabase(String ownerId, String dataSetId, String dataStore);

  void renameDatabase(String ownerId, String dataSetId, String databaseName, String newDatabaseName);

  void backUpDatabases(String backupPath, String ownerId, String dataSetId) throws IOException;
}
