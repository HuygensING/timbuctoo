package nl.knaw.huygens.timbuctoo.model;

/*
 * #%L
 * Timbuctoo core
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

import nl.knaw.huygens.facetedsearch.model.Facet;
import nl.knaw.huygens.facetedsearch.model.parameters.SortParameter;
import nl.knaw.huygens.timbuctoo.annotations.EntityTypeName;
import nl.knaw.huygens.timbuctoo.annotations.IDPrefix;

@IDPrefix(SearchResult.ID_PREFIX)
@EntityTypeName("search")
public class SearchResult extends SystemEntity implements Persistent {

  // Unique definition of prefix; also used in SearchResource
  public static final String ID_PREFIX = "QURY";

  private String vreId;
  /** Internal name of entity searched for. */
  private String searchType;
  private List<String> ids;
  private String term;
  private List<SortParameter> sort;
  private List<Facet> facets;

  private boolean relationSearch;
  private List<String> sourceIds;
  private List<String> targetIds;
  private List<String> relationTypeIds;

  public SearchResult() {
    relationSearch = false;
  }

  @Override
  public String getDisplayName() {
    return "Search " + getId();
  }

  public String getVreId() {
    return vreId;
  }

  public void setVreId(String vreId) {
    this.vreId = vreId;
  }

  public String getSearchType() {
    return searchType;
  }

  public void setSearchType(String type) {
    searchType = type;
  }

  public List<String> getIds() {
    return ids;
  }

  public void setIds(List<String> ids) {
    this.ids = ids;
  }

  public String getTerm() {
    return term;
  }

  public void setTerm(String term) {
    this.term = term;
  }

  public List<SortParameter> getSort() {
    return sort;
  }

  public void setSort(List<SortParameter> sort) {
    this.sort = sort;
  }

  public List<Facet> getFacets() {
    return facets;
  }

  public void setFacets(List<Facet> facets) {
    this.facets = facets;
  }

  // -- relation search --------------------------------------------------------

  public boolean isRelationSearch() {
    return relationSearch;
  }

  public void setRelationSearch(boolean relationSearch) {
    this.relationSearch = relationSearch;
  }

  public List<String> getSourceIds() {
    return sourceIds;
  }

  public void setSourceIds(List<String> ids) {
    sourceIds = ids;
  }

  public List<String> getTargetIds() {
    return targetIds;
  }

  public void setTargetIds(List<String> ids) {
    targetIds = ids;
  }

  public List<String> getRelationTypeIds() {
    return relationTypeIds;
  }

  public void setRelationTypeIds(List<String> ids) {
    relationTypeIds = ids;
  }

}
