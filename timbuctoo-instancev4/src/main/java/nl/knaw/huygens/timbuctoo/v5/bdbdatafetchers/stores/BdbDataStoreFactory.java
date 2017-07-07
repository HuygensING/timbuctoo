package nl.knaw.huygens.timbuctoo.v5.bdbdatafetchers.stores;

import nl.knaw.huygens.timbuctoo.v5.bdb.BdbDatabaseCreator;
import nl.knaw.huygens.timbuctoo.v5.dataset.DataProvider;
import nl.knaw.huygens.timbuctoo.v5.dataset.DataStoreFactory;
import nl.knaw.huygens.timbuctoo.v5.dataset.ImportManager;
import nl.knaw.huygens.timbuctoo.v5.dataset.QuadStore;
import nl.knaw.huygens.timbuctoo.v5.dataset.SubjectStore;
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
  public SubjectStore createSubjectStore(DataProvider importManager, String userId,
                                         String dataSetId) throws DataStoreCreationException {
    return new BdbCollectionIndex(importManager, dbFactory, userId, dataSetId);
  }

  @Override
  public DataSourceStore createDataSourceStore(ImportManager importManager, String userId, String dataSetId)
    throws DataStoreCreationException {
    return new DataSourceStore(userId, dataSetId, dbFactory, importManager);
  }

  @Override
  public void removeDataStoresFor(String userId, String dataSetId) {
    dbFactory.removeDatabasesFor(userId, dataSetId);
  }

}
