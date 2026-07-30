package nl.knaw.huygens.timbuctoo.berkeleydb;

import com.sleepycat.bind.EntryBinding;
import nl.knaw.huygens.timbuctoo.berkeleydb.exceptions.BdbDbCreationException;
import nl.knaw.huygens.timbuctoo.berkeleydb.isclean.IsCleanHandler;
import nl.knaw.huygens.timbuctoo.dataset.EnvironmentCreator;

public interface BdbEnvironmentCreator extends EnvironmentCreator {
  <KeyT, ValueT> BdbWrapper<KeyT, ValueT> getDatabase(String userId, String dataSetName, String databaseName,
                                                      boolean allowDuplicates, EntryBinding<KeyT> keyBinder,
                                                      EntryBinding<ValueT> valueBinder,
                                                      IsCleanHandler<KeyT, ValueT> cleanHandler)
      throws BdbDbCreationException;
}
