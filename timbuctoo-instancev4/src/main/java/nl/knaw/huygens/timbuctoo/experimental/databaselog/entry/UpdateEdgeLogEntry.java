package nl.knaw.huygens.timbuctoo.experimental.databaselog.entry;

import nl.knaw.huygens.timbuctoo.experimental.databaselog.DatabaseLog;
import nl.knaw.huygens.timbuctoo.experimental.databaselog.EdgeLogEntry;
import org.apache.tinkerpop.gremlin.structure.Edge;

import java.util.Objects;

class UpdateEdgeLogEntry extends EdgeLogEntry {
  private final Edge edge;
  private final Edge prevEdge;
  private final PropertyUpdater propertyUpdater;

  public UpdateEdgeLogEntry(Edge edge, Long timestamp, String id, Edge prevEdge) {
    super(timestamp, id);
    this.edge = edge;
    this.prevEdge = prevEdge;
    this.propertyUpdater = new PropertyUpdater(edge, prevEdge);
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
