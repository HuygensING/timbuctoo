package nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop;

import static nl.knaw.huygens.timbuctoo.model.Entity.ID_PROPERTY_NAME;
import static nl.knaw.huygens.timbuctoo.storage.graph.SystemRelationType.VERSION_OF;
import static nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.VertexFields.VERTEX_TYPE;
import nl.knaw.huygens.timbuctoo.config.TypeNames;
import nl.knaw.huygens.timbuctoo.model.Entity;

import com.google.common.base.Predicate;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Vertex;

public class TinkerpopLowLevelAPI {

  private final Graph db;

  public TinkerpopLowLevelAPI(Graph db) {
    this.db = db;
  }

  public <T extends Entity> Vertex getLatestVertexById(Class<T> type, String id) {
    Iterable<Vertex> foundVertices = db.query() //
        .has(VERTEX_TYPE, TypeNames.getInternalName(type)) //
        .has(ID_PROPERTY_NAME, id) //
        .vertices();

    IsLatestVersionOfVertex isLatestVersionOfVertex = new IsLatestVersionOfVertex();

    for (Vertex vertex : foundVertices) {
      if (isLatestVersionOfVertex.apply(vertex)) {
        return vertex;
      }
    }

    return null;
  }

  private static final class IsLatestVersionOfVertex implements Predicate<Vertex> {
    @Override
    public boolean apply(Vertex vertex) {
      Iterable<Edge> incomingVersionOfEdges = vertex.getEdges(Direction.IN, VERSION_OF.name());
      return incomingVersionOfEdges == null || !incomingVersionOfEdges.iterator().hasNext();
    }
  }

}
