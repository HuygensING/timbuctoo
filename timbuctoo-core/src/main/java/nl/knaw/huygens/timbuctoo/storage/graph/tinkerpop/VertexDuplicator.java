package nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop;

import static nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.ElementHelper.sourceOfEdge;
import static nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.ElementHelper.targetOfEdge;

import java.util.Iterator;
import java.util.Objects;

import nl.knaw.huygens.timbuctoo.storage.graph.SystemRelationType;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Vertex;

public class VertexDuplicator {

  private static final String VERSION_OF_LABEL = SystemRelationType.VERSION_OF.name();
  private Graph db;

  public VertexDuplicator(Graph db) {
    this.db = db;

  }

  public Vertex duplicate(Vertex vertexToDuplicate) {
    Vertex duplicate = db.addVertex(null);

    duplicateProperties(vertexToDuplicate, duplicate);

    duplicateOutgoingEdges(vertexToDuplicate, duplicate);

    duplicateIncomingEdges(vertexToDuplicate, duplicate);

    vertexToDuplicate.addEdge(VERSION_OF_LABEL, duplicate);

    return duplicate;
  }

  private void duplicateIncomingEdges(Vertex vertexToDuplicate, Vertex duplicate) {
    for (Iterator<Edge> iterator = vertexToDuplicate.getEdges(Direction.IN).iterator(); iterator.hasNext();) {
      Edge edge = iterator.next();

      if (!isVersionOfEdge(edge)) {
        sourceOfEdge(edge).addEdge(edge.getLabel(), duplicate);
      }

      edge.remove();
    }
  }

  private void duplicateOutgoingEdges(Vertex vertexToDuplicate, Vertex duplicate) {
    for (Iterator<Edge> iterator = vertexToDuplicate.getEdges(Direction.OUT).iterator(); iterator.hasNext();) {
      Edge edge = iterator.next();

      if (!isVersionOfEdge(edge)) {
        duplicate.addEdge(edge.getLabel(), targetOfEdge(edge));
      }

      edge.remove();
    }
  }

  private boolean isVersionOfEdge(Edge edge) {
    return Objects.equals(edge.getLabel(), VERSION_OF_LABEL);
  }

  private void duplicateProperties(Vertex vertexToDuplicate, Vertex duplicate) {
    for (String key : vertexToDuplicate.getPropertyKeys()) {
      duplicate.setProperty(key, vertexToDuplicate.getProperty(key));
    }
  }

}
