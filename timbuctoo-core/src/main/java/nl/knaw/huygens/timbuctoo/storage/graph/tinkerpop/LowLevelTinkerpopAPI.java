package nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop;

import nl.knaw.huygens.timbuctoo.model.Entity;

import com.tinkerpop.blueprints.Vertex;

public class LowLevelTinkerpopAPI {

  public <T extends Entity> Vertex getLatestVertexById(Class<T> type, String id) {
    throw new UnsupportedOperationException("Yet to be implemented");
  }

}
