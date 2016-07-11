package nl.knaw.huygens.timbuctoo.rdf.tripleprocessor;

import org.apache.jena.graph.Node;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;

public class GraphUtil {
  public static Vertex findOrCreateVertex(Graph graph, Node node) {
    Vertex subjectVertex;
    final GraphTraversal<Vertex, Vertex> existingT = graph.traversal().V()
                                                          .has(TripleProcessor.RDF_URI_PROP, node.getURI());

    if (existingT.hasNext()) {
      subjectVertex = existingT.next();
    } else {
      subjectVertex = graph.addVertex();
      subjectVertex.property(TripleProcessor.RDF_URI_PROP, node.getURI());
    }
    return subjectVertex;
  }
}
