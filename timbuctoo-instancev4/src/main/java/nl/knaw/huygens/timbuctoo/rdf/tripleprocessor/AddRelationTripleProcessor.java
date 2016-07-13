package nl.knaw.huygens.timbuctoo.rdf.tripleprocessor;

import nl.knaw.huygens.timbuctoo.server.GraphWrapper;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.time.Clock;

class AddRelationTripleProcessor implements TripleProcessor {
  private final GraphUtil graphUtil;
  private final SystemPropertyModifier systemPropertyModifier;

  public AddRelationTripleProcessor(GraphWrapper graphWrapper) {
    graphUtil = new GraphUtil(graphWrapper);
    systemPropertyModifier = new SystemPropertyModifier(Clock.systemDefaultZone());
  }

  @Override
  public void process(Triple triple, String vreName) {
    Node node = triple.getSubject();
    final Vertex subjectVertex = graphUtil.findOrCreateEntityVertex(node, CollectionDescription.getDefault(vreName));
    final Vertex objectVertex =
      graphUtil.findOrCreateEntityVertex(triple.getObject(), CollectionDescription.getDefault(vreName));

    final Edge relationEdge = subjectVertex.addEdge(triple.getPredicate().getLocalName(), objectVertex);
    relationEdge.property(GraphUtil.RDF_URI_PROP, triple.getPredicate().getURI());
    systemPropertyModifier.setCreated(relationEdge, "rdf-importer");
    systemPropertyModifier.setModified(relationEdge, "rdf-importer");
    systemPropertyModifier.setRev(relationEdge, 1);
    systemPropertyModifier.setIsDeleted(relationEdge, false);
    systemPropertyModifier.setIsLatest(relationEdge, true);
    systemPropertyModifier.setTimId(relationEdge);
  }
}
