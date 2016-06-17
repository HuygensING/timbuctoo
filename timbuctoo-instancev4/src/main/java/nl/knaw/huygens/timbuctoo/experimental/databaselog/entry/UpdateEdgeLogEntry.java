package nl.knaw.huygens.timbuctoo.experimental.databaselog.entry;

import nl.knaw.huygens.timbuctoo.experimental.databaselog.DatabaseLog;
import nl.knaw.huygens.timbuctoo.experimental.databaselog.LogEntry;
import org.apache.tinkerpop.gremlin.structure.Edge;

import java.util.Set;

class UpdateEdgeLogEntry implements LogEntry {
  private final Edge edge;
  private final Edge prevEdge;
  private final PropertyUpdater propertyUpdater;

  public UpdateEdgeLogEntry(Edge edge, Edge prevEdge) {
    this(edge, prevEdge, LogEntry.SYSTEM_PROPERTIES);

  }

  UpdateEdgeLogEntry(Edge edge, Edge prevEdge, Set<String> propertiesToIgnore) {
    this.edge = edge;
    this.prevEdge = prevEdge;
    this.propertyUpdater = new PropertyUpdater(edge, prevEdge, propertiesToIgnore);
  }

  @Override
  public void appendToLog(DatabaseLog dbLog) {
    dbLog.updateEdge(edge);
    propertyUpdater.updateProperties(dbLog);
  }

}
