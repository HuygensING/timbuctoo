package nl.knaw.huygens.timbuctoo.experimental.databaselog.entry;

import nl.knaw.huygens.timbuctoo.experimental.databaselog.DatabaseLog;
import nl.knaw.huygens.timbuctoo.experimental.databaselog.EdgeLogEntryAdder;
import nl.knaw.huygens.timbuctoo.experimental.databaselog.LogEntry;
import nl.knaw.huygens.timbuctoo.experimental.databaselog.VertexLogEntry;
import nl.knaw.huygens.timbuctoo.util.StreamIterator;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.Objects;
import java.util.Set;


class CreateVertexLogEntry implements VertexLogEntry {
  public static final Logger LOG = LoggerFactory.getLogger(CreateVertexLogEntry.class);
  private final Vertex vertex;
  private final Set<String> propertiesToIgnore;
  private final LogEntryFactory logEntryFactory;

  public CreateVertexLogEntry(Vertex vertex, LogEntryFactory logEntryFactory) {
    this(vertex, LogEntry.SYSTEM_PROPERTIES, logEntryFactory);
  }


  CreateVertexLogEntry(Vertex vertex, Set<String> propertiesToIgnore, LogEntryFactory logEntryFactory) {
    this.vertex = vertex;
    this.propertiesToIgnore = propertiesToIgnore;
    this.logEntryFactory = logEntryFactory;
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
        try {
          edgeLogEntryAdder.entryFor(logEntryFactory.createForEdge(edge));
        } catch (IllegalArgumentException e) {
          LOG.error(e.getMessage());
          LOG.error("exception thrown", e);
        }
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
