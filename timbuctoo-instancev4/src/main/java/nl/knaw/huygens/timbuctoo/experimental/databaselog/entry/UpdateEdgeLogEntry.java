package nl.knaw.huygens.timbuctoo.experimental.databaselog.entry;

import nl.knaw.huygens.timbuctoo.experimental.databaselog.DatabaseLog;
import nl.knaw.huygens.timbuctoo.experimental.databaselog.EdgeLogEntry;
import org.apache.tinkerpop.gremlin.structure.Edge;

import java.util.Objects;
import java.util.Set;

class UpdateEdgeLogEntry extends EdgeLogEntry {
  private final Edge edge;
  private final Edge prevEdge;
  private final PropertyUpdater propertyUpdater;

  public UpdateEdgeLogEntry(Edge edge, Long timestamp, String id, Edge prevEdge) {
    this(edge, timestamp, id, prevEdge, PropertyHelper.SYSTEM_PROPERTIES);

  }

  UpdateEdgeLogEntry(Edge edge, long timestamp, String id, Edge prevEdge, Set<String> propertiesToIgnore) {
    super(timestamp, id);
    this.edge = edge;
    this.prevEdge = prevEdge;
    this.propertyUpdater = new PropertyUpdater(edge, prevEdge, propertiesToIgnore);
  }

  @Override
  public void appendToLog(DatabaseLog dbLog) {
    if (Objects.equals(edge.value("rev"), prevEdge.value("rev"))) {
      return;
    }

    dbLog.updateEdge(edge);
    propertyUpdater.updateProperties(dbLog);
  }

}
