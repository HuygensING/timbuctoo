package nl.knaw.huygens.timbuctoo.experimental.databaselog.entry;

import nl.knaw.huygens.timbuctoo.experimental.databaselog.DatabaseLog;
import nl.knaw.huygens.timbuctoo.experimental.databaselog.EdgeLogEntryAdder;
import nl.knaw.huygens.timbuctoo.experimental.databaselog.VertexLogEntry;
import nl.knaw.huygens.timbuctoo.util.StreamIterator;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.util.Iterator;
import java.util.Objects;
import java.util.Set;


public class CreateVertexLogEntry implements VertexLogEntry {
  private final Vertex vertex;
  private final Set<String> propertiesToIgnore;

  public CreateVertexLogEntry(Vertex vertex) {
    this(vertex, PropertyHelper.SYSTEM_PROPERTIES);
  }

  CreateVertexLogEntry(Vertex vertex, Set<String> propertiesToIgnore) {
    this.vertex = vertex;
    this.propertiesToIgnore = propertiesToIgnore;
  }

  @Override
  public void appendToLog(DatabaseLog dbLog) {
    dbLog.newVertex(vertex);

    StreamIterator.stream(vertex.properties())
                  .filter(property -> !propertiesToIgnore.contains(property.key()))
                  .forEach(dbLog::newProperty);
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
