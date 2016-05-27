package nl.knaw.huygens.timbuctoo.experimental.databaselog.entry;

import nl.knaw.huygens.timbuctoo.experimental.databaselog.VertexLogEntry;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.util.Iterator;

public class LogEntryFactory {

  public VertexLogEntry createForVertex(Vertex vertex) {
    Iterator<Vertex> previous = vertex.vertices(Direction.IN, "VERSION_OF");
    if (previous.hasNext()) {
      return new UpdateVertexLogEntry(vertex, previous.next());
    }
    return new CreateVertexLogEntry(vertex);
  }
}
