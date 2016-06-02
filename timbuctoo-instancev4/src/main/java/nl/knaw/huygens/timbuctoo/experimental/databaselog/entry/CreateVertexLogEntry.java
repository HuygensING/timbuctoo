package nl.knaw.huygens.timbuctoo.experimental.databaselog.entry;

import nl.knaw.huygens.timbuctoo.experimental.databaselog.DatabaseLog;
import nl.knaw.huygens.timbuctoo.experimental.databaselog.EdgeLogEntryAdder;
import nl.knaw.huygens.timbuctoo.experimental.databaselog.VertexLogEntry;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.util.Iterator;
import java.util.Objects;


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

  @Override
  public void addEdgeLogEntriesTo(EdgeLogEntryAdder edgeLogEntryAdder) {
    Vertex latest = getLatestVersion(vertex);
    // Only add OUT edges, to make sure no duplicates are added.
    latest.edges(Direction.OUT).forEachRemaining(edge -> {
      if (!Objects.equals(edge.label(), "VERSION_OF")) {
        edgeLogEntryAdder.entryFor(edge);
      }
    });
  }

  private Vertex getLatestVersion(Vertex vertex) {
    Iterator<Vertex> nextVersions = vertex.vertices(Direction.OUT, "VERSION_OF");
    if (nextVersions.hasNext()) {
      return getLatestVersion(nextVersions.next());
    }
    return vertex;
  }
}
