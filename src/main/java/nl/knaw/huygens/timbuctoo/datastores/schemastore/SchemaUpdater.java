package nl.knaw.huygens.timbuctoo.datastores.schemastore;

import nl.knaw.huygens.timbuctoo.datastores.schemastore.dto.Type;

import java.util.Map;

public interface SchemaUpdater {
  void replaceSchema(Map<String, Type> newSchema) throws SchemaUpdateException;
}
