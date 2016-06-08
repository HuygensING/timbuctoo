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

public class DatabaseLogGenerator {


  public static final Logger LOG = LoggerFactory.getLogger(DatabaseLogGenerator.class);
  private final GraphWrapper graphWrapper;
  private final LogEntryFactory logEntryFactory;
  private final EdgeLogEntryAdder edgeLogEntryAdder;
  private final ObjectMapper objectMapper;

  public DatabaseLogGenerator(GraphWrapper graphWrapper) {
    this(graphWrapper, new LogEntryFactory());
  }

  private DatabaseLogGenerator(GraphWrapper graphWrapper, LogEntryFactory logEntryFactory) {
    this(graphWrapper, logEntryFactory, new EdgeLogEntryAdder());
  }

  DatabaseLogGenerator(GraphWrapper graphWrapper, LogEntryFactory logEntryFactory,
                       EdgeLogEntryAdder edgeLogEntryAdder) {
    this.graphWrapper = graphWrapper;
    this.logEntryFactory = logEntryFactory;
    this.edgeLogEntryAdder = edgeLogEntryAdder;
    objectMapper = new ObjectMapper();
  }

  public void generate() {
    DatabaseLog databaseLog = new DatabaseLog();

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
