package nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop;

import nl.knaw.huygens.timbuctoo.model.Entity;

public class ElementConverterFactory {

  public <T extends Entity> VertexConverter<T> forType(Class<T> type) {
    throw new UnsupportedOperationException("Yet to be implemented");
  }

}
