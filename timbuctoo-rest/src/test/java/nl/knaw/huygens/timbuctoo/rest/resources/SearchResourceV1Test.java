package nl.knaw.huygens.timbuctoo.rest.resources;

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

import static com.sun.jersey.api.client.ClientResponse.Status.BAD_REQUEST;
import static com.sun.jersey.api.client.ClientResponse.Status.CREATED;
import static com.sun.jersey.api.client.ClientResponse.Status.INTERNAL_SERVER_ERROR;
import static nl.knaw.huygens.timbuctoo.rest.util.CustomHeaders.VRE_ID_KEY;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.isNotNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import nl.knaw.huygens.solr.RelationSearchParameters;
import nl.knaw.huygens.solr.SearchParametersV1;
import nl.knaw.huygens.timbuctoo.config.Configuration;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.RegularClientSearchResult;
import nl.knaw.huygens.timbuctoo.model.Relation;
import nl.knaw.huygens.timbuctoo.model.RelationClientSearchResult;
import nl.knaw.huygens.timbuctoo.model.SearchResult;
import nl.knaw.huygens.timbuctoo.rest.TimbuctooException;
import nl.knaw.huygens.timbuctoo.search.RelationSearcher;
import nl.knaw.huygens.timbuctoo.storage.StorageException;
import nl.knaw.huygens.timbuctoo.storage.ValidationException;
import nl.knaw.huygens.timbuctoo.vre.SearchException;
import nl.knaw.huygens.timbuctoo.vre.SearchValidationException;
import nl.knaw.huygens.timbuctoo.vre.VRE;

import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.ClientResponse.Status;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.core.util.MultivaluedMapImpl;

public class SearchResourceV1Test extends SearchResourceTestBase {

  private static final Class<Class<? extends Relation>> RELATION_TYPE = new GenericType<Class<? extends Relation>>() {}.getRawClass();
  private static final String V1_PREFIX = "v1";
  private static final String TYPE_STRING = "persons";
  protected static final String RELATION_TYPE_STRING = "testrelations";
  private static final String RELATION_SEARCH_RESULT_TYPE = "testrelation";
  private void setupPublicUrl(String url) {
    when(injector.getInstance(Configuration.class).getSetting("public_url")).thenReturn(url);
  }

  @Override
  protected WebResource searchResource(String... pathElements) {
    WebResource resource = resource().path(V1_PREFIX).path("search");
    for (String pathElement : pathElements) {
      resource = resource.path(pathElement);
    }
    return resource;
  }

  @Test
  public void testPostSuccess() throws Exception {
    VRE vreMock = setUpVREManager(true, true);

    SearchParametersV1 params = new SearchParametersV1().setTerm(TERM);
    SearchResult searchResult = mock(SearchResult.class);
    setSearchResult(vreMock, searchResult);
    when(repository.addSystemEntity(SearchResult.class, searchResult)).thenReturn(ID);

    setupPublicUrl(resource().getURI().toString());

    String expected = getExpectedURL(ID);
    ClientResponse response = searchResourceBuilder(TYPE_STRING).header(VRE_ID_KEY, VRE_ID).post(ClientResponse.class, params);
    String actual = response.getHeaders().getFirst(LOCATION_HEADER);

    assertEquals(Status.CREATED, response.getClientResponseStatus());
    assertEquals(expected, actual);
    verify(vreManager).getVREById(anyString());
    verifyVRESearchIsCalled(vreMock);
  }

  protected String getExpectedURL(String id) {
    return String.format("%s%s/search/%s", resource().getURI().toString(), V1_PREFIX, id);
  }

  @Test
  public void testPostRequestInvalid() {
    // setup
    SearchParametersV1 SearchParametersV1 = new SearchParametersV1();
    doThrow(new TimbuctooException(Response.Status.BAD_REQUEST, "Error")).when(searchRequestValidator).validate(anyString(), anyString(), any(SearchParametersV1.class));

    // action
    ClientResponse response = searchResourceBuilder(TYPE_STRING).header(VRE_ID_KEY, VRE_ID).post(ClientResponse.class, SearchParametersV1);

    // verify
    assertThat(response.getClientResponseStatus(), equalTo(Status.BAD_REQUEST));
    verifyZeroInteractions(repository, vreManager);

  }

  @Test
  public void testPostVREThrowsAnException() throws Exception {
    VRE vreMock = setUpVREManager(true, true);

    SearchParametersV1 params = new SearchParametersV1().setTerm(TERM);
    doThrow(Exception.class).when(vreMock).search(Matchers.<Class<? extends DomainEntity>> any(), any(SearchParametersV1.class));

    ClientResponse response = searchResourceBuilder(TYPE_STRING).header(VRE_ID_KEY, VRE_ID).post(ClientResponse.class, params);

    assertEquals(Status.INTERNAL_SERVER_ERROR, response.getClientResponseStatus());
    verify(repository, never()).addSystemEntity(Matchers.<Class<SearchResult>> any(), any(SearchResult.class));
    verifyVRESearchIsCalled(vreMock);
  }

  protected void verifyVRESearchIsCalled(VRE vreMock) throws SearchException, SearchValidationException {
    verify(vreMock).search(Mockito.<Class<? extends DomainEntity>> any(), any(SearchParametersV1.class));
  }

  @Test
  public void testPostStorageManagerThrowsAnException() throws Exception {
    VRE vreMock = setUpVREManager(true, true);

    SearchParametersV1 params = new SearchParametersV1().setTerm(TERM);
    SearchResult searchResult = mock(SearchResult.class);
    setSearchResult(vreMock, searchResult);
    doThrow(IOException.class).when(repository).addSystemEntity(Matchers.<Class<SearchResult>> any(), any(SearchResult.class));

    ClientResponse response = searchResourceBuilder(TYPE_STRING).header(VRE_ID_KEY, VRE_ID).post(ClientResponse.class, params);

    assertEquals(Status.INTERNAL_SERVER_ERROR, response.getClientResponseStatus());
    verify(vreManager).getVREById(anyString());
    verifyVRESearchIsCalled(vreMock);
  }

  @Test
  public void testGetSuccess() {
    // setup
    SearchResult searchResult = new SearchResult();
    searchResult.setSearchType(SEARCH_RESULT_TYPE_STRING);

    RegularClientSearchResult clientSearchResult = new RegularClientSearchResult();

    final int defaultStart = 0;
    final int defaultRows = 10;

    when(repository.getEntity(SearchResult.class, ID)).thenReturn(searchResult);
    when(regularClientSearchResultCreatorMock.create(SEARCH_RESULT_TYPE, searchResult, defaultStart, defaultRows)).thenReturn(clientSearchResult);

    // action
    ClientResponse response = searchResourceBuilder(ID).accept(MediaType.APPLICATION_JSON_TYPE).get(ClientResponse.class);

    // verify
    assertThat(response.getClientResponseStatus(), equalTo(Status.OK));

    RegularClientSearchResult actualResult = response.getEntity(RegularClientSearchResult.class);
    assertThat(actualResult, notNullValue(RegularClientSearchResult.class));

    verify(repository).getEntity(SearchResult.class, ID);
    verify(regularClientSearchResultCreatorMock).create(SEARCH_RESULT_TYPE, searchResult, defaultStart, defaultRows);

  }

  @Test
  public void testGetSuccessWithStartAndRows() {
    // setup
    int startIndex = 20;
    int numberOfRows = 20;

    SearchResult searchResult = new SearchResult();
    searchResult.setSearchType(SEARCH_RESULT_TYPE_STRING);

    RegularClientSearchResult clientSearchResult = new RegularClientSearchResult();

    MultivaluedMap<String, String> queryParameters = new MultivaluedMapImpl();
    queryParameters.add("start", "20");
    queryParameters.add("rows", "20");

    when(repository.getEntity(SearchResult.class, ID)).thenReturn(searchResult);
    when(regularClientSearchResultCreatorMock.create(SEARCH_RESULT_TYPE, searchResult, startIndex, numberOfRows)).thenReturn(clientSearchResult);

    // action
    ClientResponse clientResponse = searchResource(ID).queryParams(queryParameters).type(MediaType.APPLICATION_JSON_TYPE).accept(MediaType.APPLICATION_JSON_TYPE).get(ClientResponse.class);

    // verify
    assertThat(clientResponse.getClientResponseStatus(), equalTo(Status.OK));

    RegularClientSearchResult actualResult = clientResponse.getEntity(RegularClientSearchResult.class);
    assertThat(actualResult, notNullValue(RegularClientSearchResult.class));

    verify(repository).getEntity(SearchResult.class, ID);
    verify(regularClientSearchResultCreatorMock).create(SEARCH_RESULT_TYPE, searchResult, startIndex, numberOfRows);
  }

  @Test
  public void testGetRelationSearch() {
    // setup
    SearchResult searchResult = new SearchResult();
    searchResult.setSearchType(RELATION_SEARCH_RESULT_TYPE);

    RelationClientSearchResult clientSearchResult = new RelationClientSearchResult();

    final int defaultStart = 0;
    final int defaultRows = 10;

    when(repository.getEntity(SearchResult.class, ID)).thenReturn(searchResult);
    when(relationClientSearchResultCreatorMock.create(TEST_RELATION_TYPE, searchResult, defaultStart, defaultRows)).thenReturn(clientSearchResult);

    // action
    ClientResponse response = searchResourceBuilder(ID).accept(MediaType.APPLICATION_JSON_TYPE).get(ClientResponse.class);

    // verify
    assertThat(response.getClientResponseStatus(), equalTo(Status.OK));

    RegularClientSearchResult actualResult = response.getEntity(RegularClientSearchResult.class);
    assertThat(actualResult, notNullValue(RegularClientSearchResult.class));

    verify(repository).getEntity(SearchResult.class, ID);
    verify(relationClientSearchResultCreatorMock).create(TEST_RELATION_TYPE, searchResult, defaultStart, defaultRows);
  }

  @Test
  public void testGetNoId() {
    ClientResponse response = searchResourceBuilder().accept(MediaType.APPLICATION_JSON_TYPE).get(ClientResponse.class);

    assertEquals(Status.METHOD_NOT_ALLOWED, response.getClientResponseStatus());
  }

  @Test
  public void testGetUnknownId() {
    when(repository.getEntity(SearchResult.class, ID)).thenReturn(null);

    ClientResponse response = searchResourceBuilder(ID).accept(MediaType.APPLICATION_JSON_TYPE).get(ClientResponse.class);

    assertEquals(Status.NOT_FOUND, response.getClientResponseStatus());
  }

  @Test
  public void testGetSearchTypeUnknown() {
    SearchResult searchResult = new SearchResult();
    searchResult.setId(ID);
    String unknownType = "unknown";
    searchResult.setSearchType(unknownType);

    when(repository.getEntity(SearchResult.class, ID)).thenReturn(searchResult);

    ClientResponse response = searchResourceBuilder(ID).accept(MediaType.APPLICATION_JSON_TYPE).get(ClientResponse.class);

    assertEquals(Status.BAD_REQUEST, response.getClientResponseStatus());
  }

  /*
   * ****************************************************************************
   * Reception Search                                                           *
   * ****************************************************************************
   */

  @Test
  public void aSuccessfulRelationSearchPostShouldResponseWithStatusCodeCreatedandALocationHeader() throws SearchException, SearchValidationException, StorageException, ValidationException {
    // setup
    setupPublicUrl(resource().getURI().toString());
    String expectedLocationHeader = getRelationSearchURL(ID);

    RelationSearchParameters relationSearchParameters = new RelationSearchParameters();

    final VRE vreMock = mock(VRE.class);
    final SearchResult searchResultMock = mock(SearchResult.class);

    RelationSearcher relationSearcher = injector.getInstance(RelationSearcher.class);

    when(vreManager.getVREById(VRE_ID)).thenReturn(vreMock);
    when(relationSearcher.search(any(VRE.class), isNotNull(new GenericType<Class<? extends DomainEntity>>() {}.getRawClass()), any(RelationSearchParameters.class))).thenReturn(searchResultMock);
    when(repository.addSystemEntity(SearchResult.class, searchResultMock)).thenReturn(ID);

    // action
    ClientResponse response = searchResourceBuilder(RELATION_TYPE_STRING).header(VRE_ID_KEY, VRE_ID).post(ClientResponse.class, relationSearchParameters);

    // verify
    assertThat(response.getClientResponseStatus(), equalTo(CREATED));
    assertThat(response.getLocation().toString(), equalTo(expectedLocationHeader));

    verify(searchRequestValidator).validateRelationRequest(anyString(), anyString(), any(RelationSearchParameters.class));
    verify(relationSearcher).search(isNotNull(VRE.class), isNotNull(RELATION_TYPE), isNotNull(RelationSearchParameters.class));
    verify(repository).addSystemEntity(SearchResult.class, searchResultMock);

  }

  @Test
  public void anInvalidSearchRequestPostShouldRespondWithABadRequestStatus() throws StorageException, ValidationException {
    // setup
    RelationSearchParameters relationSearchParameters = new RelationSearchParameters();

    RelationSearcher relationSearcher = injector.getInstance(RelationSearcher.class);

    doThrow(new TimbuctooException(Response.Status.BAD_REQUEST, "Error")).when(searchRequestValidator).validateRelationRequest(anyString(), anyString(), any(RelationSearchParameters.class));

    // action
    ClientResponse response = searchResourceBuilder(RELATION_TYPE_STRING).type(MediaType.APPLICATION_JSON).header(VRE_ID_KEY, VRE_ID).post(ClientResponse.class, relationSearchParameters);

    // verify
    assertThat(response.getClientResponseStatus(), equalTo(BAD_REQUEST));
    verifyZeroInteractions(vreManager, repository, relationSearcher);
  }

  @Test
  public void whenTheRepositoryCannotStoreTheRelationSearchResultAnInternalServerErrorShouldBeReturned() throws StorageException, ValidationException, Exception {
    // setup
    RelationSearchParameters relationSearchParameters = new RelationSearchParameters();

    final VRE vreMock = mock(VRE.class);
    final SearchResult searchResultMock = mock(SearchResult.class);

    RelationSearcher relationSearcher = injector.getInstance(RelationSearcher.class);

    when(vreManager.getVREById(VRE_ID)).thenReturn(vreMock);
    when(relationSearcher.search(any(VRE.class), isNotNull(RELATION_TYPE), any(RelationSearchParameters.class))).thenReturn(searchResultMock);
    doThrow(Exception.class).when(repository).addSystemEntity(SearchResult.class, searchResultMock);

    // action
    ClientResponse response = searchResourceBuilder(RELATION_TYPE_STRING).type(MediaType.APPLICATION_JSON).header(VRE_ID_KEY, VRE_ID).post(ClientResponse.class, relationSearchParameters);

    // verify
    assertThat(response.getClientResponseStatus(), equalTo(INTERNAL_SERVER_ERROR));

    verify(searchRequestValidator).validateRelationRequest(anyString(), anyString(), any(RelationSearchParameters.class));
    verify(relationSearcher).search(any(VRE.class), isNotNull(RELATION_TYPE), any(RelationSearchParameters.class));
    verify(repository).addSystemEntity(SearchResult.class, searchResultMock);
  }

  @Test
  public void whenTheRelationSearcherThrowsAnSearchExceptionAnInternalServerErrorShouldBeReturned() throws StorageException, ValidationException, Exception {
    // setup
    RelationSearchParameters relationSearchParameters = new RelationSearchParameters();

    final VRE vreMock = mock(VRE.class);
    RelationSearcher relationSearcher = injector.getInstance(RelationSearcher.class);

    when(vreManager.getVREById(VRE_ID)).thenReturn(vreMock);
    doThrow(SearchException.class).when(relationSearcher).search(any(VRE.class), isNotNull(RELATION_TYPE), any(RelationSearchParameters.class));

    // action
    ClientResponse response = searchResourceBuilder(RELATION_TYPE_STRING).type(MediaType.APPLICATION_JSON).header(VRE_ID_KEY, VRE_ID).post(ClientResponse.class, relationSearchParameters);

    // verify
    assertThat(response.getClientResponseStatus(), equalTo(INTERNAL_SERVER_ERROR));

    verify(searchRequestValidator).validateRelationRequest(anyString(), anyString(), any(RelationSearchParameters.class));
    verify(relationSearcher).search(any(VRE.class), isNotNull(RELATION_TYPE), any(RelationSearchParameters.class));
    verifyZeroInteractions(repository);
  }

  private String getRelationSearchURL(String id) {
    return String.format(//
        "%s%s/search/%s", //
        resource().getURI().toString(), //
        V1_PREFIX, //
        id);
  }

}
