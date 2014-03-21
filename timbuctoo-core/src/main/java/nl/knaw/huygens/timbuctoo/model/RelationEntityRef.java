package nl.knaw.huygens.timbuctoo.model;

public class RelationEntityRef extends EntityRef {
  private String relationId;

  public RelationEntityRef() {}

  public RelationEntityRef(String type, String xtype, String id, String displayName, String relationId) {
    super(type, xtype, id, displayName);
    this.relationId = relationId;
  }

  public String getRelationId() {
    return relationId;
  }

  public void setRelationId(String relationId) {
    this.relationId = relationId;
  }
}
