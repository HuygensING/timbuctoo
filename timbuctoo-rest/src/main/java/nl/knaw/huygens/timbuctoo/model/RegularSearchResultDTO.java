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

import nl.knaw.huygens.facetedsearch.model.Facet;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.List;
import java.util.Set;

public class RegularSearchResultDTO extends SearchResultDTO {

  private String term;
  private List<Facet> facets;
  private List<DomainEntityDTO> refs;
  private Set<String> fullTextSearchFields;

  public String getTerm() {
    return term;
  }

  public void setTerm(String term) {
    this.term = term;
  }

  public List<Facet> getFacets() {
    return facets;
  }

  public void setFacets(List<Facet> facets) {
    this.facets = facets;
  }

  public List<DomainEntityDTO> getRefs() {
    return refs;
  }

  public void setRefs(List<DomainEntityDTO> refs) {
    this.refs = refs;
  }

  public Set<String> getFullTextSearchFields() {
    return fullTextSearchFields;
  }

  public void setFullTextSearchFields(Set<String> fields) {
    this.fullTextSearchFields = fields;
  }


  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this);
  }
}
