package nl.knaw.huygens.timbuctoo.search;

/*
 * #%L
 * Timbuctoo VRE
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

import com.google.common.collect.Lists;
import nl.knaw.huygens.facetedsearch.model.DefaultFacet;
import nl.knaw.huygens.facetedsearch.model.Facet;
import nl.knaw.huygens.solr.SearchParametersV1;
import nl.knaw.huygens.timbuctoo.Repository;
import nl.knaw.huygens.timbuctoo.config.TypeNames;
import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.ModelException;
import nl.knaw.huygens.timbuctoo.model.Relation;
import nl.knaw.huygens.timbuctoo.model.SearchResult;
import nl.knaw.huygens.timbuctoo.search.converters.RelationSearchParametersConverter;
import nl.knaw.huygens.timbuctoo.vre.RelationSearchParameters;
import nl.knaw.huygens.timbuctoo.vre.SearchException;
import nl.knaw.huygens.timbuctoo.vre.SearchValidationException;
import nl.knaw.huygens.timbuctoo.vre.VRE;
import nl.knaw.huygens.timbuctoo.vre.VREException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import test.timbuctoo.index.model.projecta.ProjectAType1;
import test.timbuctoo.index.model.projecta.ProjectAType2;

import java.util.List;
import java.util.Map;

import static nl.knaw.huygens.timbuctoo.search.RelationSearchParametersMatcher.likeSearchParametersV1;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

public class RelationSearcherTest {

  public static final DefaultFacet TARGET_FACET = new DefaultFacet("test", "test");
  public static final String TARGET_TERM = "targetTerm";
  public static final Class<ProjectAType1> SOURCE_TYPE = ProjectAType1.class;
  public static final Class<ProjectAType2> TARGET_TYPE = ProjectAType2.class;
  private RelationSearcher instance;
  private String typeString = "relation";
  private SearchResult searchResult = new SearchResult();
  private SearchParametersV1 searchParametersV1;
  private Class<? extends DomainEntity> type = Relation.class;
  private RelationSearchParametersConverter relationSearcherParametersConverterMock;
  private VRE vreMock;
  private TypeRegistry typeRegistry;
  private Repository repositoryMock;
  private CollectionConverter collectionConverterMock;
  private List<String> defaultRelationTypeIdsFromRepository = Lists.newArrayList("id1", "id2");
  private List<String> relationTypeIdsArgument = Lists.newArrayList("argId1", "argId2");
  private List<String> sourceIds = Lists.newArrayList("sourceId1", "sourceId2");
  private List<String> targetIds = Lists.newArrayList("targetId1", "targetId2");
  private String sourceSearchId = "sourceSearchId";
  private String targetSearchId = "targetSearchId";
  private RelationFacetedSearchResultFilter facetedSearchResultFilterMock;

  @Mock
  private List<Map<String, Object>> rawSearchResults;
  @Mock
  private FilterableSet<Map<String, Object>> filterableResultsMock;
  public static final List<String> RELATION_TYPE_NAMES = Lists.newArrayList("testName", "testName2");

  @Before
  public void setup() throws Exception {
    MockitoAnnotations.initMocks(this);

    searchParametersV1 = new SearchParametersV1();
    setupParametersConversion(searchParametersV1);

    collectionConverterMock = mock(CollectionConverter.class);
    facetedSearchResultFilterMock = mock(RelationFacetedSearchResultFilter.class);
    setupTypeRegistry();
    setupVRE(searchParametersV1);
    setupRepository();



    instance = new RelationSearcher(repositoryMock, relationSearcherParametersConverterMock, typeRegistry, collectionConverterMock) {
      @Override
      protected RelationFacetedSearchResultFilter createRelationFacetedSearchResultFilter(List<String> sourceIds, List<String> targetIds) {
        return facetedSearchResultFilterMock;
      }
    };
  }

  private void setupRepository() {
    repositoryMock = mock(Repository.class);
    when(repositoryMock.getRelationTypeIdsByName(RELATION_TYPE_NAMES)).thenReturn(defaultRelationTypeIdsFromRepository);
    when(repositoryMock.getEntityOrDefaultVariation(SearchResult.class, sourceSearchId)).thenReturn(createSearchResult(SOURCE_TYPE, sourceIds));
    when(repositoryMock.getEntityOrDefaultVariation(SearchResult.class, targetSearchId)).thenReturn(createSearchResult(TARGET_TYPE, targetIds, TARGET_TERM, TARGET_FACET));
  }

  private void setupVRE(SearchParametersV1 searchParametersV1) throws SearchException, SearchValidationException, VREException {
    vreMock = mock(VRE.class);
    when(vreMock.search(type, searchParametersV1, facetedSearchResultFilterMock)).thenReturn(searchResult);
    when(vreMock.getRelationTypeNamesBetween(SOURCE_TYPE, TARGET_TYPE)).thenReturn(RELATION_TYPE_NAMES);
  }

  private void setupTypeRegistry() throws ModelException {
    typeRegistry = TypeRegistry.getInstance();
    typeRegistry.init(type.getPackage().getName() + " " + TARGET_TYPE.getPackage().getName());
  }

  private void setupParametersConversion(SearchParametersV1 searchParametersV1) {
    relationSearcherParametersConverterMock = mock(RelationSearchParametersConverter.class);
    when(relationSearcherParametersConverterMock.toSearchParametersV1(any(RelationSearchParameters.class))).thenReturn(searchParametersV1);
  }

  private SearchResult createSearchResult(Class<? extends DomainEntity> type, List<String> ids, String term, Facet facet) {
    SearchResult searchResult = createSearchResult(type, ids);
    searchResult.setTerm(term);
    searchResult.setFacets(Lists.newArrayList(facet));

    return searchResult;
  }

  private SearchResult createSearchResult(Class<? extends DomainEntity> type, List<String> ids) {
    SearchResult searchResult = new SearchResult();
    searchResult.setSearchType(TypeNames.getInternalName(type));
    searchResult.setIds(ids);
    return searchResult;
  }

  @Test
  public void searchShouldSearchUsingTheVRE() throws Exception {
    // setup
    RelationSearchParameters relationSearchParameters = createRelationSearchParameters(typeString, sourceSearchId, targetSearchId, relationTypeIdsArgument);

    // action
    SearchResult actualResult = instance.search(vreMock, type, relationSearchParameters);

    // verify
    //verify that the VRE is called correctly
    verify(vreMock).search(type, searchParametersV1, facetedSearchResultFilterMock);
    //SearchResult is what the VRE returns
    assertThat(actualResult, is(sameInstance(searchResult)));
  }

  @Test
  public void searchUsesTheProvidedRelationTypeIds() throws Exception {
    // setup
    RelationSearchParameters relationSearchParameters = createRelationSearchParameters(typeString, sourceSearchId, targetSearchId, relationTypeIdsArgument);

    // action
    instance.search(vreMock, type, relationSearchParameters);

    // verify
    //we can assert that the parameters are taken into account by checking if they were passed to the converter method
    verify(relationSearcherParametersConverterMock).toSearchParametersV1(argThat(likeSearchParametersV1().withTypeIds(relationTypeIdsArgument)));
  }

  @Test
  public void searchUsesTheDefaultRelationsWhenNoRelationsAreProvided() throws Exception {
    // setup
    RelationSearchParameters relationSearchParameters = createSearchParameters(typeString, sourceSearchId, targetSearchId);

    // action
    instance.search(vreMock, Relation.class, relationSearchParameters);

    // verify
    verify(vreMock).getRelationTypeNamesBetween(SOURCE_TYPE, TARGET_TYPE);
    verify(repositoryMock).getRelationTypeIdsByName(RELATION_TYPE_NAMES);
    verify(relationSearcherParametersConverterMock).toSearchParametersV1(argThat(likeSearchParametersV1().withTypeIds(defaultRelationTypeIdsFromRepository)));
  }

  @Test
  public void searchAddsTheFacetsAndTermOfTheTargetSearch() throws SearchException, SearchValidationException {
    // setup
    RelationSearchParameters relationSearchParameters = createRelationSearchParameters(typeString, sourceSearchId, targetSearchId, relationTypeIdsArgument);

    // action
    SearchResult actualResult = instance.search(vreMock, type, relationSearchParameters);

    // verify
    assertThat(actualResult.getFacets(), contains(TARGET_FACET));
    assertThat(actualResult.getTerm(), is(TARGET_TERM));
  }

  protected RelationSearchParameters createRelationSearchParameters(String typeString, String sourceSearchId, String targetSearchId, List<String> relationTypeIds) {
    RelationSearchParameters parameters = createSearchParameters(typeString, sourceSearchId, targetSearchId);
    parameters.setRelationTypeIds(relationTypeIds);
    return parameters;
  }

  protected RelationSearchParameters createSearchParameters(String typeString, String sourceSearchId, String targetSearchId) {
    RelationSearchParameters parameters = new RelationSearchParameters();
    parameters.setTypeString(typeString);
    parameters.setSourceSearchId(sourceSearchId);
    parameters.setTargetSearchId(targetSearchId);
    return parameters;
  }

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Test
  public void searchThrowsASearchExceptionWhenTheVRECannotRetrieveTheRelationTypes() throws Exception {
    // setup
    RelationSearchParameters relationSearchParameters = createSearchParameters(typeString, sourceSearchId, targetSearchId);
    when(vreMock.getRelationTypeNamesBetween(SOURCE_TYPE, TARGET_TYPE)).thenThrow(new VREException(new Exception()));

    //verification setup
    expectedException.expect(SearchException.class);
    expectedException.expectCause(is(instanceOf(VREException.class)));

    // action
    instance.search(vreMock, Relation.class, relationSearchParameters);
  }

  @Test(expected = SearchValidationException.class)
  public void searchPassesTheVRESearchValidationExceptionOnwards() throws Exception {
    searchPassesTheVREExceptionOnwards(SearchValidationException.class);
  }

  @Test(expected = SearchException.class)
  public void searchPassesTheVRESearchExceptionOnwards() throws Exception {
    searchPassesTheVREExceptionOnwards(SearchException.class);
  }

  private void searchPassesTheVREExceptionOnwards(Class<? extends Exception> exceptionToBeThrown) throws SearchException, SearchValidationException {
    //setup
    RelationSearchParameters parameters = createRelationSearchParameters(typeString, sourceSearchId, targetSearchId, relationTypeIdsArgument);
    doThrow(exceptionToBeThrown).when(vreMock).search(type, searchParametersV1, facetedSearchResultFilterMock);

    try {
      // action
      instance.search(vreMock, Relation.class, parameters);
    } finally {
      // verify
      verify(relationSearcherParametersConverterMock).toSearchParametersV1(parameters);
      verify(vreMock).search(type, searchParametersV1, facetedSearchResultFilterMock);
      verifyZeroInteractions(facetedSearchResultFilterMock);
    }
  }
}
