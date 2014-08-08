package nl.knaw.huygens.timbuctoo.search;

/*
 * #%L
 * Timbuctoo search
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
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import nl.knaw.huygens.facetedsearch.model.FacetedSearchResult;
import nl.knaw.huygens.solr.RelationSearchParameters;
import nl.knaw.huygens.solr.SearchParametersV1;
import nl.knaw.huygens.timbuctoo.Repository;
import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.Relation;
import nl.knaw.huygens.timbuctoo.model.SearchResult;
import nl.knaw.huygens.timbuctoo.search.converters.RelationFacetedSearchResultConverter;
import nl.knaw.huygens.timbuctoo.search.converters.RelationSearchParametersConverter;
import nl.knaw.huygens.timbuctoo.vre.SearchException;
import nl.knaw.huygens.timbuctoo.vre.SearchValidationException;
import nl.knaw.huygens.timbuctoo.vre.VRE;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.google.common.collect.Lists;

public class SolrRelationSearcherTest {
  private SolrRelationSearcher instance;
  private String typeString = "relation";
  private FacetedSearchResult facetedSearchResult = new FacetedSearchResult();
  private FacetedSearchResult filteredFacetedSearchResult = new FacetedSearchResult();
  private SearchResult searchResult = new SearchResult();
  private SearchParametersV1 searchParametersV1 = new SearchParametersV1();
  private Class<? extends DomainEntity> type = Relation.class;
  private RelationSearchParametersConverter relationSearcherParametersConverterMock;
  private RelationFacetedSearchResultConverter facetedSearchResultConverterMock;
  private VRE vreMock;
  private TypeRegistry typeRegistryMock;
  private Repository repositoryMock;
  private CollectionConverter collectionConverterMock;
  private ArrayList<String> relationTypeIds = Lists.newArrayList("id1", "id2");
  private List<String> sourceIds = Lists.newArrayList("sourceId1", "sourceId2");
  private List<String> targetIds = Lists.newArrayList("targetId1", "targetId2");
  private String sourceSearchId = "sourceSearchId";
  private String targetSearchId = "targetSearchId";

  private RelationFacetedSearchResultFilter facetedSearchResultFilterMock;

  @Mock
  private List<Map<String, Object>> rawSearchResults;
  @Mock
  private FilterableSet<Map<String, Object>> filterableResultsMock;

  @Before
  public void setup() throws Exception {
    MockitoAnnotations.initMocks(this);

    relationSearcherParametersConverterMock = mock(RelationSearchParametersConverter.class);
    vreMock = mock(VRE.class);
    typeRegistryMock = mock(TypeRegistry.class);
    repositoryMock = mock(Repository.class);
    collectionConverterMock = mock(CollectionConverter.class);
    facetedSearchResultConverterMock = mock(RelationFacetedSearchResultConverter.class);
    facetedSearchResultFilterMock = mock(RelationFacetedSearchResultFilter.class);

    doReturn(type).when(typeRegistryMock).getDomainEntityType(typeString);
    when(vreMock.search(type, searchParametersV1, facetedSearchResultConverterMock, facetedSearchResultFilterMock)).thenReturn(searchResult);
    when(repositoryMock.getEntity(SearchResult.class, sourceSearchId)).thenReturn(createSearchResult(sourceIds));
    when(repositoryMock.getEntity(SearchResult.class, targetSearchId)).thenReturn(createSearchResult(targetIds));

    when(facetedSearchResultFilterMock.process(facetedSearchResult)).thenReturn(filteredFacetedSearchResult);
    when(facetedSearchResultConverterMock.convert(typeString, filteredFacetedSearchResult)).thenReturn(searchResult);

    instance = new SolrRelationSearcher(repositoryMock, relationSearcherParametersConverterMock, typeRegistryMock, collectionConverterMock) {
      @Override
      protected RelationFacetedSearchResultFilter createRelationFacetedSearchResultFilter(List<String> sourceIds, List<String> targetIds) {
        return facetedSearchResultFilterMock;
      }

      @Override
      protected RelationFacetedSearchResultConverter createFacetedSearchResultConverter(List<String> sourceSearchIds, List<String> targetSearchIds, List<String> relationTypeIds) {
        return facetedSearchResultConverterMock;
      }

    };
  }

  private SearchResult createSearchResult(List<String> ids) {
    SearchResult searchResult = new SearchResult();
    searchResult.setIds(ids);

    return searchResult;
  }

  @Test
  public void testSearchWithRelationTypes() throws Exception {
    RelationSearchParameters relationSearchParameters = createRelationSearchParameters(typeString, sourceSearchId, targetSearchId, relationTypeIds);

    when(relationSearcherParametersConverterMock.toSearchParametersV1(relationSearchParameters)).thenReturn(searchParametersV1);

    // action
    SearchResult actualResult = instance.search(vreMock, type, relationSearchParameters);

    // verify
    verify(relationSearcherParametersConverterMock).toSearchParametersV1(relationSearchParameters);
    verify(vreMock).search(type, searchParametersV1, facetedSearchResultConverterMock, facetedSearchResultFilterMock);

    assertThat(actualResult, equalTo(searchResult));

  }

  @Test
  public void testSearchWithOutRelationTypesSpecified() throws Exception {
    RelationSearchParameters relationSearchParameters = createSearchParameters(typeString, sourceSearchId, targetSearchId);

    List<String> relationTypeNames = Lists.newArrayList();

    when(relationSearcherParametersConverterMock.toSearchParametersV1(relationSearchParameters)).thenReturn(searchParametersV1);
    when(vreMock.getReceptionNames()).thenReturn(relationTypeNames);
    when(repositoryMock.getRelationTypeIdsByName(relationTypeNames)).thenReturn(relationTypeIds);

    // action
    SearchResult actualResult = instance.search(vreMock, Relation.class, relationSearchParameters);

    // verify
    InOrder inOrder = Mockito.inOrder(repositoryMock, relationSearcherParametersConverterMock);
    inOrder.verify(repositoryMock).getRelationTypeIdsByName(relationTypeNames);
    inOrder.verify(relationSearcherParametersConverterMock).toSearchParametersV1(relationSearchParameters);
    verify(vreMock).search(type, searchParametersV1, facetedSearchResultConverterMock, facetedSearchResultFilterMock);

    assertThat(actualResult, equalTo(searchResult));
  }

  protected RelationSearchParameters createRelationSearchParameters(String typeString, String sourceSearchId, String targetSearchId, ArrayList<String> relationTypeIds) {
    RelationSearchParameters relationSearchParameters = createSearchParameters(typeString, sourceSearchId, targetSearchId);
    relationSearchParameters.setRelationTypeIds(relationTypeIds);
    return relationSearchParameters;
  }

  protected RelationSearchParameters createSearchParameters(String typeString, String sourceSearchId, String targetSearchId) {
    RelationSearchParameters relationSearchParameters = new RelationSearchParameters();
    relationSearchParameters.setTypeString(typeString);
    relationSearchParameters.setSourceSearchId(sourceSearchId);
    relationSearchParameters.setTargetSearchId(targetSearchId);
    return relationSearchParameters;
  }

  @Test(expected = SearchValidationException.class)
  public void testSearchVREThrowsASearchValidationException() throws Exception {
    testSearchVREThrowsAnException(SearchValidationException.class);
  }

  @Test(expected = SearchException.class)
  public void testSearchVREThrowsASearchException() throws Exception {
    testSearchVREThrowsAnException(SearchException.class);
  }

  private void testSearchVREThrowsAnException(Class<? extends Exception> exceptionToBeThrown) throws SearchException, SearchValidationException {
    RelationSearchParameters relationSearchParameters = createRelationSearchParameters(typeString, sourceSearchId, targetSearchId, relationTypeIds);

    when(relationSearcherParametersConverterMock.toSearchParametersV1(relationSearchParameters)).thenReturn(searchParametersV1);
    doThrow(exceptionToBeThrown).when(vreMock).search(type, searchParametersV1, facetedSearchResultConverterMock, facetedSearchResultFilterMock);

    try {
      // action
      instance.search(vreMock, Relation.class, relationSearchParameters);
    } finally {
      // verify
      verify(relationSearcherParametersConverterMock).toSearchParametersV1(relationSearchParameters);
      verify(vreMock).search(type, searchParametersV1, facetedSearchResultConverterMock, facetedSearchResultFilterMock);
      verifyZeroInteractions(facetedSearchResultFilterMock, facetedSearchResultConverterMock);
    }
  }
}
