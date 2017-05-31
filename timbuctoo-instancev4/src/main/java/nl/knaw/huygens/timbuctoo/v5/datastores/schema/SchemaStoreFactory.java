package nl.knaw.huygens.timbuctoo.v5.datastores.schema;

import nl.knaw.huygens.timbuctoo.v5.datastores.exceptions.DataStoreCreationException;

public interface SchemaStoreFactory {
  SchemaStore createSchemaStore(String userId, String dataSetId) throws DataStoreCreationException;
}
