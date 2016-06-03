package nl.knaw.huygens.timbuctoo.experimental.databaselog.entry;

import nl.knaw.huygens.timbuctoo.experimental.databaselog.DatabaseLog;
import nl.knaw.huygens.timbuctoo.experimental.databaselog.EdgeLogEntry;
import org.apache.tinkerpop.gremlin.structure.Edge;

class CreateEdgeLogEntry extends EdgeLogEntry {
  private final Edge edge;

  public CreateEdgeLogEntry(Edge edge, Long timestamp, String id) {
    super(timestamp, id);
    this.edge = edge;
  }

  @Override
  public void appendToLog(DatabaseLog dbLog) {
    dbLog.newEdge(edge);

    edge.properties().forEachRemaining(dbLog::newProperty);
  }
}
