package nl.knaw.huygens.timbuctoo.search;

/*
 * #%L
 * Timbuctoo VRE
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

import java.util.List;
import java.util.Set;

import nl.knaw.huygens.timbuctoo.model.Relation;
import nl.knaw.huygens.timbuctoo.model.SearchResult;

import com.google.common.collect.Lists;
import com.google.inject.Singleton;

@Singleton
public class RelationSearchResultCreator {

  public SearchResult create(String vreId, String typeString, Set<Relation> filteredRelations, List<String> sourceIds, List<String> targetIds, List<String> relationTypeIds) {

    SearchResult result = createSearchResult();
    result.setRelationSearch(true);
    result.setVreId(vreId);
    result.setSearchType(typeString);
    result.setSourceIds(sourceIds);
    result.setTargetIds(targetIds);
    result.setRelationTypeIds(relationTypeIds);

    result.setIds(getRelationIds(filteredRelations));

    return result;
  }

  private List<String> getRelationIds(Set<Relation> filteredRelations) {
    List<String> relationIds = Lists.newArrayList();
    for (Relation relation : filteredRelations) {
      relationIds.add(relation.getId());
    }
    return relationIds;
  }

  protected SearchResult createSearchResult() {
    return new SearchResult();
  }

}
