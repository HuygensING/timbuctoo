package nl.knaw.huygens.timbuctoo.rdf.tripleprocessor;

import nl.knaw.huygens.timbuctoo.server.GraphWrapper;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.tinkerpop.gremlin.structure.Vertex;

class AddPropertyTripleProcessor implements TripleProcessor {
  private final GraphWrapper graphWrapper;
  private final CollectionMapper collectionMapper;

  public AddPropertyTripleProcessor(GraphWrapper graphWrapper) {
    this.graphWrapper = graphWrapper;
    this.collectionMapper = new CollectionMapper(graphWrapper);
  }

  @Override
  public void process(Triple triple) {
    Node node = triple.getSubject();
    final Vertex subjectVertex = GraphUtil.findOrCreateVertex(graphWrapper.getGraph(), node);
    collectionMapper.addToCollection(subjectVertex, "unknown");
    subjectVertex.property(triple.getPredicate().getLocalName(), triple.getObject().getLiteralLexicalForm());
  }
}
