package nl.knaw.huygens.timbuctoo.experimental.databaselog;


import com.fasterxml.jackson.databind.ObjectMapper;
import nl.knaw.huygens.timbuctoo.experimental.databaselog.entry.LogEntryFactory;
import nl.knaw.huygens.timbuctoo.model.Change;
import nl.knaw.huygens.timbuctoo.server.GraphWrapper;
import org.apache.tinkerpop.gremlin.neo4j.process.traversal.LabelP;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Comparator;

/**
 * <p>
 * Currently (as of 2016-06-13) the Timbuctoo database contains all versions of all entities and all versions of all
 * relations as one interlinked graph. The old versions are filtered out while querying. We want to simplify the
 * database model and the code that uses the database directly, without loosing information. Therefore we're moving the
 * history to a separate databaseLog.
 * </p>
 * <p>
 * This class creates that log from the current format.
 * </p>
 * <p>
 * It sorts all vertices (i.e. versions of entities) as one big chronological list and then iterates over it, creating
 * the log entries and calling their addToLog method. //FIXME an object that is immediately removed?
 * </p>
 * <p>
 * A complicating factor is that many vertices and edges share the exact same modified time. So we don't know the exact
 * order in which they were added. For vertices we do not to care about the exact order, however an edge between two
 * vertices can only be added once those vertices exist (obviously).
 * </p>
 * <p>
 * Our solution is: for a given modified-time we first add {@link LogEntry}s for all vertices and then for all edges.
 * </p>
 * <p>
 * After creating the {@link LogEntry} objects it calls the {@link DatabaseLog} to write the {@link LogEntry}s. At this
 * moment they are written as text to a file, we will later replace this with a {@link DatabaseLog} implementation that
 * creates log vertices in the database.
 * </p>
 */
public class DatabaseLogGenerator {
  public static final Logger LOG = LoggerFactory.getLogger(DatabaseLogGenerator.class);
  private final GraphWrapper graphWrapper;
  private final LogEntryFactory logEntryFactory;
  private final EdgeLogEntryAdder edgeLogEntryAdder;
  private final DatabaseLog databaseLog;
  private final ObjectMapper objectMapper;

  public DatabaseLogGenerator(GraphWrapper graphWrapper) {
    this(graphWrapper, new LogEntryFactory());
  }

  private DatabaseLogGenerator(GraphWrapper graphWrapper, LogEntryFactory logEntryFactory) {
    this(graphWrapper, logEntryFactory, new EdgeLogEntryAdder());
  }

  DatabaseLogGenerator(GraphWrapper graphWrapper, LogEntryFactory logEntryFactory,
                       EdgeLogEntryAdder edgeLogEntryAdder) {
    this(graphWrapper, logEntryFactory, edgeLogEntryAdder, new DatabaseLog());

  }

  DatabaseLogGenerator(GraphWrapper graphWrapper, LogEntryFactory logEntryFactory,
                              EdgeLogEntryAdder edgeLogEntryAdder, DatabaseLog databaseLog) {
    this.graphWrapper = graphWrapper;
    this.logEntryFactory = logEntryFactory;
    this.edgeLogEntryAdder = edgeLogEntryAdder;
    this.databaseLog = databaseLog;
    this.objectMapper = new ObjectMapper();
  }

  public void generate() {
    graphWrapper.getGraph().traversal()
                .V().has("modified")
                .not(__.has(T.label, LabelP.of("searchresult"))) // ignore search results
                .dedup()
                .order()
                .by("modified", (Comparator<String>) (o1, o2) -> {
                  try {
                    long timeStamp1 = getTimestampFromChangeString(o1);
                    long timeStamp2 = getTimestampFromChangeString(o2);
                    return Long.compare(timeStamp1, timeStamp2);
                  } catch (IOException e) {
                    LOG.error("Cannot convert change", e);
                    LOG.error("Change 1 '{}'", o1);
                    LOG.error("Change 2 '{}'", o2);
                    return 0;
                  }
                })
                .forEachRemaining(vertex -> {
                  // Because both vertices most exist, the edges can only be added after the last vertex with a
                  // certain timestamp. This is before the the vertex with a timestamp after the edge timestamp.
                  appendEdgeChangedBeforeVertexToLog(databaseLog, vertex);
                  appendVertexToLog(databaseLog, vertex);
                });
    edgeLogEntryAdder.appendRemaining(databaseLog);
  }

  private void appendEdgeChangedBeforeVertexToLog(DatabaseLog databaseLog, Vertex vertex) {
    try {
      String modifiedString = vertex.value("modified");
      long modifiedTimestamp = getTimestampFromChangeString(modifiedString);
      edgeLogEntryAdder.appendEdgesToLog(databaseLog, modifiedTimestamp);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void appendVertexToLog(DatabaseLog databaseLog, Vertex vertex) {
    VertexLogEntry vertexLogEntry = logEntryFactory.createForVertex(vertex);
    vertexLogEntry.appendToLog(databaseLog);
    vertexLogEntry.addEdgeLogEntriesTo(edgeLogEntryAdder);
  }

  private long getTimestampFromChangeString(String changeString) throws IOException {
    return objectMapper.readValue(changeString, Change.class).getTimeStamp();
  }
}
