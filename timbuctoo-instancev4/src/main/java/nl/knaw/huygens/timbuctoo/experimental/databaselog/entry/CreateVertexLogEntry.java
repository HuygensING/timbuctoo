package nl.knaw.huygens.timbuctoo.experimental.databaselog.entry;

import nl.knaw.huygens.timbuctoo.experimental.databaselog.DatabaseLog;
import nl.knaw.huygens.timbuctoo.experimental.databaselog.LogEntry;
import nl.knaw.huygens.timbuctoo.experimental.databaselog.VertexLogEntry;
import nl.knaw.huygens.timbuctoo.util.StreamIterator;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;


class CreateVertexLogEntry implements VertexLogEntry {
  public static final Logger LOG = LoggerFactory.getLogger(CreateVertexLogEntry.class);
  private final Vertex vertex;
  private final Set<String> propertiesToIgnore;

  public CreateVertexLogEntry(Vertex vertex) {
    this(vertex, LogEntry.SYSTEM_PROPERTIES);
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

}
