package nl.knaw.huygens.timbuctoo.rdf;

import nl.knaw.huygens.timbuctoo.server.GraphWrapper;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;

public class Importer {

  static final String RDF_URI_PROP = "rdfUri";
  private GraphWrapper graphWrapper;

  public Importer(GraphWrapper graphWrapper) {

    this.graphWrapper = graphWrapper;
  }

  public void importTriple(Triple triple) {
    final Graph graph = graphWrapper.getGraph();
    Node node = triple.getSubject();
    final Vertex subjectVertex;
    subjectVertex = findOrCreateVertex(graph, node);

    if (triple.getObject().isLiteral()) {
      subjectVertex.property(triple.getPredicate().getURI(), triple.getObject().getLiteralLexicalForm());
    } else {
      final Vertex objectVertex = findOrCreateVertex(graph, triple.getObject());
      subjectVertex.addEdge(triple.getPredicate().getURI(), objectVertex);
    }
  }

  private Vertex findOrCreateVertex(Graph graph, Node node) {
    Vertex subjectVertex;
    final GraphTraversal<Vertex, Vertex> existingT = graph.traversal().V()
                                                          .has(RDF_URI_PROP, node.getURI());

    if (existingT.hasNext()) {
      subjectVertex = existingT.next();
    } else {
      subjectVertex = graph.addVertex();
      subjectVertex.property(RDF_URI_PROP, node.getURI());
    }
    return subjectVertex;
  }

}
