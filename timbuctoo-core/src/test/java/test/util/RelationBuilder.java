package test.util;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import nl.knaw.huygens.timbuctoo.model.Relation;

public class RelationBuilder {
  private String typeType;
  private String typeId;
  private String sourceId;
  private String targetId;
  private String sourceType;
  private String targetType;

  private RelationBuilder() {

  }

  public static RelationBuilder createRelation() {
    return new RelationBuilder();
  }

  public RelationBuilder withRelationTypeType(String type) {
    this.typeType = type;
    return this;
  }

  public RelationBuilder withRelationTypeId(String id) {
    this.typeId = id;
    return this;
  }

  public RelationBuilder withSourceId(String id) {
    this.sourceId = id;
    return this;
  }

  public RelationBuilder withSourceType(String type) {
    this.sourceType = type;
    return this;
  }

  public RelationBuilder withTargetId(String id) {
    this.targetId = id;
    return this;
  }

  public RelationBuilder withTargeType(String type) {
    this.targetType = type;
    return this;
  }

  public Relation build() {
    Relation relation = new Relation();
    relation.setTypeId(typeId);
    relation.setTypeType(typeType);
    relation.setSourceId(sourceId);
    relation.setSourceType(sourceType);
    relation.setTargetId(targetId);
    relation.setTargetType(targetType);

    return relation;
  }

  public Relation buildMock() {
    Relation relation = mock(Relation.class);
    when(relation.getTypeId()).thenReturn(typeId);
    when(relation.getTypeType()).thenReturn(typeType);
    when(relation.getSourceId()).thenReturn(sourceId);
    when(relation.getSourceType()).thenReturn(sourceType);
    when(relation.getTargetId()).thenReturn(targetId);
    when(relation.getTargetType()).thenReturn(targetType);

    return relation;
  }
}
