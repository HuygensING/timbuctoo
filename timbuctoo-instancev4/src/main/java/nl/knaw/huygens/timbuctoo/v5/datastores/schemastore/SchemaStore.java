package nl.knaw.huygens.timbuctoo.v5.datastores.schemastore;

import nl.knaw.huygens.timbuctoo.v5.dataset.OptimizedPatchListener;
import nl.knaw.huygens.timbuctoo.v5.datastores.schemastore.dto.Type;

import java.util.Map;

public interface SchemaStore extends OptimizedPatchListener {

  Map<String, Type> getStableTypes();

  void close();

  boolean isClean();

  void empty();
}
