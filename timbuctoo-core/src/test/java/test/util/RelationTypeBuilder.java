package test.util;

import nl.knaw.huygens.timbuctoo.model.RelationType;

public class RelationTypeBuilder {
  private String targetTypeName;
  private String sourceTypeName;

  private RelationTypeBuilder() {

  }

  public static RelationTypeBuilder createRelationType() {
    return new RelationTypeBuilder();
  }

  public RelationTypeBuilder withSourceTypeName(String sourceTypeName) {
    this.sourceTypeName = sourceTypeName;
    return this;
  }

  public RelationTypeBuilder withTargetTypeName(String targetTypeName) {
    this.targetTypeName = targetTypeName;
    return this;
  }

  public RelationType build() {
    RelationType relationType = new RelationType();

    relationType.setTargetTypeName(targetTypeName);
    relationType.setSourceTypeName(sourceTypeName);

    return relationType;
  }

}
