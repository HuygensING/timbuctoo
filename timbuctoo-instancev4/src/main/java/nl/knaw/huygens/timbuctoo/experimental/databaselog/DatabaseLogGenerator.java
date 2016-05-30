package nl.knaw.huygens.timbuctoo.experimental.databaselog;


import com.fasterxml.jackson.databind.ObjectMapper;
import nl.knaw.huygens.timbuctoo.experimental.databaselog.entry.LogEntryFactory;
import nl.knaw.huygens.timbuctoo.model.Change;
import nl.knaw.huygens.timbuctoo.server.GraphWrapper;
import org.apache.tinkerpop.gremlin.neo4j.process.traversal.LabelP;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.T;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Comparator;

public class DatabaseLogGenerator {


  public static final Logger LOG = LoggerFactory.getLogger(DatabaseLogGenerator.class);
  private final GraphWrapper graphWrapper;
  private final LogEntryFactory logEntryFactory;
  private final ObjectMapper objectMapper;

  public DatabaseLogGenerator(GraphWrapper graphWrapper) {
    this(graphWrapper, new LogEntryFactory());
  }

  DatabaseLogGenerator(GraphWrapper graphWrapper, LogEntryFactory logEntryFactory) {
    this.graphWrapper = graphWrapper;
    this.logEntryFactory = logEntryFactory;
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
                    Change change1 =
                      objectMapper.readValue(o1, Change.class);
                    Change change2 =
                      objectMapper.readValue(o2, Change.class);

                    return Long
                      .compare(change1.getTimeStamp(),
                        change2.getTimeStamp());
                  } catch (IOException e) {
                    LOG.error("Cannot convert change", e);
                    LOG.error("Change 1 '{}'", o1);
                    LOG.error("Change 2 '{}'", o2);
                    return 0;
                  }
                })
                .forEachRemaining(vertex -> logEntryFactory.createForVertex(vertex).appendToLog(databaseLog));
  }
}
