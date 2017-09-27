package nl.knaw.huygens.timbuctoo.v5.dataset;

import nl.knaw.huygens.timbuctoo.v5.datastores.collectionindex.CollectionIndex;
import nl.knaw.huygens.timbuctoo.v5.dataset.exceptions.DataStoreCreationException;
import nl.knaw.huygens.timbuctoo.v5.datastores.quadstore.QuadStore;
import nl.knaw.huygens.timbuctoo.v5.rml.RmlDataSourceStore;

public interface DataStoreFactory {
  QuadStore createQuadStore(DataProvider importManager, String userId,
                            String dataSetId) throws DataStoreCreationException;

  CollectionIndex createCollectionIndex(DataProvider dataProvider, String userId,
                                        String dataSetId) throws DataStoreCreationException;

  RmlDataSourceStore createDataSourceStore(DataProvider dataProvider, String userId, String dataSetId)
    throws DataStoreCreationException;

  void removeDataStoresFor(String userId, String dataSetId);

  void stop();

  void start();
}
