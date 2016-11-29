package nl.knaw.huygens.timbuctoo.rdf;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.structure.VertexProperty;

import static nl.knaw.huygens.timbuctoo.database.dto.dataset.Collection.ENTITY_TYPE_NAME_PROPERTY_NAME;


class CollectionDescription {
  public static final String RDF_URI_PREFIX = "https://repository.huygens.knaw.nl";
  private static final String DEFAULT_COLLECTION_NAME = "unknown";
  private final String entityTypeName;
  private final String vreName;
  private final String rdfUri;
  private boolean unknown;

  private CollectionDescription(String entityTypeName, String vreName, String rdfUri) {
    this.entityTypeName = entityTypeName;
    this.vreName = vreName;
    this.rdfUri = rdfUri;
  }

  public static CollectionDescription getDefault(String vreName) {
    CollectionDescription result;
    if (vreName.equals("Admin")) {
      result = createCollectionDescription("concept", "");
    } else {
      result = createCollectionDescription(DEFAULT_COLLECTION_NAME, vreName);
    }
    result.unknown = true;
    return result;
  }

  public static CollectionDescription fromVertex(String vreName, Vertex vertex) {
    VertexProperty<String> rdfUri = vertex.property(Database.RDF_URI_PROP);
    if (rdfUri.isPresent()) {
      return createCollectionDescription(vertex.value(ENTITY_TYPE_NAME_PROPERTY_NAME), vreName, rdfUri.value());
    }
    return createCollectionDescription(vertex.value(ENTITY_TYPE_NAME_PROPERTY_NAME), vreName);
  }


  public static CollectionDescription createCollectionDescription(String entityTypeName, String vreName) {
    String prefixedEntityTypeName = createEntityTypeName(entityTypeName, vreName);
    return createCollectionDescription(
      prefixedEntityTypeName,
      vreName,
      createRdfUri(prefixedEntityTypeName));
  }

  public static CollectionDescription createCollectionDescription(String entityTypeName, String vreName,
                                                                  String rdfUri) {
    return new CollectionDescription(createEntityTypeName(entityTypeName, vreName), vreName, rdfUri);
  }

  public static CollectionDescription getAdmin(Vertex archetypeVertex) {
    String entityTypeName = archetypeVertex.value(ENTITY_TYPE_NAME_PROPERTY_NAME);
    return new CollectionDescription(entityTypeName, "Admin", createRdfUri(entityTypeName));
  }

  public static CollectionDescription createForAdmin(String entityTypeName) {
    return new CollectionDescription(entityTypeName, "Admin", createRdfUri(entityTypeName));
  }

  private static String createEntityTypeName(String entityTypeName, String vreName) {
    return entityTypeName.startsWith(vreName) ? entityTypeName : vreName + entityTypeName;
  }

  private static String createRdfUri(String entityTypeName) {
    return String.format("%s/%s", RDF_URI_PREFIX, entityTypeName);
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
    return getEntityTypeName();
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
    return rdfUri;
  }

  public boolean isUnknown() {
    return unknown;
  }
}

