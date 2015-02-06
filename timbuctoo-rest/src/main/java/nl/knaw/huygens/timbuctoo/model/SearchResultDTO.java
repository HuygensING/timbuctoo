package nl.knaw.huygens.timbuctoo.model;

/*
 * #%L
 * Timbuctoo REST api
 * =======
 * Copyright (C) 2012 - 2015 Huygens ING
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
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

public abstract class SearchResultDTO {

  private static final String PREV_KEY = "_prev";
  private static final String NEXT_KEY = "_next";

  private Set<String> sortableFields;
  private int numFound;
  private List<? extends DomainEntity> results;
  private List<String> ids;
  private int start;
  private int rows;
  private String nextLink;
  private String prevLink;

  public List<String> getIds() {
    return ids;
  }

  public void setIds(List<String> ids) {
    this.ids = ids;
  }

  @Deprecated
  public List<? extends DomainEntity> getResults() {
    return results;
  }

  @Deprecated
  public void setResults(List<? extends DomainEntity> results) {
    this.results = results;
  }

  public int getNumFound() {
    return numFound;
  }

  public void setNumFound(int numFound) {
    this.numFound = numFound;
  }

  public int getStart() {
    return start;
  }

  public void setStart(int start) {
    this.start = start;
  }

  public int getRows() {
    return rows;
  }

  public void setRows(int rows) {
    this.rows = rows;
  }

  public Set<String> getSortableFields() {
    return sortableFields;
  }

  public void setSortableFields(Set<String> sortableFields) {
    this.sortableFields = sortableFields;
  }

  public String getNextLink() {
    return nextLink;
  }

  @JsonProperty(NEXT_KEY)
  @JsonInclude(value = Include.NON_NULL)
  public void setNextLink(String nextLink) {
    this.nextLink = nextLink;
  }

  public String getPrevLink() {
    return prevLink;
  }

  @JsonProperty(PREV_KEY)
  @JsonInclude(value = Include.NON_NULL)
  public void setPrevLink(String prevLink) {
    this.prevLink = prevLink;
  }

}
