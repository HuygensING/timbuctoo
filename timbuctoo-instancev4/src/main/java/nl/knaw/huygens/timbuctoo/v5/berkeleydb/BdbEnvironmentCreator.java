package nl.knaw.huygens.timbuctoo.v5.berkeleydb;

import com.sleepycat.bind.EntryBinding;
import nl.knaw.huygens.timbuctoo.v5.berkeleydb.exceptions.BdbDbCreationException;
import nl.knaw.huygens.timbuctoo.v5.berkeleydb.isclean.IsCleanHandler;

public interface BdbEnvironmentCreator {
  <KeyT, ValueT> BdbWrapper<KeyT, ValueT> getDatabase(String userId, String dataSetId, String databaseName,
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

  void startTransaction();

  void commitTransaction();

  void cleanDatabases(String userId, String dataSetId);
}
