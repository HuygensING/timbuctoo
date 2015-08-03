package nl.knaw.huygens.timbuctoo.rest.util.search;

/*
 * #%L
 * Timbuctoo REST api
 * =======
 * Copyright (C) 2012 - 2015 Huygens ING
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

import com.google.inject.Inject;
import nl.knaw.huygens.timbuctoo.Repository;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.RelationSearchResultDTO;
import nl.knaw.huygens.timbuctoo.model.SearchResult;
import nl.knaw.huygens.timbuctoo.rest.util.HATEOASURICreator;
import nl.knaw.huygens.timbuctoo.search.SortableFieldFinder;
import nl.knaw.huygens.timbuctoo.vre.VRE;
import nl.knaw.huygens.timbuctoo.vre.VRECollection;

import java.util.List;

public class RelationSearchResultMapper extends SearchResultMapper {

  private final RelationMapper relationMapper;

  @Inject
  public RelationSearchResultMapper(Repository repository, SortableFieldFinder sortableFieldFinder, HATEOASURICreator hateoasURICreator, RelationMapper relationMapper, VRECollection vreCollection) {
    super(repository, sortableFieldFinder, hateoasURICreator, vreCollection);
    this.relationMapper = relationMapper;
  }

  @Override
  public <T extends DomainEntity> RelationSearchResultDTO create(Class<T> type, SearchResult searchResult, int start, int rows, String version) {
    RelationSearchResultDTO dto = new RelationSearchResultDTO();

    String queryId = searchResult.getId();
    VRE vre = vreCollection.getVREById(searchResult.getVreId());
    List<String> ids = getIds(searchResult);
    int numFound = ids.size();
    int normalizedStart = mapToRange(start, 0, numFound);
    int normalizedRows = mapToRange(rows, 0, numFound - normalizedStart);
    int end = normalizedStart + normalizedRows;

    List<String> idsToRetrieve = ids.subList(normalizedStart, end);
    List<T> results = retrieveEntities(type, idsToRetrieve);

    dto.setRows(normalizedRows);
    dto.setStart(normalizedStart);
    dto.setIds(ids);
    dto.setResults(results);
    dto.setNumFound(numFound);
    dto.setSourceType(searchResult.getSourceType());
    dto.setTargetType(searchResult.getTargetType());
    dto.setRefs(relationMapper.createRefs(vre, type, results));
    dto.setSortableFields(sortableFieldFinder.findFields(type));
    setNextLink(normalizedStart, rows, dto, numFound, end, queryId, version);
    setPreviousLink(normalizedStart, rows, dto, queryId, version);

    return dto;
  }

}
