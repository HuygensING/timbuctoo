package nl.knaw.huygens.timbuctoo.v5.bdbdatafetchers.stores;

import nl.knaw.huygens.timbuctoo.v5.bdb.BdbDatabaseCreator;
import nl.knaw.huygens.timbuctoo.v5.dataset.CollectionIndex;
import nl.knaw.huygens.timbuctoo.v5.dataset.DataProvider;
import nl.knaw.huygens.timbuctoo.v5.dataset.DataStoreFactory;
import nl.knaw.huygens.timbuctoo.v5.dataset.QuadStore;
import nl.knaw.huygens.timbuctoo.v5.datastores.exceptions.DataStoreCreationException;
import nl.knaw.huygens.timbuctoo.v5.rml.DataSourceStore;

public class BdbDataStoreFactory implements DataStoreFactory {


  private final BdbDatabaseCreator dbFactory;

  public BdbDataStoreFactory(BdbDatabaseCreator dbFactory) {
    this.dbFactory = dbFactory;
  }

  @Override
  public QuadStore createQuadStore(DataProvider dataProvider, String userId,
                                   String dataSetId) throws DataStoreCreationException {
    return new BdbTripleStore(dataProvider, dbFactory, userId, dataSetId);
  }

  @Override
  public CollectionIndex createCollectionIndex(DataProvider dataProvider, String userId,
                                               String dataSetId) throws DataStoreCreationException {
    return new BdbCollectionIndex(dataProvider, dbFactory, userId, dataSetId);
  }

  @Override
  public DataSourceStore createDataSourceStore(DataProvider dataProvider, String userId, String dataSetId)
    throws DataStoreCreationException {
    return new DataSourceStore(userId, dataSetId, dbFactory, dataProvider);
  }

  @Override
  public void removeDataStoresFor(String userId, String dataSetId) {
    dbFactory.removeDatabasesFor(userId, dataSetId);
  }

  @Override
  public void stop() {
    dbFactory.stop();
  }

  @Override
  public void start() {
    dbFactory.start();
  }

}
