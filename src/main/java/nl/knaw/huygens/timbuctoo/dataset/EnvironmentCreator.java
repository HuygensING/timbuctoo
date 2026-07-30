package nl.knaw.huygens.timbuctoo.dataset;

import java.io.IOException;
import java.util.List;

public interface EnvironmentCreator {
  StoreProvider createStoreProvider(String userId, String dataSetId);

  /**
   * Closes and remove all the databases for a data set
   */
  void closeEnvironment(String ownerId, String dataSetId);

  List<String> getUnavailableDatabases(String ownerId, String dataSetId);

  void backUpDatabases(String backupPath, String ownerId, String dataSetId) throws IOException;

  void closeDatabase(String ownerId, String dataSetId, String dataStore);

  void start();

  void stop();
}
