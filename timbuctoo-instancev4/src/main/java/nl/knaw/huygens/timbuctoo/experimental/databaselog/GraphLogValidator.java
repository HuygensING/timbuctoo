package nl.knaw.huygens.timbuctoo.experimental.databaselog;

import com.google.common.collect.Sets;
import nl.knaw.huygens.timbuctoo.server.GraphWrapper;
import nl.knaw.huygens.timbuctoo.util.Tuple;
import org.apache.tinkerpop.gremlin.neo4j.process.traversal.LabelP;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.Element;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.has;

public class GraphLogValidator {
  public static final Logger LOG = LoggerFactory.getLogger(GraphLogValidator.class);
  private final GraphWrapper graphWrapper;

  public GraphLogValidator(GraphWrapper graphWrapper) {
    this.graphWrapper = graphWrapper;
  }

  @SuppressWarnings("unchecked")
  public Set<Element> validate() {
    LoggerFactory.getLogger(GraphLogValidator.class).info("Start Vertex validation");
    Set<Element> elements = Sets.newHashSet();

    Graph graph = graphWrapper.getGraph();

    Map<Object, Object> next = graph.traversal().V()
                                    .has("tim_id")
                                    .has("rev")
                                    .not(has(T.label, LabelP.of("searchresult"))) // ignore search results
                                    // ignore the edge entries, because they give false positives
                                    .not(has(T.label, LabelP.of("createEdgeEntry")))
                                    .not(has(T.label, LabelP.of("updateEdgeEntry")))
                                    .group().<Map<Object, List<Vertex>>>by(__.valueMap("tim_id", "rev")).next();
    next.entrySet().stream().filter(entry -> ((List<Vertex>) entry.getValue()).size() < 2)
        .forEach(entry -> {
          Vertex vertex = ((List<Vertex>) entry.getValue()).get(0);
          elements.add(vertex);
        });


    LOG.info("Start Edge validation");
    Set<Tuple<String, Integer>> edgeIdsAndVersions = graph.traversal().E()
                                                          // ignore system edges
                                                          .not(__.hasLabel("VERSION_OF", "NEXT_ITEM"))
                                                          // ignore the corrupt edges
                                                          .has("tim_id")
                                                          .has("rev")
                                                          .map(e -> new Tuple<>(
                                                            e.get().<String>value("tim_id"),
                                                            e.get().<Integer>value("rev")))
                                                          .toSet();

    Set<Tuple<String, Integer>> edgeLogEntryIdsAndVersions = graph.traversal().V()
                                                                  .or(__.has(T.label, LabelP.of("createEdgeEntry")),
                                                                    __.has(T.label, LabelP.of("updateEdgeEntry"))
                                                                  )
                                                                  .map(e -> new Tuple<>(
                                                                    e.get().<String>value("tim_id"),
                                                                    e.get().<Integer>value("rev")))
                                                                  .toSet();

    Set<Tuple<String, Integer>> missingLogEntries = Sets.difference(edgeIdsAndVersions, edgeLogEntryIdsAndVersions);

    missingLogEntries.forEach(idRev -> elements.add(graph.traversal().E()
                                                         .has("tim_id", idRev.getLeft())
                                                         .has("rev", idRev.getRight()).next()));
    return elements;
  }

}
