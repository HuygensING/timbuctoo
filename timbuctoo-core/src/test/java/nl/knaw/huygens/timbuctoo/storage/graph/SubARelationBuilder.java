package nl.knaw.huygens.timbuctoo.storage.graph;

import nl.knaw.huygens.timbuctoo.model.util.Change;
import test.model.projecta.SubARelation;

public class SubARelationBuilder {
  private String sourceId;
  private String sourceType;
  private String targetId;
  private String tagetType;
  private String typeId;
  private String typeType;
  private String id;
  private boolean accepted;
  private int revision;
  private Change modified;
  private String pid;

  private SubARelationBuilder() {}

  public static SubARelationBuilder aRelation() {
    return new SubARelationBuilder();
  }

  public SubARelationBuilder withSourceId(String sourceId) {
    this.sourceId = sourceId;
    return this;
  }

  public SubARelationBuilder withSourceType(String sourceType) {
    this.sourceType = sourceType;
    return this;
  }

  public SubARelationBuilder withTargetId(String targetId) {
    this.targetId = targetId;
    return this;
  }

  public SubARelationBuilder withTargetType(String tagetType) {
    this.tagetType = tagetType;
    return this;
  }

  public SubARelationBuilder withTypeId(String typeId) {
    this.typeId = typeId;
    return this;
  }

  public SubARelationBuilder withTypeType(String typeType) {
    this.typeType = typeType;
    return this;
  }

  public SubARelationBuilder withId(String id) {
    this.id = id;
    return this;
  }

  public SubARelationBuilder isAccepted(boolean accepted) {
    this.accepted = accepted;
    return this;
  }

  public SubARelationBuilder withRevision(int revision) {
    this.revision = revision;
    return this;
  }

  public SubARelationBuilder withModified(Change modified) {
    this.modified = modified;
    return this;
  }

  public SubARelation build() {
    SubARelation relation = new SubARelation();
    relation.setId(id);
    relation.setSourceId(sourceId);
    relation.setSourceType(sourceType);
    relation.setTargetId(targetId);
    relation.setTargetType(tagetType);
    relation.setTypeId(typeId);
    relation.setTypeType(typeType);
    relation.setAccepted(accepted);
    relation.setRev(revision);
    relation.setModified(modified);
    relation.setPid(pid);

    return relation;
  }

  public SubARelationBuilder withAPID() {
    this.pid = "pid";
    return this;
  }

}
