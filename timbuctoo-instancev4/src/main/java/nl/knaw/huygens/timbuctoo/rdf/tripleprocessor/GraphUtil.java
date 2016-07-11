package nl.knaw.huygens.timbuctoo.rdf.tripleprocessor;

import nl.knaw.huygens.timbuctoo.server.GraphWrapper;
import org.apache.jena.graph.Node;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.time.Clock;

public class GraphUtil {
  public static final String RDF_URI_PROP = "rdfUri";
  private final GraphWrapper graphWrapper;
  private final SystemPropertyModifier systemPropertyModifier;

  public GraphUtil(GraphWrapper graphWrapper) {
    this(graphWrapper, new SystemPropertyModifier(Clock.systemDefaultZone()));
  }

  GraphUtil(GraphWrapper graphWrapper, SystemPropertyModifier systemPropertyModifier) {
    this.graphWrapper = graphWrapper;
    this.systemPropertyModifier = systemPropertyModifier;
  }

  public Vertex findOrCreateEntityVertex(Node node) {
    Graph graph = graphWrapper.getGraph();
    final GraphTraversal<Vertex, Vertex> existingT = graph.traversal().V()
                                                                 .has(RDF_URI_PROP, node.getURI());
    if (existingT.hasNext()) {
      return existingT.next();
    } else {
      Vertex vertex = graph.addVertex();
      vertex.property(RDF_URI_PROP, node.getURI());

      systemPropertyModifier.setCreated(vertex, "rdf-importer");
      systemPropertyModifier.setModified(vertex, "rdf-importer");
      systemPropertyModifier.setTimId(vertex);
      systemPropertyModifier.setRev(vertex, 1);
      systemPropertyModifier.setIsLatest(vertex, true);

      return vertex;
    }
  }
}
