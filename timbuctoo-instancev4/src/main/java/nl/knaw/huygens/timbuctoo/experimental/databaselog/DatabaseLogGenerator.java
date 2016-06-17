package nl.knaw.huygens.timbuctoo.experimental.databaselog;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import nl.knaw.huygens.timbuctoo.experimental.databaselog.entry.LogEntryFactory;
import nl.knaw.huygens.timbuctoo.model.Change;
import nl.knaw.huygens.timbuctoo.server.GraphWrapper;
import org.apache.tinkerpop.gremlin.neo4j.process.traversal.LabelP;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Element;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;

/**
 * <p>
 * Currently (as of 2016-06-13) the Timbuctoo database contains all versions of all entities and all versions of all
 * relations as one interlinked graph. The old versions are filtered out while querying. We want to simplify the
 * database model and the code that uses the database directly, without losing information. Therefore we're moving the
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
  private final DatabaseLog databaseLog;
  private final ObjectMapper objectMapper;

  public DatabaseLogGenerator(GraphWrapper graphWrapper) {
    this(graphWrapper, new LogEntryFactory());
  }

  DatabaseLogGenerator(GraphWrapper graphWrapper, LogEntryFactory logEntryFactory) {
    this(graphWrapper, logEntryFactory, new DatabaseLog());
  }

  DatabaseLogGenerator(GraphWrapper graphWrapper, LogEntryFactory logEntryFactory,
                       DatabaseLog databaseLog) {
    this.graphWrapper = graphWrapper;
    this.logEntryFactory = logEntryFactory;
    this.databaseLog = databaseLog;
    this.objectMapper = new ObjectMapper();
  }

  public void generate() {
    long curTimestamp = 0L;

    @SuppressWarnings("unchecked") // union warns about unchecked.
      GraphTraversal<Vertex, ? extends Element> results = graphWrapper
      .getGraph()
      .traversal()
      .V().has("modified")
      .not(__.has(T.label, LabelP.of("searchresult"))) // ignore search results
      .union(__.identity(), __.outE().has("modified"))
      .dedup().by(__.valueMap("rev", "tim_id")) // add each version once
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
      });

    List<Edge> edges = Lists.newArrayList();
    List<Vertex> vertices = Lists.newArrayList();
    for (; results.hasNext(); ) {
      Element element = results.next();
      String modifiedString = element.value("modified");
      try {
        long modifiedTimestamp = getTimestampFromChangeString(modifiedString);
        if (curTimestamp != modifiedTimestamp) {
          vertices.forEach(vertex -> logEntryFactory.createForVertex(vertex).appendToLog(databaseLog));
          vertices.clear();
          edges.forEach(edge -> logEntryFactory.createForEdge(edge).appendToLog(databaseLog));
          edges.clear();
          curTimestamp = modifiedTimestamp;
        }

        if (element instanceof Vertex) {
          vertices.add((Vertex) element);
        } else if (element instanceof Edge) {
          edges.add((Edge) element);
        } else {
          LOG.error("Element type {} not expected.", element.getClass());
        }

      } catch (IOException e) {
        LOG.error("Change '{}' could nog be parsed.", modifiedString);
      }
    }
    // add remaining
    vertices.forEach(vertex -> logEntryFactory.createForVertex(vertex).appendToLog(databaseLog));
    vertices.clear();
    edges.forEach(edge -> logEntryFactory.createForEdge(edge).appendToLog(databaseLog));
    edges.clear();
  }

  private long getTimestampFromChangeString(String changeString) throws IOException {
    return objectMapper.readValue(changeString, Change.class).getTimeStamp();
  }

  private static class IdRevComparator implements Comparator<Element> {
    @Override
    public int compare(Element o1, Element o2) {
      String id1 = o1.<String>property("tim_id").orElse("");
      Integer rev1 = o1.<Integer>property("rev").orElse(0);
      String id2 = o2.<String>property("tim_id").orElse("");
      Integer rev2 = o2.<Integer>property("rev").orElse(0);

      return id1.equals(id2) ? rev1.compareTo(rev2) : id1.compareTo(id2);
    }
  }
}
