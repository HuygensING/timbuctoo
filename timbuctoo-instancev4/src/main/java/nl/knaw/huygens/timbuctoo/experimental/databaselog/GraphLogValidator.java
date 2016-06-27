package nl.knaw.huygens.timbuctoo.experimental.databaselog;

import com.google.common.collect.Sets;
import nl.knaw.huygens.timbuctoo.server.GraphWrapper;
import org.apache.tinkerpop.gremlin.neo4j.process.traversal.LabelP;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.Element;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class GraphLogValidator {
  private final GraphWrapper graphWrapper;

  public GraphLogValidator(GraphWrapper graphWrapper) {
    this.graphWrapper = graphWrapper;
  }

  public Set<Element> validate() {
    LoggerFactory.getLogger(GraphLogValidator.class).info("Start Vertex validation");
    Set<Element> elements = Sets.newHashSet();

    Graph graph = graphWrapper.getGraph();
    Map<Object, Object> next = graph.traversal().V()
                                    .has("tim_id")
                                    .has("rev")
                                    .not(__.has(T.label, LabelP.of("searchresult"))) // ignore search results
                                    // ignore the edge entries, because they give false positives
                                    .not(__.has(T.label, LabelP.of("createEdgeEntry")))
                                    .not(__.has(T.label, LabelP.of("updateEdgeEntry")))
                                    .group().<Map<Object, List<Vertex>>>by(__.valueMap("tim_id", "rev")).next();
    next.entrySet().stream().filter(entry -> ((List<Vertex>) entry.getValue()).size() < 2)
        .forEach(entry -> {
          Vertex vertex = ((List<Vertex>) entry.getValue()).get(0);
          elements.add(vertex);
        });

    // GraphTraversal<Edge, Edge> edges = graph.traversal().E()
    //                                         .not(__.hasLabel("VERSION_OF", "NEXT_ITEM")); // ignore system edges
    // for (; edges.hasNext(); ) {
    //   Element element = edges.next();
    //
    //   boolean hasLogEdgeEntry = graph.traversal().V()
    //                                  .or(
    //                                    __.has(T.label, LabelP.of("createEdgeEntry")),
    //                                    __.has(T.label, LabelP.of("updateEdgeEntry"))
    //                                  )
    //                                  .has("tim_id", element.<String>value("tim_id"))
    //                                  .has("rev", element.<Integer>value("rev"))
    //                                  .hasNext();
    //
    //   if (!hasLogEdgeEntry) {
    //     elements.add(element);
    //   }
    // }
    //
    // LoggerFactory.getLogger(GraphLogValidator.class).info("Start Vertex validation");
    return elements;
  }

}
