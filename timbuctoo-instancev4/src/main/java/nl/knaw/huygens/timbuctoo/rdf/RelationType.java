package nl.knaw.huygens.timbuctoo.rdf;

import org.apache.tinkerpop.gremlin.structure.Vertex;

import static nl.knaw.huygens.timbuctoo.rdf.RdfProperties.RDF_URI_PROP;

public class RelationType {

  private final boolean inverted;
  private Vertex relationTypeVertex;

  public RelationType(Vertex relationTypeVertex) {
    this(relationTypeVertex, false);
  }

  public boolean isInverted() {
    return inverted;
  }

  public RelationType(Vertex relationTypeVertex, boolean isInverted) {
    this.relationTypeVertex = relationTypeVertex;
    this.inverted = isInverted;
  }

  public String getRegularName() {
    return relationTypeVertex.value("relationtype_regularName");
  }

  public String getRdfUri() {
    return relationTypeVertex.value(RDF_URI_PROP);
  }

  public String getTimId() {
    return relationTypeVertex.value("tim_id");
  }
}
