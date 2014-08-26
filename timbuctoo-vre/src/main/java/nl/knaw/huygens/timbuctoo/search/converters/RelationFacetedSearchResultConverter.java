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

import java.util.List;

import nl.knaw.huygens.facetedsearch.model.FacetedSearchResult;
import nl.knaw.huygens.timbuctoo.model.SearchResult;

public class RelationFacetedSearchResultConverter extends RegularFacetedSearchResultConverter {
  private final List<String> sourceSearchIds;
  private final List<String> targetSearchIds;
  private final List<String> relationTypeIds;

  public RelationFacetedSearchResultConverter(List<String> sourceSearchIds, List<String> targetSearchIds, List<String> relationTypeIds) {
    this.sourceSearchIds = sourceSearchIds;
    this.targetSearchIds = targetSearchIds;
    this.relationTypeIds = relationTypeIds;
  }

  @Override
  public SearchResult convert(String typeString, FacetedSearchResult facetedSearchResult) {
    SearchResult searchResult = super.convert(typeString, facetedSearchResult);
    searchResult.setSourceIds(sourceSearchIds);
    searchResult.setTargetIds(targetSearchIds);
    searchResult.setRelationTypeIds(relationTypeIds);
    searchResult.setRelationSearch(true);
    return searchResult;
  }

}
