package nl.knaw.huygens.timbuctoo.index;

import java.util.List;
import java.util.Map;

import nl.knaw.huygens.facetedsearch.model.FacetedSearchResult;
import nl.knaw.huygens.solr.SolrFields;
import nl.knaw.huygens.timbuctoo.model.SearchResult;

import com.google.common.collect.Lists;

/**
 * Converts a FacetedSearchResult to a SearchResult, Timbuctoo uses. 
 */
public class FacetedSearchResultConverter {

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
