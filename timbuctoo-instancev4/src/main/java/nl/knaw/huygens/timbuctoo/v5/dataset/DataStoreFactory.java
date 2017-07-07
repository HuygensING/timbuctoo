package nl.knaw.huygens.timbuctoo.v5.dataset;

import nl.knaw.huygens.timbuctoo.v5.datastores.exceptions.DataStoreCreationException;
import nl.knaw.huygens.timbuctoo.v5.rml.DataSourceStore;

public interface DataStoreFactory {
  QuadStore createQuadStore(DataProvider importManager, String userId,
                            String dataSetId) throws DataStoreCreationException;

  SubjectStore createSubjectStore(DataProvider importManager, String userId,
                                  String dataSetId) throws DataStoreCreationException;

  DataSourceStore createDataSourceStore(ImportManager importManager, String userId, String dataSetId)
    throws DataStoreCreationException;

  void removeDataStoresFor(String userId, String dataSetId);

}
