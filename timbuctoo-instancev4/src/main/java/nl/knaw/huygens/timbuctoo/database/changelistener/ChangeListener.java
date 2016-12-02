package nl.knaw.huygens.timbuctoo.database.changelistener;

import nl.knaw.huygens.timbuctoo.database.dto.dataset.Collection;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.util.Optional;

public interface ChangeListener {
  void onCreate(Collection collection, Vertex vertex);

  void onPropertyUpdate(Collection collection, Optional<Vertex> oldVertex, Vertex newVertex);

  void onRemoveFromCollection(Collection collection, Optional<Vertex> oldVertex, Vertex newVertex);

  void onAddToCollection(Collection collection, Optional<Vertex> oldVertex, Vertex newVertex);
}
