package nl.knaw.huygens.timbuctoo.datastores.schemastore;

import nl.knaw.huygens.timbuctoo.datastores.schemastore.dto.Type;

import java.util.Map;

public interface SchemaStore {
  /**
   * @return a map that can be empty
   */
  Map<String, Type> getStableTypes();

  void close();
}
