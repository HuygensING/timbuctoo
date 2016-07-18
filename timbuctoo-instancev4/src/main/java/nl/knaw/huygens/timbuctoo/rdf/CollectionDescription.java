package nl.knaw.huygens.timbuctoo.rdf;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.structure.VertexProperty;

import static nl.knaw.huygens.timbuctoo.model.vre.Collection.ENTITY_TYPE_NAME_PROPERTY_NAME;


class CollectionDescription {
  public static final String DEFAULT_COLLECTION_NAME = "unknown";
  public static final String RDF_URI_PREFIX = "https://repository.huygens.knaw.nl";
  private final String entityTypeName;
  private final String vreName;
  private final String rdfUri;

  public CollectionDescription(String entityTypeName, String vreName) {
    this(entityTypeName, vreName, createRdfUri(entityTypeName));
  }

  private static String createRdfUri(String entityTypeName) {
    return String.format("%s/%s", RDF_URI_PREFIX, entityTypeName);
  }

  public CollectionDescription(String entityTypeName, String vreName, String rdfUri) {
    this.entityTypeName = entityTypeName;
    this.vreName = vreName;
    this.rdfUri = rdfUri;
  }

  public static CollectionDescription getDefault(String vreName) {
    return new CollectionDescription(DEFAULT_COLLECTION_NAME, vreName);
  }

  public static CollectionDescription fromVertex(String vreName, Vertex vertex) {
    VertexProperty<String> rdfUri = vertex.property(Database.RDF_URI_PROP);
    if(rdfUri.isPresent()){
      return new CollectionDescription(vertex.value(ENTITY_TYPE_NAME_PROPERTY_NAME), vreName, rdfUri.value());
    }
    return new CollectionDescription(vertex.value(ENTITY_TYPE_NAME_PROPERTY_NAME), vreName);
  }

  public String getEntityTypeName() {
    return entityTypeName;
  }

  public String getCollectionName() {
    return entityTypeName + "s";
  }

  public String createPropertyName(String propertyName) {
    return getPrefix() + "_" + propertyName;
  }

  public String getVreName() {
    return vreName;
  }

  public String getPrefix() {
    return vreName + getEntityTypeName();
  }

  @Override
  public boolean equals(Object obj) {
    return EqualsBuilder.reflectionEquals(this, obj);
  }

  @Override
  public int hashCode() {
    return HashCodeBuilder.reflectionHashCode(this);
  }

  public String getRdfUri() {
    return rdfUri ;
  }
}

