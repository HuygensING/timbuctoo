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
import nl.knaw.huygens.timbuctoo.config.Paths;
import nl.knaw.huygens.timbuctoo.config.TypeNames;
import nl.knaw.huygens.timbuctoo.rest.util.search.IndexRegularSearchResultMapper;
import nl.knaw.huygens.timbuctoo.rest.util.search.IndexRelationSearchResultMapper;
import nl.knaw.huygens.timbuctoo.rest.util.search.RegularSearchResultMapper;
import nl.knaw.huygens.timbuctoo.rest.util.search.RelationSearchResultMapper;
import nl.knaw.huygens.timbuctoo.search.converters.RelationSearchParametersConverter;
import nl.knaw.huygens.timbuctoo.storage.StorageException;
import nl.knaw.huygens.timbuctoo.storage.ValidationException;
import nl.knaw.huygens.timbuctoo.vre.RelationSearchParameters;
import nl.knaw.huygens.timbuctoo.vre.RelationSearchParametersV2_1;
import nl.knaw.huygens.timbuctoo.vre.SearchException;
import nl.knaw.huygens.timbuctoo.vre.SearchValidationException;
import nl.knaw.huygens.timbuctoo.vre.VRE;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import test.rest.model.TestDomainEntity;
import test.rest.model.TestRelation;

import static nl.knaw.huygens.timbuctoo.rest.util.CustomHeaders.VRE_ID_KEY;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SearchResourceV2_1Test extends SearchResourceV1Test {

  private static final String RELATION_TYPE_STRING = TypeNames.getExternalName(TestRelation.class);
  private static final String RELATED_TYPE_STRING = TypeNames.getExternalName(TestDomainEntity.class);
  public static final RelationSearchParametersV2_1 PARAMETERS_V_2_1 = new RelationSearchParametersV2_1();
  public static final Class<TestRelation> RELATION_TYPE = TestRelation.class;
  public static final RelationSearchParameters PARAMETERS = new RelationSearchParameters();
  private VRE vreMock;

  @Before
  public void setup() {
    setupPublicUrl(resource().getURI().toString());
    setupVRE();
    setupConverter();
  }

  protected void setupVRE() {
    vreMock = mock(VRE.class);
    makeVREAvailable(vreMock, VRE_ID);
  }

  protected void setupConverter() {
    RelationSearchParametersConverter relationSearchParametersConverter = injector.getInstance(RelationSearchParametersConverter.class);
    when(relationSearchParametersConverter.fromRelationParametersV2_1(any(RelationSearchParametersV2_1.class))).thenReturn(PARAMETERS);
  }


  @Ignore
  @Test
  @Override
  public void testGetRelationSearch() {

    fail("Yet to be implemented");
  }


  /*
   * ****************************************************************************
   * Reception Search                                                           *
   * ****************************************************************************
   */

  @Test
  @Override
  public void aSuccessfulRelationSearchPostShouldResponseWithStatusCodeCreatedAndALocationHeader() throws SearchException, SearchValidationException, StorageException, ValidationException {
    // setup
    when(vreMock.searchRelations(RELATION_TYPE, PARAMETERS)).thenReturn(ID);

    // action
    ClientResponse response = executeRelationSearchPostRequest();

    // verify
    verifyResponseStatus(response, ClientResponse.Status.CREATED);
    assertThat(response.getLocation().toString(), equalTo(getRelationSearchURL(ID)));
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

  protected void searchParametersAreInvalid() throws Exception{
    when(vreMock.searchRelations(RELATION_TYPE, PARAMETERS)).thenThrow(new SearchValidationException(new Exception()));
  }

  @Test
  @Override
  public void whenTheRepositoryCannotStoreTheRelationSearchResultAnInternalServerErrorShouldBeReturned() throws Exception {
    // setup
    searchResultCannotBeStored();

    // action
    ClientResponse response = executeRelationSearchPostRequest();

    // verify
    verifyResponseStatus(response, ClientResponse.Status.INTERNAL_SERVER_ERROR);
  }

  private ClientResponse executeRelationSearchPostRequest() {
    return searchResourceBuilder(RELATION_TYPE_STRING, RELATED_TYPE_STRING) //
        .header(VRE_ID_KEY, VRE_ID) //
        .post(ClientResponse.class, PARAMETERS_V_2_1);
  }

  private void searchResultCannotBeStored() throws Exception {
    when(vreMock.searchRelations(RELATION_TYPE, PARAMETERS)).thenThrow(new StorageException("exception"));
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
  public void theRelationSearchReturnANotFoundWhenTheOldURLIsUsed() {
    // action
    ClientResponse response = searchResourceBuilder(RELATION_TYPE_STRING) //
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
