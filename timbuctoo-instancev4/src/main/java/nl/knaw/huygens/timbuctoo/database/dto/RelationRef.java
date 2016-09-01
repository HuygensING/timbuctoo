package nl.knaw.huygens.timbuctoo.database.dto;

public class RelationRef {
  private String entityId;
  private String collectionName;
  private String entityType;
  private boolean relationAccepted;
  private String relationId;
  private int relationRev;
  private String relationType;
  private String displayName;

  public RelationRef(String entityId, String collectionName, String entityType, boolean relationAccepted,
                     String relationId,
                     int relationRev, String relationType, String displayName) {
    this.entityId = entityId;
    this.collectionName = collectionName;
    this.entityType = entityType;
    this.relationAccepted = relationAccepted;
    this.relationId = relationId;
    this.relationRev = relationRev;
    this.relationType = relationType;
    this.displayName = displayName;
  }

  public String getEntityId() {
    return entityId;
  }

  public String getCollectionName() {
    return collectionName;
  }

  public String getEntityType() {
    return entityType;
  }

  public boolean isRelationAccepted() {
    return relationAccepted;
  }

  public String getRelationId() {
    return relationId;
  }

  public int getRelationRev() {
    return relationRev;
  }

  public String getRelationType() {
    return relationType;
  }

  public String getDisplayName() {
    return displayName;
  }

}
