package nl.knaw.huygens.timbuctoo.search.converters;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import nl.knaw.huygens.facetedsearch.model.Facet;
import nl.knaw.huygens.facetedsearch.model.FacetedSearchResult;
import nl.knaw.huygens.facetedsearch.model.parameters.SortParameter;
import nl.knaw.huygens.solr.SolrFields;

import org.junit.Before;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public abstract class FacetedSearchResultConverterTestBase {

  protected static final ArrayList<String> IDS = Lists.newArrayList("id1", "id2", "id3");
  protected static final String SEARCH_TERM = "searchTerm";
  protected static final String TYPE_STRING = "typeString";
  protected static final List<Facet> FACETS = Lists.newArrayList();
  protected static final List<SortParameter> SORT = Lists.newArrayList();
  private List<Map<String, Object>> rawResult;
  protected FacetedSearchResult facetedSearchResult;

  protected FacetedSearchResult createFacetedSearch(List<Facet> facets, List<SortParameter> sort, String term, List<Map<String, Object>> rawResult) {
    FacetedSearchResult facetedSearchResult = new FacetedSearchResult();
    facetedSearchResult.setRawResults(rawResult);
    facetedSearchResult.setFacets(facets);
    facetedSearchResult.setTerm(term);
    facetedSearchResult.setSort(sort);
    return facetedSearchResult;
  }

  protected List<Map<String, Object>> createRawResultListForIds(List<String> ids) {
    List<Map<String, Object>> list = Lists.newArrayList();

    for (String id : ids) {
      list.add(createRawResultMapForId(id));
    }

    return list;
  }

  protected Map<String, Object> createRawResultMapForId(String id) {
    Map<String, Object> map = Maps.newHashMap();

    map.put(SolrFields.DOC_ID, id);

    return map;
  }

  @Before
  public void setUp() {
    rawResult = createRawResultListForIds(IDS);
    facetedSearchResult = createFacetedSearch(FACETS, SORT, SEARCH_TERM, rawResult);
  }

}