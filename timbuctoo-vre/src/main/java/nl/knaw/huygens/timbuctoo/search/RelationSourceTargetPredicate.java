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
    return isMatchingRelation(relation) || isMatchingInverseRelation(relation);
  }

  private boolean isMatchingInverseRelation(T relation) {
    return sourceIds.contains(relation.getTargetId()) && targetIds.contains(relation.getSourceId());
  }

  private boolean isMatchingRelation(T relation) {
    return sourceIds.contains(relation.getSourceId()) && targetIds.contains(relation.getTargetId());
  }

}