package nl.knaw.huygens.timbuctoo.model;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static test.util.RelationBuilder.createRelation;
import static test.util.RelationTypeBuilder.createRelationType;

import org.junit.Test;

public class RelationTest {

  private String targetType = "targetType";
  private String sourceType = "sourceType";

  @Test
  public void testConfomsRelationTypeRelationConforms() {

    Relation relation = createRelation() //
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
    Relation relation = createRelation() //
        .withSourceType("differentSourceType") //
        .withTargeType(targetType) //
        .build();

    testConformsRelationType(relation, false);
  }

  @Test
  public void testConfomsRelationTypeSourceTypeNull() {
    Relation relation = createRelation() //
        .withSourceType(null) //
        .withTargeType(targetType) //
        .build();

    testConformsRelationType(relation, false);
  }

  @Test
  public void testConfomsRelationTypeTargetTypeDifferent() {
    Relation relation = createRelation() //
        .withSourceType(sourceType) //
        .withTargeType("differentTargetType") //
        .build();

    testConformsRelationType(relation, false);
  }

  @Test
  public void testConfomsRelationTypeTargetTypeNull() {
    Relation relation = createRelation() //
        .withSourceType(sourceType) //
        .withTargeType(null) //
        .build();

    testConformsRelationType(relation, false);
  }
}
