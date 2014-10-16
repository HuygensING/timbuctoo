package nl.knaw.huygens.timbuctoo.search.converters;

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

import static nl.knaw.huygens.timbuctoo.search.converters.SearchResultMatcher.likeSearchResult;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import nl.knaw.huygens.facetedsearch.model.Facet;
import nl.knaw.huygens.facetedsearch.model.FacetedSearchResult;
import nl.knaw.huygens.facetedsearch.model.parameters.SortParameter;
import nl.knaw.huygens.solr.SolrFields;
import nl.knaw.huygens.timbuctoo.model.SearchResult;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class SearchResultConverterTest {

  private static final ArrayList<String> IDS = Lists.newArrayList("id1", "id2", "id3");
  private static final String SEARCH_TERM = "searchTerm";
  private static final String TYPE_STRING = "typeString";
  private static final List<Facet> FACETS = Lists.newArrayList();
  private static final List<SortParameter> SORT = Lists.newArrayList();

  private List<Map<String, Object>> rawResult;
  private FacetedSearchResult facetedSearchResult;

  private FacetedSearchResult createFacetedSearch(List<Facet> facets, List<SortParameter> sort, String term, List<Map<String, Object>> rawResult) {
    FacetedSearchResult facetedSearchResult = new FacetedSearchResult();
    facetedSearchResult.setRawResults(rawResult);
    facetedSearchResult.setFacets(facets);
    facetedSearchResult.setTerm(term);
    facetedSearchResult.setSort(sort);
    return facetedSearchResult;
  }

  private List<Map<String, Object>> createRawResultListForIds(List<String> ids) {
    List<Map<String, Object>> list = Lists.newArrayList();
    for (String id : ids) {
      list.add(createRawResultMapForId(id));
    }
    return list;
  }

  private Map<String, Object> createRawResultMapForId(String id) {
    Map<String, Object> map = Maps.newHashMap();
    map.put(SolrFields.DOC_ID, id);
    return map;
  }

  @Before
  public void setUp() {
    rawResult = createRawResultListForIds(IDS);
    facetedSearchResult = createFacetedSearch(FACETS, SORT, SEARCH_TERM, rawResult);
  }

  @Test
  public void testConvert() {
    SearchResultConverter instance = new SearchResultConverter();
    SearchResult actualSearchResult = instance.convert(TYPE_STRING, facetedSearchResult);
    assertThat(actualSearchResult, likeSearchResult(TYPE_STRING, IDS, SEARCH_TERM, SORT, FACETS));
  }

}
