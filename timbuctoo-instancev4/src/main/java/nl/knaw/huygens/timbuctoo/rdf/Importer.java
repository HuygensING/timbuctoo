package nl.knaw.huygens.timbuctoo.rdf;

import nl.knaw.huygens.timbuctoo.server.GraphWrapper;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;

public class Importer {

  static final String RDF_URI_PROP = "rdfUri";
  private final CollectionMapper collectionMapper;
  private final GraphWrapper graphWrapper;

  public Importer(GraphWrapper graphWrapper) {
    this(graphWrapper, new CollectionMapper(graphWrapper));
  }

  Importer(GraphWrapper graphWrapper, CollectionMapper collectionMapper) {
    this.graphWrapper = graphWrapper;
    this.collectionMapper = collectionMapper;
  }

  public void importTriple(Triple triple) {
    final Graph graph = graphWrapper.getGraph();
    Node node = triple.getSubject();
    final Vertex subjectVertex = findOrCreateVertex(graph, node);

    if (describesType(triple)) {
      collectionMapper.addToCollection(subjectVertex, triple.getObject().getLocalName());
    } else if (describesProperty(triple)) {
      collectionMapper.addToCollection(subjectVertex, "unknown");
      subjectVertex.property(triple.getPredicate().getLocalName(), triple.getObject().getLiteralLexicalForm());
    } else if (describesRelation(triple)) {
      final Vertex objectVertex = findOrCreateVertex(graph, triple.getObject());
      collectionMapper.addToCollection(subjectVertex, "unknown");
      collectionMapper.addToCollection(objectVertex, "unknown");
      subjectVertex.addEdge(triple.getPredicate().getLocalName(), objectVertex);
    }
  }

  private boolean describesRelation(Triple triple) {
    return triple.getObject().isURI();
  }

  private boolean describesProperty(Triple triple) {
    return triple.getObject().isLiteral();
  }

  private boolean describesType(Triple triple) {
    return triple.getPredicate().getURI().equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
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
