package nl.knaw.huygens.timbuctoo.tools.conversion;

import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.IsLatestVersionOfVertex;

import com.google.common.base.Predicate;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Vertex;

public class VertexFinder {

  private static final Predicate<Vertex> IS_LATEST = new IsLatestVersionOfVertex();
  private Graph graph;

  public VertexFinder(Graph graph) {
    this.graph = graph;
  }

  public Vertex getLatestVertexById(String id) {
    Iterable<Vertex> vertices = graph.query().has(Entity.ID_DB_PROPERTY_NAME, id).vertices();

    for (Vertex vertex : vertices) {
      if (IS_LATEST.apply(vertex)) {
        return vertex;
      }
    }

    return null;
  }

}
