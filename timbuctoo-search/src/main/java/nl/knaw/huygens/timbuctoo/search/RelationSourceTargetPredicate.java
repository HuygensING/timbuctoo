package nl.knaw.huygens.timbuctoo.search;

import java.util.Collection;

import nl.knaw.huygens.timbuctoo.model.Relation;

import com.google.common.base.Predicate;

public class RelationSourceTargetPredicate<T extends Relation> //
    implements Predicate<T> {

  private final Collection<String> sourceIds;
  private final Collection<String> targetIds;

  public RelationSourceTargetPredicate(Collection<String> sourceIds, Collection<String> targetIds) {
    this.sourceIds = sourceIds;
    this.targetIds = targetIds;
  }

  @Override
  public boolean apply(T relation) {
    return sourceIds.contains(relation.getSourceId()) && targetIds.contains(relation.getTargetId());
  }

}