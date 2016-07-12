package nl.knaw.huygens.timbuctoo.rdf.tripleprocessor;

import nl.knaw.huygens.timbuctoo.server.GraphWrapper;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.tinkerpop.gremlin.structure.Vertex;

class AddPropertyTripleProcessor implements TripleProcessor {
  private final CollectionMapper collectionMapper;
  private final GraphUtil graphUtil;

  public AddPropertyTripleProcessor(GraphWrapper graphWrapper) {
    this.collectionMapper = new CollectionMapper(graphWrapper);
    graphUtil = new GraphUtil(graphWrapper);
  }

  @Override
  public void process(Triple triple, String vreName) {
    Node node = triple.getSubject();
    final Vertex subjectVertex = graphUtil.findOrCreateEntityVertex(node);

    final CollectionDescription collectionDesc = collectionMapper.getCollectionDescription(subjectVertex);
    subjectVertex.property(
      createPropertyName(triple, vreName, collectionDesc),
      triple.getObject().getLiteralLexicalForm());
  }

  private String createPropertyName(Triple triple, String vreName, CollectionDescription collectionDescription) {
    return vreName + collectionDescription.getEntityTypeName() + "_" + triple.getPredicate().getLocalName();
  }
}
