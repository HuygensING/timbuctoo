package nl.knaw.huygens.timbuctoo.experimental.databaselog.entry;

import nl.knaw.huygens.timbuctoo.experimental.databaselog.VertexLogEntry;
import org.apache.tinkerpop.gremlin.structure.Vertex;

public class LogEntryFactory {

  public VertexLogEntry createForVertex(Vertex vertex) {
    return new CreateVertexLogEntry(vertex);
  }
}
