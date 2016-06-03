package nl.knaw.huygens.timbuctoo.experimental.databaselog.entry;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import nl.knaw.huygens.timbuctoo.experimental.databaselog.EdgeLogEntry;
import nl.knaw.huygens.timbuctoo.experimental.databaselog.VertexLogEntry;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Property;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.util.Iterator;

public class LogEntryFactory {

  private final ObjectMapper objectMapper;

  public LogEntryFactory() {
    objectMapper = new ObjectMapper();
  }

  public VertexLogEntry createForVertex(Vertex vertex) {
    Iterator<Vertex> previous = vertex.vertices(Direction.IN, "VERSION_OF");
    if (previous.hasNext()) {
      return new UpdateVertexLogEntry(vertex, previous.next());
    }
    return new CreateVertexLogEntry(vertex);
  }

  public EdgeLogEntry createForEdge(Edge edge, long modifiedTimeStamp, String id) {
    Integer rev = edge.<Integer>value("rev");
    if (rev > 1) {
      Edge prevEdge = new NoOpEdge();
      for (Iterator<Edge> it = edge.outVertex().edges(Direction.OUT, edge.label()); it.hasNext(); ) {
        Edge next = it.next();
        if (next.<Integer>value("rev") == (rev - 1)) {
          prevEdge = next;
          break;
        }
      }

      return new UpdateEdgeLogEntry(edge, modifiedTimeStamp, id, prevEdge);
    }
    return new CreateEdgeLogEntry(edge, modifiedTimeStamp, id);
  }

  private static class NoOpEdge implements Edge {
    @Override
    public Iterator<Vertex> vertices(Direction direction) {
      return Lists.<Vertex>newArrayList().iterator();
    }

    @Override
    public <V> Iterator<Property<V>> properties(String... propertyKeys) {
      return Lists.<Property<V>>newArrayList().iterator();
    }

    @Override
    public Object id() {
      return "";
    }

    @Override
    public String label() {
      return "";
    }

    @Override
    public Graph graph() {
      return null;
    }

    @Override
    public <V> Property<V> property(String key, V value) {
      return null;
    }

    @Override
    public void remove() {

    }
  }
}
