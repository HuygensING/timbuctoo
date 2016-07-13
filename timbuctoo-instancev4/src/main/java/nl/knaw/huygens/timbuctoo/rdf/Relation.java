package nl.knaw.huygens.timbuctoo.rdf;

import nl.knaw.huygens.timbuctoo.rdf.tripleprocessor.SystemPropertyModifier;
import org.apache.jena.graph.Node;
import org.apache.tinkerpop.gremlin.structure.Edge;

import java.time.Clock;

import static nl.knaw.huygens.timbuctoo.rdf.tripleprocessor.Database.RDF_URI_PROP;

public class Relation {
  private final Edge edge;

  public Relation(Edge edge, Node node) {
    this.edge = edge;
    init(edge, node);
  }

  private void init(Edge edge, Node node) {
    SystemPropertyModifier systemPropertyModifier = new SystemPropertyModifier(Clock.systemDefaultZone());

    systemPropertyModifier.setCreated(edge, "rdf-importer");
    systemPropertyModifier.setModified(edge, "rdf-importer");
    systemPropertyModifier.setRev(edge, 1);
    systemPropertyModifier.setIsDeleted(edge, false);
    systemPropertyModifier.setIsLatest(edge, true);
    systemPropertyModifier.setTimId(edge);
    edge.property(RDF_URI_PROP, node.getURI());
  }
}
