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
import nl.knaw.huygens.timbuctoo.Repository;
import nl.knaw.huygens.timbuctoo.config.Paths;
import nl.knaw.huygens.timbuctoo.config.TypeNames;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.Relation;
import nl.knaw.huygens.timbuctoo.model.RelationSearchResultDTOV2_1;
import nl.knaw.huygens.timbuctoo.model.SearchResult;
import nl.knaw.huygens.timbuctoo.rest.util.search.IndexRegularSearchResultMapper;
import nl.knaw.huygens.timbuctoo.rest.util.search.IndexRelationSearchResultMapper;
import nl.knaw.huygens.timbuctoo.rest.util.search.RegularSearchResultMapper;
import nl.knaw.huygens.timbuctoo.rest.util.search.RelationSearchResultMapper;
import nl.knaw.huygens.timbuctoo.search.converters.RelationSearchParametersConverter;
import nl.knaw.huygens.timbuctoo.search.converters.SearchConversionException;
import nl.knaw.huygens.timbuctoo.vre.RelationSearchParameters;
import nl.knaw.huygens.timbuctoo.vre.RelationSearchParametersV2_1;
import nl.knaw.huygens.timbuctoo.vre.SearchException;
import nl.knaw.huygens.timbuctoo.vre.SearchValidationException;
import nl.knaw.huygens.timbuctoo.vre.VRE;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import test.rest.model.TestDomainEntity;
import test.rest.model.TestRelation;

import javax.ws.rs.core.MediaType;

import static nl.knaw.huygens.timbuctoo.rest.util.CustomHeaders.VRE_ID_KEY;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SearchResourceV2_1Test extends SearchResourceV1Test {


  public static final RelationSearchParametersV2_1 PARAMETERS_V_2_1 = new RelationSearchParametersV2_1();
  public static final Class<TestDomainEntity> RELATED_TYPE = TestDomainEntity.class;
  private static final String RELATED_TYPE_STRING = TypeNames.getExternalName(RELATED_TYPE);
  public static final RelationSearchParameters PARAMETERS = new RelationSearchParameters();
  public static final Class<TestRelation> RELATION_TYPE = TestRelation.class;
  private static final String RELATION_X_TYPE_STRING = TypeNames.getExternalName(RELATION_TYPE);
  public static final String RELATION_I_TYPE_STRING = TypeNames.getInternalName(RELATION_TYPE);
  private VRE vreMock;
  public static final int DEFAULT_START = 0;
  public static final int DEFAULT_ROWS = 10;
  private RelationSearchParametersConverter relationSearchParametersConverter;

  @Before
  public void setup() throws SearchConversionException {
    setupPublicUrl(resource().getURI().toString());
    setupVRE();
    setupConverter();
  }

  protected void setupVRE() {
    vreMock = mock(VRE.class);
    makeVREAvailable(vreMock, VRE_ID);
  }

  protected void setupConverter() throws SearchConversionException {
    relationSearchParametersConverter = injector.getInstance(RelationSearchParametersConverter.class);
    when(relationSearchParametersConverter.fromRelationParametersV2_1(Matchers.<Class<? extends Relation>>any(), any(RelationSearchParametersV2_1.class), any(VRE.class), Matchers.<Class<? extends DomainEntity>>any())).thenReturn(PARAMETERS);
  }


  @Test
  @Override
  public void testGetRelationSearch() {
    // setup
    Repository repository = injector.getInstance(Repository.class);
    SearchResult value = new SearchResult();
    when(repository.getEntityOrDefaultVariation(SearchResult.class, ID)).thenReturn(value);

    SearchResult searchResult = new SearchResult();
    searchResult.setSearchType(RELATION_I_TYPE_STRING);

    RelationSearchResultDTOV2_1 clientSearchResult = new RelationSearchResultDTOV2_1();

    when(repository.getEntityOrDefaultVariation(SearchResult.class, ID)).thenReturn(searchResult);
    when(relationSearchResultMapperMock.create(TEST_RELATION_TYPE, searchResult, DEFAULT_START, DEFAULT_ROWS, getAPIVersion())).thenReturn(clientSearchResult);

    // action
    ClientResponse response = searchResourceBuilder(ID) //
      .accept(MediaType.APPLICATION_JSON_TYPE) //
      .get(ClientResponse.class);

    // verify
    verifyResponseStatus(response, ClientResponse.Status.OK);

    RelationSearchResultDTOV2_1 actualResult = response.getEntity(RelationSearchResultDTOV2_1.class);
    assertThat(actualResult, notNullValue(RelationSearchResultDTOV2_1.class));

    verify(repository).getEntityOrDefaultVariation(SearchResult.class, ID);
    verify(relationSearchResultMapperMock).create(TEST_RELATION_TYPE, searchResult, DEFAULT_START, DEFAULT_ROWS, getAPIVersion());

  }


  /*
   * ****************************************************************************
   * Reception Search                                                           *
   * ****************************************************************************
   */

  @Test
  @Override
  public void aSuccessfulRelationSearchPostShouldResponseWithStatusCodeCreatedAndALocationHeader() throws Exception {
    // setup
    when(vreMock.searchRelations(RELATION_TYPE, PARAMETERS)).thenReturn(ID);

    // action
    ClientResponse response = executeRelationSearchPostRequest();

    // verify
    verifyResponseStatus(response, ClientResponse.Status.CREATED);
    assertThat(response.getLocation().toString(), equalTo(getRelationSearchURL(ID)));
    verify(relationSearchParametersConverter).fromRelationParametersV2_1(
      argThat(equalTo(RELATION_TYPE)), //
        any(RelationSearchParametersV2_1.class), //
        any(VRE.class),
      argThat(equalTo(RELATED_TYPE)));
    verify(vreMock).searchRelations(RELATION_TYPE, PARAMETERS);
  }


  @Test
  @Override
  public void anInvalidSearchRequestPostShouldRespondWithABadRequestStatus() throws Exception {
    // setup
    searchParametersAreInvalid();

    // action
    ClientResponse response = executeRelationSearchPostRequest();

    // verify
    verifyResponseStatus(response, ClientResponse.Status.BAD_REQUEST);
  }

  protected void searchParametersAreInvalid() throws Exception {
    when(vreMock.searchRelations(RELATION_TYPE, PARAMETERS)).thenThrow(new SearchValidationException(new Exception()));
  }

  @Test
  @Override
  public void whenTheRepositoryCannotStoreTheRelationSearchResultAnInternalServerErrorShouldBeReturned() throws Exception {
    // this cannot happen anymore.
  }

  private ClientResponse executeRelationSearchPostRequest() {
    return searchResourceBuilder(RELATION_X_TYPE_STRING, RELATED_TYPE_STRING) //
      .header(VRE_ID_KEY, VRE_ID) //
      .post(ClientResponse.class, PARAMETERS_V_2_1);
  }

  @Test
  @Override
  public void whenASearchExceptionIsThrownAnInternalServerErrorShouldBeReturned() throws Exception {
    // setup
    searchWentWrong();

    // action
    ClientResponse response = executeRelationSearchPostRequest();

    // verify
    verifyResponseStatus(response, ClientResponse.Status.INTERNAL_SERVER_ERROR);
  }

  private void searchWentWrong() throws Exception {
    when(vreMock.searchRelations(RELATION_TYPE, PARAMETERS)).thenThrow(new SearchException(new Exception()));
  }

  @Test
  public void whenTheRelationSearchParametersV2_1ToRelationSearchParametersAInternalServerErrorIsThrown() throws Exception {
    // setup
    RelationSearchParametersConverter converter = injector.getInstance(RelationSearchParametersConverter.class);

    when(converter.fromRelationParametersV2_1(Matchers.<Class<? extends Relation>>any(), any(RelationSearchParametersV2_1.class), any(VRE.class), Matchers.<Class<? extends DomainEntity>>any())) //
      .thenThrow(new SearchConversionException(new Exception()));

    // action
    ClientResponse response = executeRelationSearchPostRequest();

    // verify
    verifyResponseStatus(response, ClientResponse.Status.INTERNAL_SERVER_ERROR);
  }

  @Test
  public void theRelationSearchReturnsNotFoundWhenTheOldURLIsUsed() {
    // action
    ClientResponse response = searchResourceBuilder(RELATION_X_TYPE_STRING) //
      .header(VRE_ID_KEY, VRE_ID) //
      .post(ClientResponse.class, PARAMETERS_V_2_1);


    // verify
    verifyResponseStatus(response, ClientResponse.Status.NOT_FOUND);
  }


  private String getRelationSearchURL(String id) {
    return String.format(//
      "%s%ssearch/%s", //
      resource().getURI().toString(), //
      getAPIVersion(), //
      id);
  }

  @Override
  protected String getAPIVersion() {
    return Paths.V2_1_PATH;
  }

  @Override
  protected RegularSearchResultMapper getRegularSearchResultMapper() {
    return injector.getInstance(IndexRegularSearchResultMapper.class);
  }

  @Override
  protected RelationSearchResultMapper getRelationSearchResultMapper() {
    return injector.getInstance(IndexRelationSearchResultMapper.class);
  }


}
