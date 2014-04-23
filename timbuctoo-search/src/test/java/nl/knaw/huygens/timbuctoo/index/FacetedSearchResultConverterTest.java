package nl.knaw.huygens.timbuctoo.index;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.Map;

import nl.knaw.huygens.facetedsearch.model.Facet;
import nl.knaw.huygens.facetedsearch.model.FacetedSearchResult;
import nl.knaw.huygens.facetedsearch.model.parameters.SortParameter;
import nl.knaw.huygens.solr.SolrFields;
import nl.knaw.huygens.timbuctoo.model.SearchResult;

import org.junit.Test;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class FacetedSearchResultConverterTest {

  @Test
  public void testConvert() {
    // setup
    final SearchResult searchResultMock = mock(SearchResult.class);
    FacetedSearchResult facetedSearchResult = new FacetedSearchResult();

    FacetedSearchResultConverter instance = new FacetedSearchResultConverter() {
      @Override
      protected SearchResult createSearchResult() {
        return searchResultMock;
      }
    };

    List<Facet> facets = Lists.newArrayList();
    String id1 = "id1";
    String id2 = "id2";
    String id3 = "id3";
    List<SortParameter> sort = Lists.newArrayList();
    String term = "searchTerm";

    facetedSearchResult.setRawResults(createRawResultListForIds(id1, id2, id3));
    facetedSearchResult.setFacets(facets);
    facetedSearchResult.setTerm(term);
    facetedSearchResult.setSort(sort);

    List<String> ids = Lists.newArrayList(id1, id2, id3);

    String typeString = "typeString";

    // action
    SearchResult searchResult = instance.convert(typeString, facetedSearchResult);

    // verify
    verify(searchResultMock).setFacets(facets);
    verify(searchResultMock).setIds(ids);
    verify(searchResultMock).setSearchType(typeString);
    verify(searchResultMock).setSort(sort);
    verify(searchResultMock).setTerm(term);

    assertNotNull(searchResult);
  }

  private List<Map<String, Object>> createRawResultListForIds(String... ids) {
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
}
