package nl.knaw.huygens.timbuctoo.experimental.databaselog.entry;

import nl.knaw.huygens.timbuctoo.experimental.databaselog.DatabaseLog;
import nl.knaw.huygens.timbuctoo.experimental.databaselog.EdgeLogEntry;
import nl.knaw.huygens.timbuctoo.util.StreamIterator;
import org.apache.tinkerpop.gremlin.structure.Edge;

import java.util.Set;

class CreateEdgeLogEntry extends EdgeLogEntry {
  private final Edge edge;
  private final Set<String> propertiesToIgnore;

  public CreateEdgeLogEntry(Edge edge, Long timestamp, String id) {
    this(edge, timestamp, id, PropertyHelper.SYSTEM_PROPERTIES);
  }

  public CreateEdgeLogEntry(Edge edge, Long timestamp, String id, Set<String> propertiesToIgnore) {
    super(timestamp, id);
    this.edge = edge;
    this.propertiesToIgnore = propertiesToIgnore;
  }

  @Override
  public void appendToLog(DatabaseLog dbLog) {
    dbLog.newEdge(edge);

    StreamIterator.stream(edge.properties())
                  .filter(property -> !propertiesToIgnore.contains(property.key()))
                  .forEach(dbLog::newProperty);
  }
}
