package nl.knaw.huygens.timbuctoo.rdf.tripleprocessor;

import nl.knaw.huygens.timbuctoo.server.GraphWrapper;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.util.List;

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
    final Vertex subjectVertex = graphUtil.findOrCreateEntityVertex(node, CollectionDescription.getDefault(vreName));

    final List<CollectionDescription> collections = collectionMapper.getCollectionDescriptions(subjectVertex, vreName);
    collections.forEach(collectionDescription -> subjectVertex.property(
      collectionDescription.createPropertyName(triple.getPredicate().getLocalName()),
      triple.getObject().getLiteralLexicalForm()
    ));
  }

}
