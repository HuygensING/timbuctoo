package nl.knaw.huygens.timbuctoo.rdf.tripleprocessor;

public class CollectionDescription {
  private final String entityTypeName;

  public CollectionDescription(String entityTypeName) {
    this.entityTypeName = entityTypeName;
  }

  public String getEntityTypeName() {
    return entityTypeName;
  }

  public String getCollectionName() {
    return entityTypeName + "s";
  }

  public static CollectionDescription getDefault() {
    return new CollectionDescription("unknown");
  }
}

