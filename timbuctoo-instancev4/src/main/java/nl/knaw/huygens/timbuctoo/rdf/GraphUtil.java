package nl.knaw.huygens.timbuctoo.rdf;

import org.apache.jena.graph.Node;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;

public class GraphUtil {
  static Vertex findOrCreateVertex(Graph graph, Node node) {
    Vertex subjectVertex;
    final GraphTraversal<Vertex, Vertex> existingT = graph.traversal().V()
                                                          .has(TripleProcessorFactory.RDF_URI_PROP, node.getURI());

    if (existingT.hasNext()) {
      subjectVertex = existingT.next();
    } else {
      subjectVertex = graph.addVertex();
      subjectVertex.property(TripleProcessorFactory.RDF_URI_PROP, node.getURI());
    }
    return subjectVertex;
  }
}
