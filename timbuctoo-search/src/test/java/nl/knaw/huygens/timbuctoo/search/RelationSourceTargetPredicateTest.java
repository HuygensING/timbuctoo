package nl.knaw.huygens.timbuctoo.search;

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
