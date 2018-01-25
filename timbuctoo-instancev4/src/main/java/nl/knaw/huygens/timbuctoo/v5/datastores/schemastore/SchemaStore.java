package nl.knaw.huygens.timbuctoo.v5.datastores.schemastore;

import nl.knaw.huygens.timbuctoo.v5.datastores.schemastore.dto.Type;

import java.util.Map;

public interface SchemaStore {
  Map<String, Type> getTypes();

  /**
   * The method should be used, when no half created schema's can be accepted.
   */
  Map<String, Type> getStableTypes();

  void close();
}
