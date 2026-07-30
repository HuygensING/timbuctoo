package nl.knaw.huygens.timbuctoo.datastores.schemastore;

import nl.knaw.huygens.timbuctoo.dataset.OptimizedPatchListener;
import nl.knaw.huygens.timbuctoo.datastores.quadstore.QuadStore;
import nl.knaw.huygens.timbuctoo.datastores.schemastore.dto.Type;

import java.util.Map;

public interface SchemaStore extends OptimizedPatchListener {
  /**
   * @return a map that can be empty
   */
  Map<String, Type> getStableTypes();

  void close();

  boolean isClean();

  void empty();

  void rebuildSchema(QuadStore quadStore);
}
