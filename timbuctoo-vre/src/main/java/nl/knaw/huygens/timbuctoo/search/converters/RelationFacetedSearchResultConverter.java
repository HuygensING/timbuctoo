package nl.knaw.huygens.timbuctoo.search.converters;

import nl.knaw.huygens.facetedsearch.model.FacetedSearchResult;
import nl.knaw.huygens.timbuctoo.model.SearchResult;

public class RelationFacetedSearchResultConverter extends FacetedSearchResultConverter {
  @Override
  public SearchResult convert(String typeString, FacetedSearchResult facetedSearchResult) {
    return super.convert(typeString, facetedSearchResult);
  }

  private void filter() {
    // filter the results
  }
}
