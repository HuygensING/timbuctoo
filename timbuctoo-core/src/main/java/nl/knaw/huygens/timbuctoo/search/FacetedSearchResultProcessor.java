package nl.knaw.huygens.timbuctoo.search;

import nl.knaw.huygens.facetedsearch.model.FacetedSearchResult;

public interface FacetedSearchResultProcessor {
  /**
   * Processes a {@link FacetedSearchResult}.
   * @param facetedSearchResult the {@link FacetedSearchResult} to process
   */
  public void process(FacetedSearchResult facetedSearchResult);
}
