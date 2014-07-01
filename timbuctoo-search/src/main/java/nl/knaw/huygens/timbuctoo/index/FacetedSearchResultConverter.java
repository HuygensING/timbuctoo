package nl.knaw.huygens.timbuctoo.index;

import nl.knaw.huygens.facetedsearch.model.FacetedSearchResult;
import nl.knaw.huygens.timbuctoo.model.SearchResult;

public interface FacetedSearchResultConverter {

  SearchResult convert(String typeString, FacetedSearchResult facetedSearchResult);

}