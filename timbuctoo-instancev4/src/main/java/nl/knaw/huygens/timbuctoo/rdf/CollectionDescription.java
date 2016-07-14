package nl.knaw.huygens.timbuctoo.rdf;

class CollectionDescription {
  public static final String DEFAULT_COLLECTION_NAME = "unknown";
  private final String entityTypeName;
  private final String vreName;

  public CollectionDescription(String entityTypeName, String vreName) {
    this.entityTypeName = entityTypeName;
    this.vreName = vreName;
  }

  public static CollectionDescription getDefault(String vreName) {
    return new CollectionDescription(DEFAULT_COLLECTION_NAME, vreName);
  }

  public String getEntityTypeName() {
    return entityTypeName;
  }

  public String getCollectionName() {
    return entityTypeName + "s";
  }

  public String createPropertyName(String propertyName) {
    return vreName + getEntityTypeName() + "_" + propertyName;
  }

  public String getVreName() {
    return vreName;
  }

  public String getPrefix() {
    return vreName + getEntityTypeName();
  }
}

