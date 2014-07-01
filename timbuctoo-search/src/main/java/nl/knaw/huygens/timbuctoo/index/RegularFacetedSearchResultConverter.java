package nl.knaw.huygens.timbuctoo.index;

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
import java.util.Map;

import nl.knaw.huygens.facetedsearch.model.FacetedSearchResult;
import nl.knaw.huygens.solr.SolrFields;
import nl.knaw.huygens.timbuctoo.model.SearchResult;

import com.google.common.collect.Lists;

/**
 * Converts a FacetedSearchResult to a SearchResult, Timbuctoo uses. 
 */
public class RegularFacetedSearchResultConverter implements FacetedSearchResultConverter {

  @Override
  public SearchResult convert(String typeString, FacetedSearchResult facetedSearchResult) {
    SearchResult searchResult = createSearchResult();

    searchResult.setFacets(facetedSearchResult.getFacets());
    searchResult.setSort(facetedSearchResult.getSort());
    searchResult.setTerm(facetedSearchResult.getTerm());
    searchResult.setSearchType(typeString);

    searchResult.setIds(extractIdsFrom(facetedSearchResult));

    return searchResult;
  }

  private List<String> extractIdsFrom(FacetedSearchResult facetedSearchResult) {
    List<String> ids = Lists.newArrayList();

    for (Map<String, Object> resultRow : facetedSearchResult.getRawResults()) {
      ids.add(String.valueOf(resultRow.get(SolrFields.DOC_ID)));
    }

    return ids;
  }

  protected SearchResult createSearchResult() {
    return new SearchResult();
  }

}
