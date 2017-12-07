package nl.knaw.huygens.timbuctoo.v5.berkeleydb;

import com.sleepycat.bind.EntryBinding;
import nl.knaw.huygens.timbuctoo.v5.berkeleydb.exceptions.BdbDbCreationException;

public interface BdbEnvironmentCreator {
  <KeyT, ValueT> BdbWrapper<KeyT, ValueT> getDatabase(String userId, String dataSetId, String databaseName,
                                                      boolean allowDuplicates, EntryBinding<KeyT> keyBinder,
                                                      EntryBinding<ValueT> valueBinder)
    throws BdbDbCreationException;

  void removeDatabasesFor(String userId, String dataSetId);

  void start();

  void stop();

  void startTransaction();

  void commitTransaction();
}
