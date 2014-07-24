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

import java.util.List;
import java.util.Set;

import nl.knaw.huygens.timbuctoo.model.Relation;
import nl.knaw.huygens.timbuctoo.model.SearchResult;

import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.inject.Singleton;

@Singleton
public class RelationSearchResultCreator {

  private static Logger LOG = LoggerFactory.getLogger(RelationSearchResultCreator.class);

  public SearchResult create(Set<Relation> filteredRelations, List<String> sourceIds, List<String> targetIds, List<String> relationTypeIds, String typeString) {

    SearchResult result = createSearchResult();
    result.setRelationSearch(true);
    result.setSearchType(typeString);
    result.setSourceIds(sourceIds);
    result.setTargetIds(targetIds);
    result.setRelationTypeIds(relationTypeIds);

    StopWatch getRelationIdsStopWatch = new StopWatch();
    getRelationIdsStopWatch.start();

    result.setIds(getRelationIds(filteredRelations));

    getRelationIdsStopWatch.stop();
    LOG.info(String.format("%s: %.3f seconds", "getRelationIds", (double) getRelationIdsStopWatch.getTime() / 1000));

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
