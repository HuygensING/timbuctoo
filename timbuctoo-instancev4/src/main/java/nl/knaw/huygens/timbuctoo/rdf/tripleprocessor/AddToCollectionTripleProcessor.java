package nl.knaw.huygens.timbuctoo.rdf.tripleprocessor;

import nl.knaw.huygens.timbuctoo.server.GraphWrapper;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.tinkerpop.gremlin.structure.Vertex;

class AddToCollectionTripleProcessor implements TripleProcessor {
  private final CollectionMapper collectionMapper;
  private final GraphUtil graphUtil;

  public AddToCollectionTripleProcessor(GraphWrapper graphWrapper) {
    this.collectionMapper = new CollectionMapper(graphWrapper);
    graphUtil = new GraphUtil(graphWrapper);
  }

  @Override
  public void process(Triple triple) {
    Node node = triple.getSubject();
    final Vertex subjectVertex = graphUtil.findOrCreateEntityVertex(node);
    collectionMapper.addToCollection(subjectVertex, triple.getObject().getLocalName());
  }
}
