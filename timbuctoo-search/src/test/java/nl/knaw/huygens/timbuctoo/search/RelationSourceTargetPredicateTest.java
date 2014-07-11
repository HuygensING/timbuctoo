package nl.knaw.huygens.timbuctoo.search;

/*
 * #%L
 * Timbuctoo search
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

import java.util.List;

import nl.knaw.huygens.timbuctoo.model.Relation;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;

public class RelationSourceTargetPredicateTest {
  private String sourceIdInCollection = "sourceId1";
  private String sourceIdNotInAnyCollection = "sourceId3";
  private String targetIdInSourceIdsCollection = "targetIdInSourceCollection";
  private List<String> sourceIds = Lists.newArrayList(sourceIdInCollection, "sourceId2", targetIdInSourceIdsCollection);
  private String targetIdInCollection = "targetId1";
  private String targetIdNotInAnyCollection = "targetId3";
  private String sourceIdInTargetIdsCollection = "sourceIdInTargetCollection";
  private List<String> targetIds = Lists.newArrayList(targetIdInCollection, "targetId2", sourceIdInTargetIdsCollection);

  private RelationSourceTargetPredicate<Relation> instance;

  @Before
  public void setup() {
    instance = new RelationSourceTargetPredicate<Relation>(sourceIds, targetIds);
  }

  @Test
  public void whenOnlyTheSourceIdIsInThePossibleSourceIdCollectionApplyReturnsFalse() {
    Relation relation = new Relation();
    relation.setSourceId(sourceIdInCollection);
    relation.setTargetId(targetIdNotInAnyCollection);

    assertThat(instance.apply(relation), is(false));

  }

  @Test
  public void whenOnlyTheTargetIdIsInThePossibleTargetIdCollectionApplyReturnsFalse() {
    Relation relation = new Relation();
    relation.setSourceId(sourceIdNotInAnyCollection);
    relation.setTargetId(targetIdInCollection);

    assertThat(instance.apply(relation), is(false));
  }

  @Test
  public void whenTheSourceIdIsInThePossibleSourceIdCollectionAndTheTargetIdIsInThePossibleTargetIdsCollectionApplyReturnsTrue() {
    Relation relation = new Relation();
    relation.setSourceId(sourceIdInCollection);
    relation.setTargetId(targetIdInCollection);

    assertThat(instance.apply(relation), is(true));
  }

  @Test
  public void whenTheSourceIdIsInTTargetIdsCollectionAndTargetIdIsInSourceIdsCollectionApplyReturnsTrue() {
    Relation relation = new Relation();
    relation.setSourceId(sourceIdInTargetIdsCollection);
    relation.setTargetId(targetIdInSourceIdsCollection);

    assertThat(instance.apply(relation), is(true));
  }

  @Test
  public void whenNeitherIsInTheAppropriateCollectionApplyReturnsFalse() {
    Relation relation = new Relation();
    relation.setSourceId(sourceIdNotInAnyCollection);
    relation.setTargetId(targetIdNotInAnyCollection);

    assertThat(instance.apply(relation), is(false));
  }
}
