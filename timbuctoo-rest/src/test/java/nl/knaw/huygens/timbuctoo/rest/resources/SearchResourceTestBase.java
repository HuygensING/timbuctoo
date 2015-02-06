package nl.knaw.huygens.timbuctoo.rest.resources;

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

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Set;

import javax.ws.rs.core.MediaType;

import nl.knaw.huygens.solr.SearchParametersV1;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.Person;
import nl.knaw.huygens.timbuctoo.model.SearchResult;
import nl.knaw.huygens.timbuctoo.rest.util.search.RegularSearchResultMapper;
import nl.knaw.huygens.timbuctoo.rest.util.search.RelationSearchResultMapper;
import nl.knaw.huygens.timbuctoo.rest.util.search.SearchRequestValidator;
import nl.knaw.huygens.timbuctoo.vre.VRE;

import org.junit.Before;
import org.mockito.Matchers;
import org.mockito.Mockito;

import test.rest.model.TestRelation;

import com.google.common.collect.Sets;
import com.sun.jersey.api.client.WebResource;

public abstract class SearchResourceTestBase extends WebServiceTestSetup {

  protected static final Set<String> SORTABLE_FIELDS = Sets.newHashSet("test1", "test");
  protected static final String TERM = "dynamic_t_name:Huygens";
  protected static final String LOCATION_HEADER = "Location";
  protected static final String ID = "QURY0000000001";
  protected static final String RELATION_SEARCH_RESULT_TYPE = "testrelation";
  protected static final String SEARCH_RESULT_TYPE_STRING = "person";
  protected static final Class<? extends DomainEntity> SEARCH_RESULT_TYPE = Person.class;
  protected static final Class<TestRelation> TEST_RELATION_TYPE = TestRelation.class;

  protected SearchRequestValidator searchRequestValidator;
  protected RegularSearchResultMapper regularSearchResultMapperMock;
  protected RelationSearchResultMapper relationSearchResultMapperMock;

  @Before
  public void setUpSearchRequestValidator() {
    searchRequestValidator = injector.getInstance(SearchRequestValidator.class);
  }

  protected void setSearchResult(VRE vreMock, SearchResult searchResult) throws Exception {
    when(vreMock.search(Matchers.<Class<? extends DomainEntity>> any(), any(SearchParametersV1.class))).thenReturn(searchResult);
  }

  protected VRE setUpVREManager(boolean isTypeInScope, boolean isVREKnown) {
    VRE vre = null;

    if (isVREKnown) {
      vre = mock(VRE.class);
      when(vre.getVreId()).thenReturn(VRE_ID);
      when(vre.inScope(Mockito.<Class<? extends DomainEntity>> any())).thenReturn(isTypeInScope);
    }

    makeVREAvailable(vre, VRE_ID);

    return vre;
  }

  protected abstract WebResource searchResource(String... pathElements);

  protected WebResource.Builder searchResourceBuilder(String... pathElements) {
    return searchResource(pathElements).type(MediaType.APPLICATION_JSON);
  }

  @Before
  public void setUpClientSearchResultCreators() {
    regularSearchResultMapperMock = injector.getInstance(RegularSearchResultMapper.class);
    relationSearchResultMapperMock = injector.getInstance(RelationSearchResultMapper.class);
  }

}
