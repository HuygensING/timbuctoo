package nl.knaw.huygens.timbuctoo.model;

/*
 * #%L
 * Timbuctoo core
 * =======
 * Copyright (C) 2012 - 2014 Huygens ING
 * =======
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

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
