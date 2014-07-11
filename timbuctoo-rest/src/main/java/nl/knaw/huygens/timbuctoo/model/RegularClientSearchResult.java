package nl.knaw.huygens.timbuctoo.model;

/*
 * #%L
 * Timbuctoo REST api
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

public class RegularClientSearchResult extends ClientSearchResult {

  private String term;
  private List<Facet> facets;
  private List<ClientEntityRepresentation> refs;

  public String getTerm() {
    return term;
  }

  public List<Facet> getFacets() {
    return facets;
  }

  public List<ClientEntityRepresentation> getRefs() {
    return refs;
  }

  public void setRefs(List<ClientEntityRepresentation> refs) {
    this.refs = refs;
  }

  public void setFacets(List<Facet> facets) {
    this.facets = facets;
  }

  public void setTerm(String term) {
    this.term = term;
  }

}
