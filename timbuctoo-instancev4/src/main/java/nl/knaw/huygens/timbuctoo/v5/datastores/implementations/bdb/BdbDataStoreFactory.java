package nl.knaw.huygens.timbuctoo.v5.datastores.implementations.bdb;

import nl.knaw.huygens.timbuctoo.v5.berkeleydb.BdbDatabaseCreator;
import nl.knaw.huygens.timbuctoo.v5.datastores.collectionindex.CollectionIndex;
import nl.knaw.huygens.timbuctoo.v5.dataset.DataProvider;
import nl.knaw.huygens.timbuctoo.v5.dataset.DataStoreFactory;
import nl.knaw.huygens.timbuctoo.v5.datastores.quadstore.QuadStore;
import nl.knaw.huygens.timbuctoo.v5.dataset.exceptions.DataStoreCreationException;
import nl.knaw.huygens.timbuctoo.v5.rml.RmlDataSourceStore;

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
  public RmlDataSourceStore createDataSourceStore(DataProvider dataProvider, String userId, String dataSetId)
    throws DataStoreCreationException {
    return new RmlDataSourceStore(userId, dataSetId, dbFactory, dataProvider);
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
