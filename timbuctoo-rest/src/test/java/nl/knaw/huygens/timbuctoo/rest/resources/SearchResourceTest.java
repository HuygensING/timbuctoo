package nl.knaw.huygens.timbuctoo.rest.resources;

/*
 * #%L
 * Timbuctoo REST api
 * =======
 * Copyright (C) 2012 - 2015 Huygens ING
 * =======
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either NULL_VERSION 3 of the
 * License, or (at your option) any later NULL_VERSION.
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
import nl.knaw.huygens.timbuctoo.search.RelationSearcher;
import nl.knaw.huygens.timbuctoo.vre.RelationSearchParameters;
import nl.knaw.huygens.solr.SearchParameters;
import nl.knaw.huygens.solr.SearchParametersV1;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.RegularSearchResultDTO;
import nl.knaw.huygens.timbuctoo.model.Relation;
import nl.knaw.huygens.timbuctoo.model.RelationSearchResultDTO;
import nl.knaw.huygens.timbuctoo.model.SearchResult;
import nl.knaw.huygens.timbuctoo.rest.TimbuctooException;
import nl.knaw.huygens.timbuctoo.search.converters.SearchParametersConverter;
import nl.knaw.huygens.timbuctoo.storage.StorageException;
import nl.knaw.huygens.timbuctoo.storage.ValidationException;
import nl.knaw.huygens.timbuctoo.vre.SearchException;
import nl.knaw.huygens.timbuctoo.vre.SearchValidationException;
import nl.knaw.huygens.timbuctoo.vre.VRE;
import org.junit.Test;
import org.mockito.Matchers;
import test.rest.model.projecta.OtherDomainEntity;

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

public class SearchResourceTest extends SearchResourceTestBase {

  private static final String RELATIONS_PATH = "relations";
  private static final String TYPE_STRING = "person";
  protected static final String RELATION_TYPE_STRING = "testrelation";
  public static final String NULL_VERSION = null;

  @Override
  protected WebResource searchResource(String... pathElements) {
    return addPathToWebResource(resource().path("search"), pathElements);
  }

  @Test
  public void testPostSuccess() throws Exception {
    SearchParametersV1 searchParametersV1Mock = mock(SearchParametersV1.class);
    VRE vreMock = setUpVREManager(true, true);
    // setup
    SearchParameters searchParameters = new SearchParameters();
    String typeString = "otherdomainentity";
    searchParameters.setTypeString(typeString);

    SearchParametersConverter searchParametersConverter = injector.getInstance(SearchParametersConverter.class);
    when(searchParametersConverter.toV1(any(SearchParameters.class))).thenReturn(searchParametersV1Mock);

    Class<OtherDomainEntity> type = OtherDomainEntity.class;

    SearchResult searchResult = mock(SearchResult.class);
    setSearchResult(vreMock, searchResult);

    when(repository.addSystemEntity(SearchResult.class, searchResult)).thenReturn(ID);

    // action
    String expected = getExpectedURL(ID);
    ClientResponse response = searchResourceBuilder().header(VRE_ID_KEY, VRE_ID).post(ClientResponse.class, searchParameters);
    String actual = response.getHeaders().getFirst(LOCATION_HEADER);

    // verify
    verify(vreMock).search(type, searchParametersV1Mock);
    verifyResponseStatus(response, Status.CREATED);
    assertEquals(expected, actual);
  }

  protected String getExpectedURL(String id) {
    return String.format("%ssearch/%s", resource().getURI().toString(), id);
  }

  @Test
  public void testPostRequestInvalid() {
    // setup
    SearchParameters searchParameters = new SearchParameters();
    doThrow(new TimbuctooException(Response.Status.BAD_REQUEST, "Error")).when(searchRequestValidator).validate(anyString(), anyString(), any(SearchParametersV1.class));

    // action
    ClientResponse response = searchResourceBuilder().header(VRE_ID_KEY, VRE_ID).post(ClientResponse.class, searchParameters);

    // verify
    verifyResponseStatus(response, Status.BAD_REQUEST);
    verifyZeroInteractions(repository);
  }

  @Test
  public void testPostVREThrowsAnException() throws Exception {
    VRE vre = setUpVREManager(true, true);

    SearchParameters params = new SearchParameters().setTypeString(TYPE_STRING).setTerm(TERM);
    doThrow(Exception.class).when(vre).search(Matchers.<Class<? extends DomainEntity>>any(), any(SearchParametersV1.class));

    ClientResponse response = searchResourceBuilder().header(VRE_ID_KEY, VRE_ID).post(ClientResponse.class, params);

    verifyResponseStatus(response, Status.INTERNAL_SERVER_ERROR);
    verify(repository, never()).addSystemEntity(Matchers.<Class<SearchResult>>any(), any(SearchResult.class));
    verify(vre).search(Matchers.<Class<? extends DomainEntity>>any(), any(SearchParametersV1.class));
  }

  @Test
  public void testPostStorageManagerThrowsAnException() throws Exception {
    VRE vreMock = setUpVREManager(true, true);

    SearchParameters params = new SearchParameters().setTypeString(TYPE_STRING).setTerm(TERM);
    SearchResult searchResult = mock(SearchResult.class);
    setSearchResult(vreMock, searchResult);
    doThrow(IOException.class).when(repository).addSystemEntity(Matchers.<Class<SearchResult>>any(), any(SearchResult.class));

    ClientResponse response = searchResourceBuilder().header(VRE_ID_KEY, VRE_ID).post(ClientResponse.class, params);

    verifyResponseStatus(response, Status.INTERNAL_SERVER_ERROR);
    verify(vreMock).search(Matchers.<Class<? extends DomainEntity>>any(), any(SearchParametersV1.class));
  }

  @Test
  public void testGetSuccess() {
    // setup
    SearchResult searchResult = new SearchResult();
    searchResult.setSearchType(SEARCH_RESULT_TYPE_STRING);

    RegularSearchResultDTO clientSearchResult = new RegularSearchResultDTO();

    int defaultStart = 0;
    int defaultRows = 10;

    when(repository.getEntityOrDefaultVariation(SearchResult.class, ID)).thenReturn(searchResult);
    when(regularSearchResultMapperMock.create(SEARCH_RESULT_TYPE, searchResult, defaultStart, defaultRows, NULL_VERSION)).thenReturn(clientSearchResult);

    // action
    ClientResponse response = searchResourceBuilder(ID) //
      .accept(MediaType.APPLICATION_JSON_TYPE) //
      .get(ClientResponse.class);

    // verify
    verifyResponseStatus(response, Status.OK);

    RegularSearchResultDTO actualResult = response.getEntity(RegularSearchResultDTO.class);
    assertThat(actualResult, notNullValue(RegularSearchResultDTO.class));

    verify(repository).getEntityOrDefaultVariation(SearchResult.class, ID);
    verify(regularSearchResultMapperMock).create(SEARCH_RESULT_TYPE, searchResult, defaultStart, defaultRows, NULL_VERSION);
  }

  @Test
  public void testGetSuccessWithStartAndRows() {
    // setup
    int startIndex = 20;
    int numberOfRows = 20;

    SearchResult searchResult = new SearchResult();
    searchResult.setSearchType(SEARCH_RESULT_TYPE_STRING);

    RegularSearchResultDTO clientSearchResult = new RegularSearchResultDTO();

    MultivaluedMap<String, String> queryParameters = new MultivaluedMapImpl();
    queryParameters.add("start", "20");
    queryParameters.add("rows", "20");

    when(repository.getEntityOrDefaultVariation(SearchResult.class, ID)).thenReturn(searchResult);
    when(regularSearchResultMapperMock.create(SEARCH_RESULT_TYPE, searchResult, startIndex, numberOfRows, NULL_VERSION)).thenReturn(clientSearchResult);

    // action
    ClientResponse response = searchResource(ID) //
      .queryParams(queryParameters) //
      .type(MediaType.APPLICATION_JSON_TYPE).accept(MediaType.APPLICATION_JSON_TYPE).get(ClientResponse.class);

    // verify
    verifyResponseStatus(response, Status.OK);

    RegularSearchResultDTO actualResult = response.getEntity(RegularSearchResultDTO.class);
    assertThat(actualResult, notNullValue(RegularSearchResultDTO.class));

    verify(repository).getEntityOrDefaultVariation(SearchResult.class, ID);
    verify(regularSearchResultMapperMock).create(SEARCH_RESULT_TYPE, searchResult, startIndex, numberOfRows, NULL_VERSION);
  }

  @Test
  public void testGetRelationSearch() {
    // setup
    SearchResult searchResult = new SearchResult();
    searchResult.setSearchType(RELATION_SEARCH_RESULT_TYPE);

    RelationSearchResultDTO clientSearchResult = new RelationSearchResultDTO();

    int defaultStart = 0;
    int defaultRows = 10;

    when(repository.getEntityOrDefaultVariation(SearchResult.class, ID)).thenReturn(searchResult);
    when(relationSearchResultMapperMock.create(TEST_RELATION_TYPE, searchResult, defaultStart, defaultRows, NULL_VERSION)).thenReturn(clientSearchResult);

    // action
    ClientResponse response = searchResourceBuilder(RELATIONS_PATH, ID) //
      .accept(MediaType.APPLICATION_JSON_TYPE) //
      .get(ClientResponse.class);

    // verify
    verifyResponseStatus(response, Status.OK);

    RelationSearchResultDTO actualResult = response.getEntity(RelationSearchResultDTO.class);
    assertThat(actualResult, notNullValue(RelationSearchResultDTO.class));

    verify(repository).getEntityOrDefaultVariation(SearchResult.class, ID);
    verify(relationSearchResultMapperMock).create(TEST_RELATION_TYPE, searchResult, defaultStart, defaultRows, NULL_VERSION);
  }

  @Test
  public void testGetNoId() {
    ClientResponse response = searchResourceBuilder() //
      .accept(MediaType.APPLICATION_JSON_TYPE) //
      .get(ClientResponse.class);
    verifyResponseStatus(response, Status.METHOD_NOT_ALLOWED);
  }

  @Test
  public void testGetUnknownId() {
    when(repository.getEntityOrDefaultVariation(SearchResult.class, ID)).thenReturn(null);

    ClientResponse response = searchResourceBuilder(ID) //
      .accept(MediaType.APPLICATION_JSON_TYPE) //
      .get(ClientResponse.class);
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
  public void aSuccessfulRelationSearchPostShouldResponseWithStatusCodeCreatedandALocationHeader() throws SearchException, SearchValidationException, StorageException, ValidationException {
    // setup
    String expectedLocationHeader = getRelationSearchURL(ID);

    RelationSearchParameters parameters = new RelationSearchParameters();
    parameters.setTypeString(RELATION_TYPE_STRING);

    VRE vreMock = mock(VRE.class);
    SearchResult searchResultMock = mock(SearchResult.class);

    RelationSearcher relationSearcher = injector.getInstance(RelationSearcher.class);

    makeVREAvailable(vreMock, VRE_ID);
    when(relationSearcher.search(any(VRE.class), isNotNull(new GenericType<Class<? extends Relation>>() {
    }.getRawClass()), any(RelationSearchParameters.class))).thenReturn(searchResultMock);
    when(repository.addSystemEntity(SearchResult.class, searchResultMock)).thenReturn(ID);

    // action
    ClientResponse response = searchResourceBuilder(RELATIONS_PATH) //
      .header(VRE_ID_KEY, VRE_ID) //
      .post(ClientResponse.class, parameters);

    // verify
    verifyResponseStatus(response, Status.CREATED);
    assertThat(response.getLocation().toString(), equalTo(expectedLocationHeader));

    verify(searchRequestValidator).validateRelationRequest(anyString(), anyString(), any(RelationSearchParameters.class));
    verify(relationSearcher).search(any(VRE.class), isNotNull(new GenericType<Class<? extends Relation>>() {
    }.getRawClass()), any(RelationSearchParameters.class));
    verify(repository).addSystemEntity(SearchResult.class, searchResultMock);
  }

  @Test
  public void anInvalidSearchRequestPostShouldRespondWithABadRequestStatus() throws StorageException, ValidationException {
    // setup
    RelationSearchParameters parameters = new RelationSearchParameters();
    RelationSearcher relationSearcher = injector.getInstance(RelationSearcher.class);
    doThrow(new TimbuctooException(Response.Status.BAD_REQUEST, "Error")).when(searchRequestValidator).validateRelationRequest(anyString(), anyString(), any(RelationSearchParameters.class));

    // action
    ClientResponse response = searchResourceBuilder(RELATIONS_PATH) //
      .header(VRE_ID_KEY, VRE_ID) //
      .post(ClientResponse.class, parameters);

    // verify
    verifyResponseStatus(response, Status.BAD_REQUEST);
    verifyZeroInteractions(repository, relationSearcher);
  }

  @Test
  public void whenTheRepositoryCannotStoreTheRelationSearchResultAnInternalServerErrorShouldBeReturned() throws StorageException, ValidationException, Exception {
    // setup
    RelationSearchParameters parameters = new RelationSearchParameters();
    parameters.setTypeString(RELATION_TYPE_STRING);

    VRE vreMock = mock(VRE.class);
    SearchResult searchResultMock = mock(SearchResult.class);

    RelationSearcher relationSearcher = injector.getInstance(RelationSearcher.class);
    parameters.setTypeString(RELATION_TYPE_STRING);

    makeVREAvailable(vreMock, VRE_ID);
    when(relationSearcher.search(any(VRE.class), isNotNull(new GenericType<Class<? extends Relation>>() {
    }.getRawClass()), any(RelationSearchParameters.class))).thenReturn(searchResultMock);
    doThrow(Exception.class).when(repository).addSystemEntity(SearchResult.class, searchResultMock);

    // action
    ClientResponse response = searchResourceBuilder(RELATIONS_PATH) //
      .header(VRE_ID_KEY, VRE_ID) //
      .post(ClientResponse.class, parameters);

    // verify
    verifyResponseStatus(response, Status.INTERNAL_SERVER_ERROR);

    verify(searchRequestValidator).validateRelationRequest(anyString(), anyString(), any(RelationSearchParameters.class));
    verify(relationSearcher).search(any(VRE.class), isNotNull(new GenericType<Class<? extends Relation>>() {
    }.getRawClass()), any(RelationSearchParameters.class));
    verify(repository).addSystemEntity(SearchResult.class, searchResultMock);
  }

  @Test
  public void whenTheRelationSearcherThrowsAnSearchExceptionAnInternalServerErrorShouldBeReturned() throws StorageException, ValidationException, Exception {
    // setup
    RelationSearchParameters parameters = new RelationSearchParameters();
    parameters.setTypeString(RELATION_TYPE_STRING);

    VRE vreMock = mock(VRE.class);
    RelationSearcher relationSearcher = injector.getInstance(RelationSearcher.class);

    makeVREAvailable(vreMock, VRE_ID);
    doThrow(SearchException.class).when(relationSearcher).search(any(VRE.class), isNotNull(new GenericType<Class<? extends Relation>>() {
    }.getRawClass()), any(RelationSearchParameters.class));

    // action
    ClientResponse response = searchResourceBuilder(RELATIONS_PATH) //
      .header(VRE_ID_KEY, VRE_ID) //
      .post(ClientResponse.class, parameters);

    // verify
    verifyResponseStatus(response, Status.INTERNAL_SERVER_ERROR);

    verify(searchRequestValidator).validateRelationRequest(anyString(), anyString(), any(RelationSearchParameters.class));
    verify(relationSearcher).search(any(VRE.class), isNotNull(new GenericType<Class<? extends Relation>>() {
    }.getRawClass()), any(RelationSearchParameters.class));
  }

  private String getRelationSearchURL(String id) {
    return String.format("%ssearch/relations/%s", resource().getURI(), id);
  }

}
