package nl.knaw.huygens.timbuctoo.model;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static test.util.RelationTypeBuilder.createRelationType;

import java.io.IOException;

import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.model.util.RelationBuilder;
import nl.knaw.huygens.timbuctoo.storage.StorageManager;
import nl.knaw.huygens.timbuctoo.storage.ValidationException;

import org.junit.Test;

public class RelationTest {

  private String targetType = "targetType";
  private String sourceType = "sourceType";

  @Test
  public void testConfomsRelationTypeRelationConforms() {
    Relation relation = RelationBuilder.createRelation(Relation.class) //
        .withSourceType(sourceType) //
        .withTargeType(targetType) //
        .build();

    testConformsRelationType(relation, true);
  }

  private void testConformsRelationType(Relation relation, boolean conform) {
    RelationType relationType = createRelationType() //
        .withSourceTypeName(sourceType) //
        .withTargetTypeName(targetType) //
        .build();

    assertThat(relation.conformsToRelationType(relationType), is(conform));
  }

  @Test
  public void testConfomsRelationTypeSourceTypeDifferent() {
    Relation relation = RelationBuilder.createRelation(Relation.class) //
        .withSourceType("differentSourceType") //
        .withTargeType(targetType) //
        .build();

    testConformsRelationType(relation, false);
  }

  @Test
  public void testConfomsRelationTypeSourceTypeNull() {
    Relation relation = RelationBuilder.createRelation(Relation.class) //
        .withSourceType(null) //
        .withTargeType(targetType) //
        .build();

    testConformsRelationType(relation, false);
  }

  @Test
  public void testConfomsRelationTypeTargetTypeDifferent() {
    Relation relation = RelationBuilder.createRelation(Relation.class) //
        .withSourceType(sourceType) //
        .withTargeType("differentTargetType") //
        .build();

    testConformsRelationType(relation, false);
  }

  @Test
  public void testConfomsRelationTypeTargetTypeNull() {
    Relation relation = RelationBuilder.createRelation(Relation.class) //
        .withSourceType(sourceType) //
        .withTargeType(null) //
        .build();

    testConformsRelationType(relation, false);
  }

  @Test
  public void testNormalizeSymmetricRelation() throws IOException, ValidationException {
    Relation relation1 = getNormalizedRelation("ID00001", "ID00002", true);
    assertEquals("ID00001", relation1.getSourceId());
    assertEquals("ID00002", relation1.getTargetId());

    Relation relation2 = getNormalizedRelation("ID00002", "ID00001", true);
    assertEquals("ID00001", relation2.getSourceId());
    assertEquals("ID00002", relation2.getTargetId());
  }

  @Test
  public void testNormalizeASymmetricRelation() throws IOException, ValidationException {
    Relation relation1 = getNormalizedRelation("ID00001", "ID00002", false);
    assertEquals("ID00001", relation1.getSourceId());
    assertEquals("ID00002", relation1.getTargetId());

    Relation relation2 = getNormalizedRelation("ID00002", "ID00001", false);
    assertEquals("ID00002", relation2.getSourceId());
    assertEquals("ID00001", relation2.getTargetId());
  }

  private Relation getNormalizedRelation(String sourceId, String targetId, boolean symmetric) {
    TypeRegistry registry = mock(TypeRegistry.class);
    StorageManager storage = mock(StorageManager.class);

    Relation relation = new Relation();
    relation.setTypeId("typeId");
    relation.setSourceId(sourceId);
    relation.setTargetId(targetId);

    RelationType relationType = new RelationType();
    relationType.setSymmetric(symmetric);
    when(storage.getRelationType("typeId")).thenReturn(relationType);

    relation.normalize(registry, storage);
    return relation;
  }

}
