package nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop;

import com.tinkerpop.blueprints.Vertex;
import nl.knaw.huygens.timbuctoo.model.Entity;

public interface VertexConverter<T extends Entity> extends ElementConverter<T, Vertex> {

  void removeVariant(Vertex vertex);

}
