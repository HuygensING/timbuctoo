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
  private String sourceIdNotInCollection = "sourceId3";
  private List<String> sourceIds = Lists.newArrayList(sourceIdInCollection, "sourceId2");
  private String targetIdInCollection = "targetId1";
  private String targetIdNotInCollection = "targetId3";
  private List<String> targetIds = Lists.newArrayList(targetIdInCollection, "targetId2");

  private RelationSourceTargetPredicate<Relation> instance;

  @Before
  public void setup() {
    instance = new RelationSourceTargetPredicate<Relation>(sourceIds, targetIds);
  }

  @Test
  public void whenOnlyTheSourceIdIsInThePossibleSourceIdCollectionApplyReturnsFalse() {
    Relation relation = new Relation();
    relation.setSourceId(sourceIdInCollection);
    relation.setTargetId(targetIdNotInCollection);

    assertThat(instance.apply(relation), is(false));

  }

  @Test
  public void whenOnlyTheTargetIdIsInThePossibleTargetIdCollectionApplyReturnsFalse() {
    Relation relation = new Relation();
    relation.setSourceId(sourceIdNotInCollection);
    relation.setTargetId(targetIdInCollection);

    assertThat(instance.apply(relation), is(false));
  }

  @Test
  public void whenTheSourceIdIsInThePossibleSourceIdCollectionAndTheTargetIdIsInThePossibleTargetIdCollectionApplyReturnsTrue() {
    Relation relation = new Relation();
    relation.setSourceId(sourceIdInCollection);
    relation.setTargetId(targetIdInCollection);

    assertThat(instance.apply(relation), is(true));
  }

  @Test
  public void whenNeitherIsInTheAppropriateCollectionApplyReturnsFalse() {
    Relation relation = new Relation();
    relation.setSourceId(sourceIdNotInCollection);
    relation.setTargetId(targetIdNotInCollection);

    assertThat(instance.apply(relation), is(false));
  }
}
