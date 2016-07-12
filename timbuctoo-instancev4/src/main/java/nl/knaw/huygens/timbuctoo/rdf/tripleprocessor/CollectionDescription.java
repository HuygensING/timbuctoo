package nl.knaw.huygens.timbuctoo.rdf.tripleprocessor;

public class CollectionDescription {
  private final String entityTypeName;
  private final String vreName;

  public CollectionDescription(String entityTypeName, String vreName) {
    this.entityTypeName = entityTypeName;
    this.vreName = vreName;
  }

  public String getEntityTypeName() {
    return entityTypeName;
  }

  public String getCollectionName() {
    return entityTypeName + "s";
  }

  public static CollectionDescription getDefault(String vreName) {
    return new CollectionDescription("unknown", vreName);
  }

  public String createPropertyName(String propertyName) {
    return vreName + getEntityTypeName() + "_" + propertyName;
  }

  public String getVreName() {
    return vreName;
  }
}

