package nl.knaw.huygens.timbuctoo.rest.model;

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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;

import java.util.List;

import nl.knaw.huygens.facetedsearch.model.DefaultFacet;
import nl.knaw.huygens.facetedsearch.model.Facet;
import nl.knaw.huygens.timbuctoo.model.ClientEntityRepresentation;
import nl.knaw.huygens.timbuctoo.model.ClientSearchResult;
import nl.knaw.huygens.timbuctoo.model.RegularClientSearchResult;

import org.junit.Test;

import test.model.BaseDomainEntity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.Lists;

public class RegularClientSearchResultSerializationTest extends ClientSearchResultTest {

  @Test
  public void testWhenObjectHasAllEmptyProperties() throws JsonProcessingException {
    ClientSearchResult result = new RegularClientSearchResult();
    assertThat(getKeySet(result), contains("sortableFields", "numFound", "results", "ids", "start", "rows", "term", "facets", "refs"));
  }

  @Test
  public void testPropertiesWhenAllPropertiesContainAValue() {
    ClientSearchResult result = createFilledSearchResult();
    assertThat(getKeySet(result), contains("sortableFields", "numFound", "results", "ids", "start", "rows", "term", "facets", "refs", "_next", "_prev"));
  }

  private RegularClientSearchResult createFilledSearchResult() {
    RegularClientSearchResult searchResult = new RegularClientSearchResult();
    setClientRelationSearchResultProperties(searchResult);
    searchResult.setRefs(createRefList());
    searchResult.setFacets(createFacetList());
    searchResult.setTerm(ANY_STRING);
    return searchResult;
  }

  private List<ClientEntityRepresentation> createRefList() {
    BaseDomainEntity entity = new BaseDomainEntity("id");
    return Lists.newArrayList(new ClientEntityRepresentation(ANY_STRING, ANY_STRING, entity));
  }

  private List<Facet> createFacetList() {
    Facet facet = new DefaultFacet(ANY_STRING, ANY_STRING);
    return Lists.newArrayList(facet);
  }

}
