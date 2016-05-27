package nl.knaw.huygens.timbuctoo.experimental.databaselog.entry;

import nl.knaw.huygens.timbuctoo.experimental.databaselog.DatabaseLog;
import nl.knaw.huygens.timbuctoo.experimental.databaselog.VertexLogEntry;
import org.apache.tinkerpop.gremlin.structure.Vertex;

public class CreateVertexLogEntry implements VertexLogEntry {
  private final Vertex vertex;

  public CreateVertexLogEntry(Vertex vertex) {
    this.vertex = vertex;
  }

  @Override
  public void appendToLog(DatabaseLog dbLog) {
    dbLog.newVertex(vertex);

    vertex.properties().forEachRemaining(dbLog::newProperty);
  }
}
