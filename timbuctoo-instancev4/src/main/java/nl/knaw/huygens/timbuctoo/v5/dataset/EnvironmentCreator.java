package nl.knaw.huygens.timbuctoo.v5.dataset;

public interface EnvironmentCreator {
  StoreProvider createStoreProvider(String userId,
                                    String dataSetId);

  /**
   * Closes and remove all the databases for a data set
   */
  void closeEnvironment(String ownerId, String dataSetId);

  void start();

  void stop();
}
