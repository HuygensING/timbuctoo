package nl.knaw.huygens.timbuctoo.rdf.tripleprocessor;

import nl.knaw.huygens.timbuctoo.server.GraphWrapper;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.tinkerpop.gremlin.structure.Vertex;

class AddRelationTripleProcessor implements TripleProcessor {
  private final CollectionMapper collectionMapper;
  private final GraphUtil graphUtil;

  public AddRelationTripleProcessor(GraphWrapper graphWrapper) {
    this.collectionMapper = new CollectionMapper(graphWrapper);
    graphUtil = new GraphUtil(graphWrapper);
  }

  @Override
  public void process(Triple triple, String vreName) {
    Node node = triple.getSubject();
    final Vertex subjectVertex = graphUtil.findOrCreateEntityVertex(node);
    final Vertex objectVertex = graphUtil.findOrCreateEntityVertex(triple.getObject());
    collectionMapper.addToCollection(subjectVertex, new CollectionDescription("unknown"));
    collectionMapper.addToCollection(objectVertex, new CollectionDescription("unknown"));
    subjectVertex.addEdge(triple.getPredicate().getLocalName(), objectVertex);
  }
}
