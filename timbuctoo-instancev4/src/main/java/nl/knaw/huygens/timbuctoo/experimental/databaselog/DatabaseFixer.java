package nl.knaw.huygens.timbuctoo.experimental.databaselog;

import com.fasterxml.jackson.databind.ObjectMapper;
import nl.knaw.huygens.timbuctoo.crud.EdgeManipulator;
import nl.knaw.huygens.timbuctoo.model.Change;
import nl.knaw.huygens.timbuctoo.server.GraphWrapper;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Property;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.structure.VertexProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Objects;
import java.util.Optional;


public class DatabaseFixer {
  public static final Logger LOG = LoggerFactory.getLogger(DatabaseLog.class);
  private final GraphWrapper graphWrapper;
  private final ObjectMapper objectMapper;

  public DatabaseFixer(GraphWrapper graphWrapper) {
    this.graphWrapper = graphWrapper;
    objectMapper = new ObjectMapper();
  }

  public void fix() {
    Graph graph = graphWrapper
      .getGraph();
    fixEdges(graph);
    fixVertices(graph);
  }

  private void fixVertices(Graph graph) {
    GraphTraversal<Vertex, Vertex> results = graph.traversal()
                                                  .V().has("modified")
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
    for (; results.hasNext(); ) {
      addMissingVertexVersions(results.next());
    }
  }

  private void addMissingVertexVersions(Vertex vertex) {
    int rev = vertex.value("rev");
    if (rev > 1) {
      Iterator<Vertex> previousVersions = vertex.vertices(Direction.IN, "VERSION_OF");
      if (previousVersions.hasNext()) {
        addMissingVertexVersions(previousVersions.next());
      } else {
        Vertex duplicate = vertex.graph().addVertex();
        duplicate.addEdge("VERSION_OF", vertex);
        for (Iterator<VertexProperty<Object>> properties = vertex.properties(); properties.hasNext(); ) {
          VertexProperty<Object> property = properties.next();
          if (Objects.equals(property.key(), "isLatest")) {
            duplicate.property(property.key(), false);
          } else if (Objects.equals(property.key(), "rev")) {
            duplicate.property(property.key(), rev - 1);
          } else if (Objects.equals(property.key(), "modified")) {
            duplicate.property("modified", vertex.value("created"));
          } else {
            duplicate.property(property.key(), property.value());
          }
        }
        addMissingVertexVersions(duplicate);
      }
    }
  }

  private void fixEdges(Graph graph) {
    graph.traversal()
         .E().has("modified")
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
         }).forEachRemaining(this::addMissingEdgeVersions);
  }


  private void addMissingEdgeVersions(Edge edge) {
    int rev = edge.value("rev");
    if (rev > 1) {
      Optional<Edge> previousVersion = getPreviousVersion(edge);
      if (previousVersion.isPresent()) {
        addMissingEdgeVersions(previousVersion.get());
      } else {
        // FIXME stop using EdgeManipulator
        Edge duplicate = EdgeManipulator.duplicateEdge(edge);
        for (Iterator<Property<Object>> properties = edge.properties(); properties.hasNext(); ) {
          Property<Object> property = properties.next();
          if (Objects.equals(property.key(), "isLatest")) {
            duplicate.property("isLatest", false);
          } else if (Objects.equals(property.key(), "rev")) {
            duplicate.property("rev", rev - 1);
          } else if (Objects.equals(property.key(), "modified")) {
            duplicate.property("modified", edge.value("created"));
          } else if (property.key().endsWith("_accepted")) {
            duplicate.property(property.key(), true);
          }
        }
        addMissingEdgeVersions(duplicate);
      }
    }
  }

  public Optional<Edge> getPreviousVersion(Edge edge) {
    Integer rev = edge.<Integer>value("rev");
    String id = edge.value("tim_id");

    Optional<Edge> prev = Optional.empty();
    for (Iterator<Edge> edges = edge.outVertex().edges(Direction.OUT, edge.label()); edges.hasNext(); ) {
      Edge next = edges.next();
      if (next.<Integer>value("rev") == (rev - 1)) {
        prev = Optional.of(next);
      }
    }

    return prev;
  }

  private boolean hasPreviousEdgeVersion(Edge edge) {
    Optional<Edge> prev = Optional.empty();
    int rev = edge.value("rev");
    for (Iterator<Edge> edges = edge.outVertex().edges(Direction.OUT, edge.label()); edges.hasNext(); ) {
      Edge next = edges.next();
      if (next.<Integer>value("rev") == (rev - 1)) {
        prev = Optional.of(next);
      }
    }

    return prev.isPresent();
  }


  private long getTimestampFromChangeString(String changeString) throws IOException {
    return objectMapper.readValue(changeString, Change.class).getTimeStamp();
  }
}
