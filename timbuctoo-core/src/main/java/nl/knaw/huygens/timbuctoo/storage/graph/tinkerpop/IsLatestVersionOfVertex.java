package nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop;

import static nl.knaw.huygens.timbuctoo.storage.graph.SystemRelationType.VERSION_OF;

import com.google.common.base.Predicate;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;

public final class IsLatestVersionOfVertex implements Predicate<Vertex> {
  @Override
  public boolean apply(Vertex vertex) {
    Iterable<Edge> outgoingVersionOfEdges = vertex.getEdges(Direction.OUT, VERSION_OF.name());
    return outgoingVersionOfEdges == null || !outgoingVersionOfEdges.iterator().hasNext();
  }
}