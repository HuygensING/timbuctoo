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
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import nl.knaw.huygens.facetedsearch.model.FacetedSearchResult;
import nl.knaw.huygens.solr.RelationSearchParameters;
import nl.knaw.huygens.solr.SearchParametersV1;
import nl.knaw.huygens.timbuctoo.Repository;
import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.index.Index;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.Relation;
import nl.knaw.huygens.timbuctoo.model.SearchResult;
import nl.knaw.huygens.timbuctoo.search.converters.RelationSearchParametersConverter;
import nl.knaw.huygens.timbuctoo.vre.SearchException;
import nl.knaw.huygens.timbuctoo.vre.SearchValidationException;
import nl.knaw.huygens.timbuctoo.vre.VRE;
import nl.knaw.huygens.timbuctoo.vre.VREManager;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.google.common.base.Predicate;
import com.google.common.collect.Lists;

public class SolrRelationSearcherTest {
  private SolrRelationSearcher instance;
  private String typeString = "relation";
  private FacetedSearchResult facetedSearchResult = new FacetedSearchResult();
  private SearchResult searchResult = new SearchResult();
  private SearchParametersV1 searchParametersV1 = new SearchParametersV1();
  private Class<? extends DomainEntity> type = Relation.class;
  private RelationSearchParametersConverter relationSearcherParametersConverterMock;
  private FacetedSearchResultConverter facetedSearchResultConverterMock;
  private Index indexMock;
  private VREManager vreManagerMock;
  private VRE vreMock;
  private TypeRegistry typeRegistryMock;
  private Repository repositoryMock;
  private CollectionConverter collectionConverterMock;
  private ArrayList<String> relationTypeIds = Lists.newArrayList("id1", "id2");
  private List<String> sourceIds = Lists.newArrayList("sourceId1", "sourceId2");
  private List<String> targetIds = Lists.newArrayList("targetId1", "targetId2");
  private String sourceSearchId = "sourceSearchId";
  private String targetSearchId = "targetSearchId";

  @Mock
  private List<Map<String, Object>> rawSearchResults;
  @Mock
  private FilterableSet<Map<String, Object>> filterableResults;

  @Before
  public void setup() throws Exception {
    MockitoAnnotations.initMocks(this);

    relationSearcherParametersConverterMock = mock(RelationSearchParametersConverter.class);
    indexMock = mock(Index.class);
    vreManagerMock = mock(VREManager.class);
    vreMock = mock(VRE.class);
    typeRegistryMock = mock(TypeRegistry.class);
    repositoryMock = mock(Repository.class);
    collectionConverterMock = mock(CollectionConverter.class);
    facetedSearchResultConverterMock = mock(FacetedSearchResultConverter.class);

    doReturn(type).when(typeRegistryMock).getDomainEntityType(typeString);
    when(vreManagerMock.getIndexFor(vreMock, type)).thenReturn(indexMock);
    when(indexMock.search(searchParametersV1)).thenReturn(facetedSearchResult);
    facetedSearchResult.setRawResults(rawSearchResults);
    when(collectionConverterMock.toFilterableSet(rawSearchResults)).thenReturn(filterableResults);
    when(repositoryMock.getEntity(SearchResult.class, sourceSearchId)).thenReturn(createSearchResult(sourceIds));
    when(repositoryMock.getEntity(SearchResult.class, targetSearchId)).thenReturn(createSearchResult(targetIds));
    when(facetedSearchResultConverterMock.convert(typeString, facetedSearchResult)).thenReturn(searchResult);

    instance = new SolrRelationSearcher(repositoryMock, vreManagerMock, relationSearcherParametersConverterMock, typeRegistryMock, collectionConverterMock, facetedSearchResultConverterMock);
  }

  private SearchResult createSearchResult(List<String> ids) {
    SearchResult searchResult = new SearchResult();
    searchResult.setIds(ids);

    return searchResult;
  }

  @Test
  public void testSearchWithRelationTypes() throws Exception {
    RelationSearchParameters relationSearchParameters = new RelationSearchParameters();
    relationSearchParameters.setTypeString(typeString);
    relationSearchParameters.setRelationTypeIds(relationTypeIds);
    relationSearchParameters.setSourceSearchId(sourceSearchId);
    relationSearchParameters.setTargetSearchId(targetSearchId);

    when(relationSearcherParametersConverterMock.toSearchParametersV1(relationSearchParameters)).thenReturn(searchParametersV1);

    // action
    SearchResult actualResult = instance.search(vreMock, Relation.class, relationSearchParameters);

    // verify
    verify(relationSearcherParametersConverterMock).toSearchParametersV1(relationSearchParameters);
    verify(indexMock).search(searchParametersV1);

    InOrder inOrder = Mockito.inOrder(filterableResults, facetedSearchResultConverterMock);
    inOrder.verify(filterableResults).filter(Mockito.<Predicate<Map<String, Object>>> any());
    inOrder.verify(facetedSearchResultConverterMock).convert(typeString, facetedSearchResult);

    assertThat(actualResult, equalTo(searchResult));
    assertThat(actualResult.getSourceIds(), containsInAnyOrder(sourceIds.toArray(new String[0])));
    assertThat(actualResult.getTargetIds(), containsInAnyOrder(targetIds.toArray(new String[0])));

  }

  @Test
  public void testSearchWithOutRelationTypesSpecified() throws Exception {
    RelationSearchParameters relationSearchParameters = new RelationSearchParameters();
    relationSearchParameters.setTypeString(typeString);
    relationSearchParameters.setSourceSearchId(sourceSearchId);
    relationSearchParameters.setTargetSearchId(targetSearchId);

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
    verify(indexMock).search(searchParametersV1);

    InOrder inOrder2 = Mockito.inOrder(filterableResults, facetedSearchResultConverterMock);
    inOrder2.verify(filterableResults).filter(Mockito.<Predicate<Map<String, Object>>> any());
    inOrder2.verify(facetedSearchResultConverterMock).convert(typeString, facetedSearchResult);

    assertThat(actualResult, equalTo(searchResult));
    assertThat(actualResult.getSourceIds(), containsInAnyOrder(sourceIds.toArray(new String[0])));
    assertThat(actualResult.getTargetIds(), containsInAnyOrder(targetIds.toArray(new String[0])));

    assertThat(actualResult, equalTo(searchResult));
  }

  @Test(expected = SearchValidationException.class)
  public void testSearchIndexThrowsASearchValidationException() throws Exception {
    testSearchIndexThrowsAnException(SearchValidationException.class);
  }

  @Test(expected = SearchException.class)
  public void testSearchIndexThrowsASearchException() throws Exception {
    testSearchIndexThrowsAnException(SearchException.class);
  }

  private void testSearchIndexThrowsAnException(Class<? extends Exception> exceptionToBeThrown) throws SearchException, SearchValidationException {
    RelationSearchParameters relationSearchParameters = new RelationSearchParameters();
    relationSearchParameters.setTypeString(typeString);
    relationSearchParameters.setRelationTypeIds(relationTypeIds);
    relationSearchParameters.setSourceSearchId(sourceSearchId);
    relationSearchParameters.setTargetSearchId(targetSearchId);

    when(relationSearcherParametersConverterMock.toSearchParametersV1(relationSearchParameters)).thenReturn(searchParametersV1);
    doThrow(exceptionToBeThrown).when(indexMock).search(searchParametersV1);

    // action
    instance.search(vreMock, Relation.class, relationSearchParameters);

    // verify
    verify(relationSearcherParametersConverterMock).toSearchParametersV1(relationSearchParameters);
    verify(indexMock).search(searchParametersV1);
  }
}
