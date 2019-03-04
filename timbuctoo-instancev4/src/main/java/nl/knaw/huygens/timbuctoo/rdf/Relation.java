package nl.knaw.huygens.timbuctoo.rdf;

import org.apache.tinkerpop.gremlin.structure.Edge;

import java.time.Clock;

import static nl.knaw.huygens.timbuctoo.rdf.RdfProperties.RDF_URI_PROP;
import static nl.knaw.huygens.timbuctoo.util.JsonBuilder.jsn;
import static nl.knaw.huygens.timbuctoo.util.JsonBuilder.jsnA;

public class Relation {
  private final Edge edge;

  public Relation(Edge edge, RelationType relationType) {
    this.edge = edge;
    setSystemProperties();
    setRdfUri(relationType.getRdfUri());
    setTypeId(relationType.getTimId());
  }

  private void setTypeId(String timId) {
    edge.property("typeId", timId);
  }

  private void setSystemProperties() {
    SystemPropertyModifier systemPropertyModifier = new SystemPropertyModifier(Clock.systemDefaultZone());

    systemPropertyModifier.setCreated(edge, "rdf-importer");
    systemPropertyModifier.setModified(edge, "rdf-importer");
    systemPropertyModifier.setRev(edge, 1);
    systemPropertyModifier.setIsDeleted(edge, false);
    systemPropertyModifier.setIsLatest(edge, true);
    systemPropertyModifier.setTimId(edge);
  }

  private void setRdfUri(String uri) {
    edge.property(RDF_URI_PROP, uri);
  }

  public void setCommonVreProperties(String vreName) {
    final String prefixedName = vreName + "relation";
    edge.property("relation_accepted", true);
    edge.property(prefixedName + "_accepted", true);
    edge.property("types", jsnA(jsn("relation"), jsn(prefixedName)).toString());
  }
}
