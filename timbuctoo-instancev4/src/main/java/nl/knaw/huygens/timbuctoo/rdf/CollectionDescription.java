package nl.knaw.huygens.timbuctoo.rdf;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.structure.VertexProperty;

import static nl.knaw.huygens.timbuctoo.core.dto.dataset.Collection.COLLECTION_IS_UNKNOWN_PROPERTY_NAME;
import static nl.knaw.huygens.timbuctoo.core.dto.dataset.Collection.ENTITY_TYPE_NAME_PROPERTY_NAME;


class CollectionDescription {
  public static final String RDF_URI_PREFIX = "https://repository.huygens.knaw.nl";
  private static final String DEFAULT_COLLECTION_NAME = "unknown";
  private final String entityTypeName;
  private final String vreName;
  private final String rdfUri;
  private boolean unknown;

  private CollectionDescription(String entityTypeName, String vreName, String rdfUri, boolean isUnknown) {
    this.entityTypeName = entityTypeName;
    this.vreName = vreName;
    this.rdfUri = rdfUri;
    this.unknown = isUnknown;
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
    VertexProperty<String> rdfUri = vertex.property(RdfProperties.RDF_URI_PROP);
    Boolean isUnknown = vertex.property(COLLECTION_IS_UNKNOWN_PROPERTY_NAME).isPresent() ?
      vertex.value(COLLECTION_IS_UNKNOWN_PROPERTY_NAME) : false;
    final String entityTypeName = vertex.value(ENTITY_TYPE_NAME_PROPERTY_NAME);
    if (rdfUri.isPresent()) {
      return createCollectionDescription(entityTypeName, vreName, rdfUri.value(), isUnknown);
    }
    return createCollectionDescription(entityTypeName, vreName);
  }


  public static CollectionDescription createCollectionDescription(String entityTypeName, String vreName) {
    String prefixedEntityTypeName = createEntityTypeName(entityTypeName, vreName);
    return createCollectionDescription(
      prefixedEntityTypeName,
      vreName,
      createRdfUri(prefixedEntityTypeName));
  }

  public static CollectionDescription createCollectionDescription(String entityTypeName, String vreName,
                                                                  String rdfUri, boolean isUnkown) {
    return new CollectionDescription(createEntityTypeName(entityTypeName, vreName), vreName, rdfUri, isUnkown);
  }

  public static CollectionDescription createCollectionDescription(String entityTypeName, String vreName,
                                                                  String rdfUri) {
    return new CollectionDescription(createEntityTypeName(entityTypeName, vreName), vreName, rdfUri, false);
  }

  public static CollectionDescription getAdmin(Vertex archetypeVertex) {
    String entityTypeName = archetypeVertex.value(ENTITY_TYPE_NAME_PROPERTY_NAME);
    return new CollectionDescription(entityTypeName, "Admin", createRdfUri(entityTypeName), false);
  }

  public static CollectionDescription createForAdmin(String entityTypeName) {
    return new CollectionDescription(entityTypeName, "Admin", createRdfUri(entityTypeName), false);
  }

  private static String createEntityTypeName(String entityTypeName, String vreName) {
    return shouldPrefixEntityTypeName(entityTypeName, vreName) ? vreName + entityTypeName : entityTypeName;
  }

  private static boolean shouldPrefixEntityTypeName(String entityTypeName, String vreName) {
    return !("Admin".equals(vreName) || entityTypeName.startsWith(vreName));
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

