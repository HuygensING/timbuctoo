package nl.knaw.huygens.timbuctoo.search.converters;

import java.util.List;

import nl.knaw.huygens.facetedsearch.model.FacetedSearchResult;
import nl.knaw.huygens.timbuctoo.model.SearchResult;

public class RelationFacetedSearchResultConverter extends RegularFacetedSearchResultConverter {
  private final List<String> sourceSearchIds;
  private final List<String> targetSearchIds;

  public RelationFacetedSearchResultConverter(List<String> sourceSearchIds, List<String> targetSearchIds) {
    this.sourceSearchIds = sourceSearchIds;
    this.targetSearchIds = targetSearchIds;
  }

  @Override
  public SearchResult convert(String typeString, FacetedSearchResult facetedSearchResult) {
    SearchResult searchResult = super.convert(typeString, facetedSearchResult);
    searchResult.setSourceIds(sourceSearchIds);
    searchResult.setTargetIds(targetSearchIds);
    searchResult.setRelationSearch(true);
    return searchResult;
  }

}
