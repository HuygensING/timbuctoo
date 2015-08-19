package nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop;

import com.google.common.base.Predicate;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static nl.knaw.huygens.timbuctoo.storage.graph.SystemRelationType.VERSION_OF;

public final class IsLatestVersionOfVertex implements Predicate<Vertex> {

  public static final Logger LOG = LoggerFactory.getLogger(IsLatestVersionOfVertex.class);

  @Override
  public boolean apply(Vertex vertex) {
    Iterable<Edge> outgoingVersionOfEdges = vertex.getEdges(Direction.OUT, VERSION_OF.name());
    boolean isLatest = outgoingVersionOfEdges == null || !outgoingVersionOfEdges.iterator().hasNext();
    return isLatest;
  }
}
