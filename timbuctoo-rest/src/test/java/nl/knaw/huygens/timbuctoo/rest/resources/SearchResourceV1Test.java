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

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.ClientResponse.Status;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import nl.knaw.huygens.solr.SearchParametersV1;
import nl.knaw.huygens.timbuctoo.config.Configuration;
import nl.knaw.huygens.timbuctoo.config.Paths;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.RegularSearchResultDTO;
import nl.knaw.huygens.timbuctoo.model.Relation;
import nl.knaw.huygens.timbuctoo.model.RelationSearchResultDTO;
import nl.knaw.huygens.timbuctoo.model.SearchResult;
import nl.knaw.huygens.timbuctoo.rest.TimbuctooException;
import nl.knaw.huygens.timbuctoo.search.RelationSearcher;
import nl.knaw.huygens.timbuctoo.vre.RelationSearchParameters;
import nl.knaw.huygens.timbuctoo.vre.SearchException;
import nl.knaw.huygens.timbuctoo.vre.SearchValidationException;
import nl.knaw.huygens.timbuctoo.vre.VRE;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.io.IOException;

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

public class SearchResourceV1Test extends SearchResourceTestBase {

  protected static final Class<Class<? extends Relation>> RELATION_TYPE = new GenericType<Class<? extends Relation>>() {
  }.getRawClass();
  private static final String TYPE_STRING = "persons";
  private static final String RELATION_TYPE_STRING = "testrelations";
  private static final String RELATION_SEARCH_RESULT_TYPE = "testrelation";

  protected void setupPublicUrl(String url) {
    when(injector.getInstance(Configuration.class).getSetting("public_url")).thenReturn(url);
  }

  @Override
  protected WebResource searchResource(String... pathElements) {
    return addPathToWebResource(resource().path(getAPIVersion()).path("search"), pathElements);
  }

  /**
   * Return the NULL_VERSION of the api to test.
   */
  @Override
  protected String getAPIVersion() {
    return Paths.V1_PATH;
  }

  @Test
  public void testPostSuccess() throws Exception {
    VRE vreMock = setUpVREManager(true, true);

    SearchParametersV1 params = new SearchParametersV1().setTerm(TERM);
    SearchResult searchResult = mock(SearchResult.class);
    setSearchResult(vreMock, searchResult);
    when(repository.addSystemEntity(SearchResult.class, searchResult)).thenReturn(ID);

    setupPublicUrl(resource().getURI().toString());

    ClientResponse response = searchResourceBuilder(TYPE_STRING) //
      .header(VRE_ID_KEY, VRE_ID) //
      .post(ClientResponse.class, params);
    verifyResponseStatus(response, Status.CREATED);

    String expected = getExpectedURL(ID);
    String actual = response.getHeaders().getFirst(LOCATION_HEADER);
    assertEquals(expected, actual);
    verifyVRESearchIsCalled(vreMock);
  }

  protected String getExpectedURL(String id) {
    return String.format("%s%ssearch/%s", resource().getURI().toString(), getAPIVersion(), id);
  }

  @Test
  public void testPostRequestInvalid() {
    SearchParametersV1 parameters = new SearchParametersV1();
    doThrow(new TimbuctooException(Response.Status.BAD_REQUEST, "Error")).when(searchRequestValidator).validate(anyString(), anyString(), any(SearchParametersV1.class));

    ClientResponse response = searchResourceBuilder(TYPE_STRING) //
      .header(VRE_ID_KEY, VRE_ID) //
      .post(ClientResponse.class, parameters);
    verifyResponseStatus(response, Status.BAD_REQUEST);

    verifyZeroInteractions(repository);
  }

  @Test
  public void testPostVREThrowsAnException() throws Exception {
    VRE vreMock = setUpVREManager(true, true);

    SearchParametersV1 params = new SearchParametersV1().setTerm(TERM);
    doThrow(Exception.class).when(vreMock).search(Matchers.<Class<? extends DomainEntity>>any(), any(SearchParametersV1.class));

    ClientResponse response = searchResourceBuilder(TYPE_STRING) //
      .header(VRE_ID_KEY, VRE_ID) //
      .post(ClientResponse.class, params);
    verifyResponseStatus(response, Status.INTERNAL_SERVER_ERROR);

    verify(repository, never()).addSystemEntity(Matchers.<Class<SearchResult>>any(), any(SearchResult.class));
    verifyVRESearchIsCalled(vreMock);
  }

  protected void verifyVRESearchIsCalled(VRE vreMock) throws SearchException, SearchValidationException {
    verify(vreMock).search(Mockito.<Class<? extends DomainEntity>>any(), any(SearchParametersV1.class));
  }

  @Test
  public void testPostStorageManagerThrowsAnException() throws Exception {
    VRE vreMock = setUpVREManager(true, true);

    SearchParametersV1 params = new SearchParametersV1().setTerm(TERM);
    SearchResult searchResult = mock(SearchResult.class);
    setSearchResult(vreMock, searchResult);
    doThrow(IOException.class).when(repository).addSystemEntity(Matchers.<Class<SearchResult>>any(), any(SearchResult.class));

    ClientResponse response = searchResourceBuilder(TYPE_STRING) //
      .header(VRE_ID_KEY, VRE_ID) //
      .post(ClientResponse.class, params);
    verifyResponseStatus(response, Status.INTERNAL_SERVER_ERROR);

    verifyVRESearchIsCalled(vreMock);
  }

  @Test
  public void testGetSuccess() {
    SearchResult searchResult = new SearchResult();
    searchResult.setSearchType(SEARCH_RESULT_TYPE_STRING);

    RegularSearchResultDTO clientSearchResult = new RegularSearchResultDTO();

    int defaultStart = 0;
    int defaultRows = 10;

    when(repository.getEntityOrDefaultVariation(SearchResult.class, ID)).thenReturn(searchResult);
    when(regularSearchResultMapperMock.create(SEARCH_RESULT_TYPE, searchResult, defaultStart, defaultRows, getAPIVersion())).thenReturn(clientSearchResult);

    ClientResponse response = searchResourceBuilder(ID) //
      .accept(MediaType.APPLICATION_JSON_TYPE).get(ClientResponse.class);
    verifyResponseStatus(response, Status.OK);

    RegularSearchResultDTO actualResult = response.getEntity(RegularSearchResultDTO.class);
    assertThat(actualResult, notNullValue(RegularSearchResultDTO.class));

    verify(repository).getEntityOrDefaultVariation(SearchResult.class, ID);
    verify(regularSearchResultMapperMock).create(SEARCH_RESULT_TYPE, searchResult, defaultStart, defaultRows, getAPIVersion());
  }

  @Test
  public void testGetSuccessWithStartAndRows() {
    int startIndex = 20;
    int numberOfRows = 20;

    SearchResult searchResult = new SearchResult();
    searchResult.setSearchType(SEARCH_RESULT_TYPE_STRING);

    RegularSearchResultDTO clientSearchResult = new RegularSearchResultDTO();

    MultivaluedMap<String, String> queryParameters = new MultivaluedMapImpl();
    queryParameters.add("start", "20");
    queryParameters.add("rows", "20");

    when(repository.getEntityOrDefaultVariation(SearchResult.class, ID)).thenReturn(searchResult);
    when(regularSearchResultMapperMock.create(SEARCH_RESULT_TYPE, searchResult, startIndex, numberOfRows, getAPIVersion())).thenReturn(clientSearchResult);

    ClientResponse clientResponse = searchResource(ID) //
      .queryParams(queryParameters) //
      .type(MediaType.APPLICATION_JSON_TYPE) //
      .accept(MediaType.APPLICATION_JSON_TYPE) //
      .get(ClientResponse.class);
    verifyResponseStatus(clientResponse, Status.OK);

    RegularSearchResultDTO actualResult = clientResponse.getEntity(RegularSearchResultDTO.class);
    assertThat(actualResult, notNullValue(RegularSearchResultDTO.class));

    verify(repository).getEntityOrDefaultVariation(SearchResult.class, ID);
    verify(regularSearchResultMapperMock).create(SEARCH_RESULT_TYPE, searchResult, startIndex, numberOfRows, getAPIVersion());
  }

  @Test
  public void testGetRelationSearch() {
    SearchResult searchResult = new SearchResult();
    searchResult.setSearchType(RELATION_SEARCH_RESULT_TYPE);

    RelationSearchResultDTO clientSearchResult = new RelationSearchResultDTO();

    int defaultStart = 0;
    int defaultRows = 10;

    when(repository.getEntityOrDefaultVariation(SearchResult.class, ID)).thenReturn(searchResult);
    when(relationSearchResultMapperMock.create(TEST_RELATION_TYPE, searchResult, defaultStart, defaultRows, getAPIVersion())).thenReturn(clientSearchResult);

    ClientResponse response = searchResourceBuilder(ID) //
      .accept(MediaType.APPLICATION_JSON_TYPE) //
      .get(ClientResponse.class);
    verifyResponseStatus(response, Status.OK);

    RelationSearchResultDTO actualResult = response.getEntity(RelationSearchResultDTO.class);
    assertThat(actualResult, notNullValue(RelationSearchResultDTO.class));

    verify(repository).getEntityOrDefaultVariation(SearchResult.class, ID);
    verify(relationSearchResultMapperMock).create(TEST_RELATION_TYPE, searchResult, defaultStart, defaultRows, getAPIVersion());
  }

  @Test
  public void testGetNoId() {
    ClientResponse response = searchResourceBuilder() //
      .accept(MediaType.APPLICATION_JSON_TYPE).get(ClientResponse.class);
    verifyResponseStatus(response, Status.METHOD_NOT_ALLOWED);
  }

  @Test
  public void testGetUnknownId() {
    when(repository.getEntityOrDefaultVariation(SearchResult.class, ID)).thenReturn(null);

    ClientResponse response = searchResourceBuilder(ID).accept(MediaType.APPLICATION_JSON_TYPE).get(ClientResponse.class);
    verifyResponseStatus(response, Status.NOT_FOUND);
  }

  @Test
  public void testGetSearchTypeUnknown() {
    SearchResult searchResult = new SearchResult();
    searchResult.setId(ID);
    searchResult.setSearchType("unknown");

    when(repository.getEntityOrDefaultVariation(SearchResult.class, ID)).thenReturn(searchResult);

    ClientResponse response = searchResourceBuilder(ID) //
      .accept(MediaType.APPLICATION_JSON_TYPE) //
      .get(ClientResponse.class);
    verifyResponseStatus(response, Status.BAD_REQUEST);
  }

  /*
   * ****************************************************************************
   * Reception Search                                                           *
   * ****************************************************************************
   */

  @Test
  public void aSuccessfulRelationSearchPostShouldResponseWithStatusCodeCreatedAndALocationHeader() throws Exception {
    RelationSearcher searcher = injector.getInstance(RelationSearcher.class);
    RelationSearchParameters parameters = new RelationSearchParameters();
    setupPublicUrl(resource().getURI().toString());
    VRE vreMock = mock(VRE.class);
    SearchResult searchResultMock = mock(SearchResult.class);

    makeVREAvailable(vreMock, VRE_ID);
    when(searcher.search(any(VRE.class), isNotNull(new GenericType<Class<? extends DomainEntity>>() {
    }.getRawClass()), any(RelationSearchParameters.class))).thenReturn(searchResultMock);
    when(repository.addSystemEntity(SearchResult.class, searchResultMock)).thenReturn(ID);

    ClientResponse response = searchResourceBuilder(RELATION_TYPE_STRING) //
      .header(VRE_ID_KEY, VRE_ID) //
      .post(ClientResponse.class, parameters);
    verifyResponseStatus(response, Status.CREATED);

    assertThat(response.getLocation().toString(), equalTo(getRelationSearchURL(ID)));
    verify(searchRequestValidator).validateRelationRequest(anyString(), anyString(), any(RelationSearchParameters.class));
    verify(searcher).search(isNotNull(VRE.class), isNotNull(RELATION_TYPE), isNotNull(RelationSearchParameters.class));
    verify(repository).addSystemEntity(SearchResult.class, searchResultMock);
  }

  @Test
  public void anInvalidSearchRequestPostShouldRespondWithABadRequestStatus() throws Exception {
    RelationSearcher searcher = injector.getInstance(RelationSearcher.class);
    RelationSearchParameters parameters = new RelationSearchParameters();
    doThrow(new TimbuctooException(Response.Status.BAD_REQUEST, "Error")).when(searchRequestValidator).validateRelationRequest(anyString(), anyString(), any(RelationSearchParameters.class));

    ClientResponse response = searchResourceBuilder(RELATION_TYPE_STRING) //
      .type(MediaType.APPLICATION_JSON) //
      .header(VRE_ID_KEY, VRE_ID) //
      .post(ClientResponse.class, parameters);
    verifyResponseStatus(response, Status.BAD_REQUEST);

    verifyZeroInteractions(repository, searcher);
  }

  @Test
  public void whenTheRepositoryCannotStoreTheRelationSearchResultAnInternalServerErrorShouldBeReturned() throws Exception {
    RelationSearcher searcher = injector.getInstance(RelationSearcher.class);
    RelationSearchParameters parameters = new RelationSearchParameters();

    VRE vreMock = mock(VRE.class);
    SearchResult searchResultMock = mock(SearchResult.class);

    makeVREAvailable(vreMock, VRE_ID);
    when(searcher.search(any(VRE.class), isNotNull(RELATION_TYPE), any(RelationSearchParameters.class))).thenReturn(searchResultMock);
    doThrow(Exception.class).when(repository).addSystemEntity(SearchResult.class, searchResultMock);

    ClientResponse response = searchResourceBuilder(RELATION_TYPE_STRING) //
      .type(MediaType.APPLICATION_JSON) //
      .header(VRE_ID_KEY, VRE_ID) //
      .post(ClientResponse.class, parameters);
    verifyResponseStatus(response, Status.INTERNAL_SERVER_ERROR);

    verify(searchRequestValidator).validateRelationRequest(anyString(), anyString(), any(RelationSearchParameters.class));
    verify(searcher).search(any(VRE.class), isNotNull(RELATION_TYPE), any(RelationSearchParameters.class));
    verify(repository).addSystemEntity(SearchResult.class, searchResultMock);
  }

  @Test
  public void whenASearchExceptionIsThrownAnInternalServerErrorShouldBeReturned() throws Exception {
    RelationSearcher searcher = injector.getInstance(RelationSearcher.class);
    RelationSearchParameters parameters = new RelationSearchParameters();

    VRE vreMock = mock(VRE.class);
    makeVREAvailable(vreMock, VRE_ID);
    doThrow(SearchException.class).when(searcher).search(any(VRE.class), isNotNull(RELATION_TYPE), any(RelationSearchParameters.class));

    ClientResponse response = searchResourceBuilder(RELATION_TYPE_STRING) //
      .type(MediaType.APPLICATION_JSON) //
      .header(VRE_ID_KEY, VRE_ID) //
      .post(ClientResponse.class, parameters);
    verifyResponseStatus(response, Status.INTERNAL_SERVER_ERROR);

    verify(searchRequestValidator).validateRelationRequest(anyString(), anyString(), any(RelationSearchParameters.class));
    verify(searcher).search(any(VRE.class), isNotNull(RELATION_TYPE), any(RelationSearchParameters.class));
  }

  private String getRelationSearchURL(String id) {
    return String.format(//
      "%s%ssearch/%s", //
      resource().getURI().toString(), //
      getAPIVersion(), //
      id);
  }

}
