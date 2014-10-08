package nl.knaw.huygens.timbuctoo.rest.util.search;

/*
 * #%L
 * Timbuctoo REST api
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

import static nl.knaw.huygens.timbuctoo.rest.util.RangeHelper.mapToRange;

import java.util.List;

import nl.knaw.huygens.timbuctoo.Repository;
import nl.knaw.huygens.timbuctoo.config.TypeNames;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.DomainEntityDTO;
import nl.knaw.huygens.timbuctoo.model.RegularClientSearchResult;
import nl.knaw.huygens.timbuctoo.model.SearchResult;
import nl.knaw.huygens.timbuctoo.rest.util.HATEOASURICreator;
import nl.knaw.huygens.timbuctoo.search.SortableFieldFinder;

import com.google.common.collect.Lists;
import com.google.inject.Inject;

public class RegularClientSearchResultCreator extends ClientSearchResultCreator {

  @Inject
  public RegularClientSearchResultCreator(Repository repository, SortableFieldFinder sortableFieldFinder, HATEOASURICreator hateoasURICreator) {
    super(repository, sortableFieldFinder, hateoasURICreator);
  }

  @Override
  public <T extends DomainEntity> RegularClientSearchResult create(Class<T> type, SearchResult searchResult, int start, int rows) {
    RegularClientSearchResult result = new RegularClientSearchResult();

    List<String> ids = getIds(searchResult);
    int numFound = ids.size();
    int normalizedStart = mapToRange(start, 0, numFound);
    int normalizedRows = mapToRange(rows, 0, numFound - normalizedStart);
    int end = normalizedStart + normalizedRows;

    List<String> idsToRetrieve = ids.subList(normalizedStart, end);
    List<T> results = retrieveEntitiesWithRelations(type, idsToRetrieve);

    String queryId = searchResult.getId();

    result.setRows(normalizedRows);
    result.setStart(normalizedStart);
    result.setIds(idsToRetrieve);
    result.setResults(results);
    result.setNumFound(numFound);
    result.setRefs(createRefs(type, results));
    result.setSortableFields(sortableFieldFinder.findFields(type));
    result.setTerm(searchResult.getTerm());
    result.setFacets(searchResult.getFacets());

    setPreviousLink(normalizedStart, rows, result, queryId);
    setNextLink(start, rows, result, numFound, end, queryId);

    return result;
  }

  private <T extends DomainEntity> List<DomainEntityDTO> createRefs(Class<T> type, List<T> entities) {
    String itype = TypeNames.getInternalName(type);
    String xtype = TypeNames.getExternalName(type);
    List<DomainEntityDTO> list = Lists.newArrayListWithCapacity(entities.size());
    for (DomainEntity entity : entities) {
      list.add(new DomainEntityDTO(itype, xtype, entity));
      // TODO eliminate this, once results are no longer part of the representation
      entity.clearRelations();
    }
    return list;
  }

}
