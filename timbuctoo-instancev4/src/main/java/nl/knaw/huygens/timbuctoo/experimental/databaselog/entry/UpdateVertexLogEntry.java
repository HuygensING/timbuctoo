package nl.knaw.huygens.timbuctoo.experimental.databaselog.entry;

import nl.knaw.huygens.timbuctoo.experimental.databaselog.DatabaseLog;
import nl.knaw.huygens.timbuctoo.experimental.databaselog.EdgeLogEntryAdder;
import nl.knaw.huygens.timbuctoo.experimental.databaselog.VertexLogEntry;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.util.Objects;

public class UpdateVertexLogEntry implements VertexLogEntry {
  private final Vertex vertex;
  private final Vertex previous;
  private final PropertyUpdater propertyUpdater;


  public UpdateVertexLogEntry(Vertex vertex, Vertex previousVersion) {
    this.vertex = vertex;
    this.previous = previousVersion;
    this.propertyUpdater = new PropertyUpdater(vertex, previousVersion);
  }

  @Override
  public void appendToLog(DatabaseLog dbLog) {
    if (Objects.equals(vertex.value("rev"), previous.value("rev"))) {
      return;
    }

    dbLog.updateVertex(vertex);
    propertyUpdater.updateProperties(dbLog);
  }

  @Override
  public void addEdgeLogEntriesTo(EdgeLogEntryAdder edgeLogEntryAdder) {
    /*
     * Do not add EdgeLogEntries. The CreateVertexLogEntry will add all the EdgeLogEntries tot he EdgeLogEntryAdder.
     * The EdgeLogEntryAdder will determine when the EdgeLogEntries should be added to the DatabaseLog.
     */

  }
}
