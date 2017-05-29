package nl.knaw.huygens.timbuctoo.v5.datastores;

import nl.knaw.huygens.timbuctoo.v5.datastores.exceptions.DataStoreCreationException;

public interface SingleDataStoreFactory<T> {
  T getOrCreate(String userId, String dataSetId) throws DataStoreCreationException;
}
