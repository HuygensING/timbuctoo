package nl.knaw.huygens.timbuctoo.rdf.tripleprocessor;

import nl.knaw.huygens.timbuctoo.server.GraphWrapper;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.tinkerpop.gremlin.structure.Vertex;

class AddRelationTripleProcessor implements TripleProcessor {
  private final GraphWrapper graphWrapper;
  private final CollectionMapper collectionMapper;

  public AddRelationTripleProcessor(GraphWrapper graphWrapper) {
    this.graphWrapper = graphWrapper;
    this.collectionMapper = new CollectionMapper(graphWrapper);
  }

  @Override
  public void process(Triple triple) {
    Node node = triple.getSubject();
    final Vertex subjectVertex = GraphUtil.findOrCreateVertex(graphWrapper.getGraph(), node);
    final Vertex objectVertex = GraphUtil.findOrCreateVertex(graphWrapper.getGraph(), triple.getObject());
    collectionMapper.addToCollection(subjectVertex, "unknown");
    collectionMapper.addToCollection(objectVertex, "unknown");
    subjectVertex.addEdge(triple.getPredicate().getLocalName(), objectVertex);
  }
}
