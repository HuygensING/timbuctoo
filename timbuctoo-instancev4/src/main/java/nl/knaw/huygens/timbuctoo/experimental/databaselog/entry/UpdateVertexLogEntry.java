package nl.knaw.huygens.timbuctoo.experimental.databaselog.entry;

import nl.knaw.huygens.timbuctoo.experimental.databaselog.DatabaseLog;
import nl.knaw.huygens.timbuctoo.experimental.databaselog.LogEntry;
import nl.knaw.huygens.timbuctoo.experimental.databaselog.VertexLogEntry;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.util.Set;

class UpdateVertexLogEntry implements VertexLogEntry {
  private final Vertex vertex;
  private final Vertex previous;
  private final PropertyUpdater propertyUpdater;


  public UpdateVertexLogEntry(Vertex vertex, Vertex previousVersion) {
    this(vertex, previousVersion, LogEntry.SYSTEM_PROPERTIES);
  }

  UpdateVertexLogEntry(Vertex vertex, Vertex previousVersion, Set<String> propertiesToIgnore) {
    this.vertex = vertex;
    this.previous = previousVersion;
    this.propertyUpdater = new PropertyUpdater(vertex, previousVersion, propertiesToIgnore);
  }

  @Override
  public void appendToLog(DatabaseLog dbLog) {
    dbLog.updateVertex(vertex);
    propertyUpdater.updateProperties(dbLog);
  }
}
