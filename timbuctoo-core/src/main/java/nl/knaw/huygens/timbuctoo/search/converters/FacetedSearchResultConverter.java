package nl.knaw.huygens.timbuctoo.search.converters;

import nl.knaw.huygens.facetedsearch.model.FacetedSearchResult;
import nl.knaw.huygens.timbuctoo.model.SearchResult;

public interface FacetedSearchResultConverter {

  public abstract SearchResult convert(String typeString, FacetedSearchResult facetedSearchResult);

}