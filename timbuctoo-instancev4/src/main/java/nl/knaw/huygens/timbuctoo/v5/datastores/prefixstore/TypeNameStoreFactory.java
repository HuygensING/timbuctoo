package nl.knaw.huygens.timbuctoo.v5.datastores.prefixstore;

import nl.knaw.huygens.timbuctoo.v5.datastores.exceptions.DataStoreCreationException;

public interface TypeNameStoreFactory {
  TypeNameStore createTypeNameStore(String userId, String dataSetId) throws DataStoreCreationException;
}
