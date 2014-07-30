package nl.knaw.huygens.timbuctoo.search;

import nl.knaw.huygens.facetedsearch.model.FacetedSearchResult;

public interface FacetedSearchResultProcessor {
  /**
   * Processes a {@code FacetedSearchResult} and returns the processed one.
   * @param facetedSearchResult the {@code FacetedSearchResult} to process
   * @return a processed {@code FacetedSearchResult}.
   */
  public FacetedSearchResult process(FacetedSearchResult facetedSearchResult);
}
