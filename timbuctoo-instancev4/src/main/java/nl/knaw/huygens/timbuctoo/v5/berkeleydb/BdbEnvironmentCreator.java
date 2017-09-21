package nl.knaw.huygens.timbuctoo.v5.berkeleydb;

import com.sleepycat.bind.EntryBinding;
import nl.knaw.huygens.timbuctoo.v5.dataset.exceptions.DataStoreCreationException;

public interface BdbEnvironmentCreator {
  <KeyT, ValueT> BdbWrapper<KeyT, ValueT> getDatabase(String userId, String dataSetId, String databaseName,
                                                      boolean allowDuplicates, EntryBinding<KeyT> keyBinder,
                                                      EntryBinding<ValueT> valueBinder)
    throws DataStoreCreationException;

  void removeDatabasesFor(String userId, String dataSetId);

  void start();

  void stop();

  void startTransaction();

  void commitTransaction();
}
