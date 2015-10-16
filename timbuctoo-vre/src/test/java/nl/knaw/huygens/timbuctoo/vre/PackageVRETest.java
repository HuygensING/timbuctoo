package nl.knaw.huygens.timbuctoo.vre;

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
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import nl.knaw.huygens.facetedsearch.model.FacetedSearchResult;
import nl.knaw.huygens.facetedsearch.model.parameters.DefaultFacetedSearchParameters;
import nl.knaw.huygens.facetedsearch.model.parameters.SortDirection;
import nl.knaw.huygens.facetedsearch.model.parameters.SortParameter;
import nl.knaw.huygens.timbuctoo.Repository;
import nl.knaw.huygens.timbuctoo.config.TypeNames;
import nl.knaw.huygens.timbuctoo.index.Index;
import nl.knaw.huygens.timbuctoo.index.IndexCollection;
import nl.knaw.huygens.timbuctoo.index.IndexException;
import nl.knaw.huygens.timbuctoo.index.IndexStatus;
import nl.knaw.huygens.timbuctoo.index.RawSearchUnavailableException;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.SearchResult;
import nl.knaw.huygens.timbuctoo.search.FacetedSearchResultProcessor;
import nl.knaw.huygens.timbuctoo.search.converters.SearchResultConverter;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;
import test.timbuctoo.index.model.BaseType1;
import test.timbuctoo.index.model.projecta.ProjectAType1;
import test.timbuctoo.index.model.projecta.ProjectAType2;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

public class PackageVRETest {

  private static final Class<ProjectAType2> OTHER_TYPE = ProjectAType2.class;
  private static final String ID = "ID";
  private static final Class<ProjectAType1> TYPE = ProjectAType1.class;
  private static final String TYPE_STRING = TypeNames.getInternalName(TYPE);
  private static final String QUERY = "query";
  private static final int ROWS = 20;
  private static final int START = 0;
  private static final Map<String, Object> FILTERS = Maps.newHashMap();
  private static final String INDEX_NAME = "indexName";
  private static final Class<BaseType1> BASE_TYPE = BaseType1.class;
  private static final List<SortParameter> SORT = Lists.newArrayList(new SortParameter("field", SortDirection.ASCENDING));

  private final DefaultFacetedSearchParameters searchParameters = new DefaultFacetedSearchParameters();
  private final Index indexMock = mock(Index.class);
  private final SearchResultConverter resultConverterMock = mock(SearchResultConverter.class);

  private IndexCollection indexCollectionMock;
  private Scope scopeMock;
  private PackageVRE vre;
  private Repository repositoryMock;

  @Before
  public void setup() {
    indexCollectionMock = mock(IndexCollection.class);
    scopeMock = mock(Scope.class);
    when(indexCollectionMock.getIndexByType(TYPE)).thenReturn(indexMock);
    repositoryMock = mock(Repository.class);
    vre = new PackageVRE("vreId", "description", scopeMock, indexCollectionMock, resultConverterMock, repositoryMock);
  }

  @Test
  public void getIndexesShouldRerturnAllTheIndexesOfTheIndexCollection() {
    // setup
    Index indexMock1 = mock(Index.class);
    Index indexMock2 = mock(Index.class);

    when(indexCollectionMock.getAll()).thenReturn(Lists.newArrayList(indexMock1, indexMock2));

    // action
    Collection<Index> indexes = vre.getIndexes();

    // verify
    assertThat(indexes, contains(new Index[]{indexMock1, indexMock2}));
  }

  @Test
  public void testDefaultSearch() throws SearchException, SearchValidationException {
    FacetedSearchResult facetedSearchResult = new FacetedSearchResult();
    SearchResult searchResult = new SearchResult();

    when(indexMock.search(searchParameters)).thenReturn(facetedSearchResult);
    when(resultConverterMock.convert(TYPE_STRING, facetedSearchResult)).thenReturn(searchResult);

    // action
    SearchResult actualSearchResult = vre.search(TYPE, searchParameters);

    // verify
    assertThat(actualSearchResult, is(searchResult));
    verify(indexMock).search(searchParameters);
    verify(resultConverterMock).convert(TYPE_STRING, facetedSearchResult);
  }

  @Test
  public void testCustomSearchWithoutSearchResultProcessors() throws SearchException, SearchValidationException {
    FacetedSearchResult facetedSearchResult = new FacetedSearchResult();
    SearchResult searchResult = new SearchResult();

    when(indexMock.search(searchParameters)).thenReturn(facetedSearchResult);
    when(resultConverterMock.convert(TYPE_STRING, facetedSearchResult)).thenReturn(searchResult);

    // action
    SearchResult actualSearchResult = vre.search(TYPE, searchParameters);

    // verify
    assertThat(actualSearchResult, is(searchResult));
    verify(indexMock).search(searchParameters);
    verify(resultConverterMock).convert(TYPE_STRING, facetedSearchResult);
  }

  @Test
  public void testCustomSearchWithSearchResultProcessors() throws SearchException, SearchValidationException {
    FacetedSearchResult facetedSearchResult = new FacetedSearchResult();
    SearchResult searchResult = new SearchResult();

    FacetedSearchResultProcessor resultProcessorMock1 = mock(FacetedSearchResultProcessor.class);
    FacetedSearchResultProcessor resultProcessorMock2 = mock(FacetedSearchResultProcessor.class);

    when(indexMock.search(searchParameters)).thenReturn(facetedSearchResult);
    when(resultConverterMock.convert(TYPE_STRING, facetedSearchResult)).thenReturn(searchResult);

    // action
    SearchResult actualSearchResult = vre.search(TYPE, searchParameters, resultProcessorMock1, resultProcessorMock2);

    // verify
    assertThat(actualSearchResult, is(searchResult));
    verify(indexMock).search(searchParameters);
    verify(resultProcessorMock1).process(facetedSearchResult);
    verify(resultProcessorMock2).process(facetedSearchResult);
    verify(resultConverterMock).convert(TYPE_STRING, facetedSearchResult);
  }

  @Test(expected = SearchException.class)
  public void testSearchIndexThrowsAnSearchException() throws SearchException, SearchValidationException {
    testSearchIndexThrowsAnException(SearchException.class);
  }

  @Test(expected = SearchValidationException.class)
  public void testSearchIndexThrowsAnSearchValidationException() throws SearchException, SearchValidationException {
    testSearchIndexThrowsAnException(SearchValidationException.class);
  }

  private void testSearchIndexThrowsAnException(Class<? extends Exception> exceptionToThrow) throws SearchException, SearchValidationException {
    doThrow(exceptionToThrow).when(indexMock).search(searchParameters);

    try {
      // action
      vre.search(TYPE, searchParameters);
    } finally {
      verify(indexMock).search(searchParameters);
      verifyZeroInteractions(resultConverterMock);
    }
  }

  @Test
  public void deleteFromIndexShouldDelegateTheCallToTheRightIndex() throws IndexException {
    // action
    vre.deleteFromIndex(TYPE, ID);

    // verify
    verify(indexMock).deleteById(ID);
  }

  @Test(expected = IndexException.class)
  public void deleteFromIndexShouldThrowTheIndexExceptionsTheIndexThrows() throws IndexException {
    // setup
    doThrow(IndexException.class).when(indexMock).deleteById(ID);

    // action
    vre.deleteFromIndex(TYPE, ID);
  }

  @Test
  public void deleteFromIndexShouldRemoveMultipleItemsFromTheRightIndex() throws IndexException {
    // setup
    List<String> ids = Lists.newArrayList(ID, "id2", "id3");

    // action
    vre.deleteFromIndex(TYPE, ids);

    // verify
    verify(indexMock).deleteById(ids);
  }

  @Test(expected = IndexException.class)
  public void deleteMultipleFromIndexShouldThrowTheIndexExceptionsTheIndexThrows() throws IndexException {
    // setup
    List<String> ids = Lists.newArrayList(ID, "id2", "id3");
    doThrow(IndexException.class).when(indexMock).deleteById(ids);

    // action
    vre.deleteFromIndex(TYPE, ids);
  }

  @Test
  public void clearEntitiesShouldClearAllTheIndexesOfThisVRE() throws IndexException {
    // setup
    Index indexMock1 = mock(Index.class);
    Index indexMock2 = mock(Index.class);

    setupIndexIterator(indexMock1, indexMock2);

    // action
    vre.clearIndexes();

    // verify
    verify(indexMock1).clear();
    verify(indexMock2).clear();
  }

  private void setupIndexIterator(Index indexMock1, Index indexMock2) {
    when(indexCollectionMock.iterator()).thenReturn(Lists.newArrayList(indexMock1, indexMock2).iterator());
  }

  @Test(expected = IndexException.class)
  public void clearEntitiesShouldThrowAnExceptionWhenAnIndexThrowsAnException() throws IndexException {
    // setup
    Index indexMock1 = mock(Index.class);
    Index indexMock2 = mock(Index.class);

    setupIndexIterator(indexMock1, indexMock2);
    doThrow(IndexException.class).when(indexMock1).clear();

    try {
      // action
      vre.clearIndexes();
    } finally {
      // verify
      verify(indexMock1).clear();
      verifyZeroInteractions(indexMock2);
    }

  }

  @Test
  public void addToIndexDeterminesTheIndexAndCallsItsAddFunctionWithAFilteredVariations() throws IndexException {
    // setup
    List<DomainEntity> variations = Lists.newArrayList();

    ProjectAType1 entityInScope = mock(TYPE);
    List<DomainEntity> filteredVariations = Lists.newArrayList();
    filteredVariations.add(entityInScope);

    when(scopeMock.filter(variations)).thenReturn(filteredVariations);

    // action
    vre.addToIndex(TYPE, variations);

    // verify
    InOrder inOrder = Mockito.inOrder(repositoryMock, indexMock);
    inOrder.verify(repositoryMock).addDerivedProperties(vre, entityInScope);
    inOrder.verify(indexMock).add(filteredVariations);
  }

  @Test(expected = IndexException.class)
  public void addToIndexThrowsAnExceptionWhenIndexAddThrowsOne() throws IndexException {
    // setup
    List<DomainEntity> variations = Lists.newArrayList();

    ProjectAType1 entityInScope = mock(TYPE);
    List<DomainEntity> filteredVariations = Lists.newArrayList();
    filteredVariations.add(entityInScope);

    when(scopeMock.filter(variations)).thenReturn(filteredVariations);
    doThrow(IndexException.class).when(indexMock).add(filteredVariations);

    try {
      // action
      vre.addToIndex(TYPE, variations);
    } finally {
      // verify
      verify(repositoryMock).addDerivedProperties(vre, entityInScope);
      verify(indexMock).add(filteredVariations);
    }
  }

  @Test
  public void updateIndexDeterminesTheIndexAndCallsItsUpdateFunctionWithAFilteredVariations() throws IndexException {
    // setup
    List<DomainEntity> variations = Lists.newArrayList();

    ProjectAType1 entityInScope = mock(TYPE);
    List<DomainEntity> filteredVariations = Lists.newArrayList();
    filteredVariations.add(entityInScope);

    when(scopeMock.filter(variations)).thenReturn(filteredVariations);

    // action
    vre.updateIndex(TYPE, variations);

    // verify
    InOrder inOrder = Mockito.inOrder(repositoryMock, indexMock);
    inOrder.verify(repositoryMock).addDerivedProperties(vre, entityInScope);
    inOrder.verify(indexMock).update(filteredVariations);
  }

  @Test(expected = IndexException.class)
  public void updateThrowsAnExceptionWhenIndexUpdateThrowsOne() throws IndexException {
    // setup
    List<DomainEntity> variations = Lists.newArrayList();

    ProjectAType1 entityInScope = mock(TYPE);
    List<DomainEntity> filteredVariations = Lists.newArrayList();
    filteredVariations.add(entityInScope);

    when(scopeMock.filter(variations)).thenReturn(filteredVariations);
    doThrow(IndexException.class).when(indexMock).update(filteredVariations);

    try {
      // action
      vre.updateIndex(TYPE, variations);
    } finally {
      // verify
      verify(repositoryMock).addDerivedProperties(vre, entityInScope);
      verify(indexMock).update(filteredVariations);
    }
  }

  @Test
  public void closeTriesToCloseAllTheIndexes() throws IndexException {
    // setup
    Index indexMock1 = mock(Index.class);
    Index indexMock2 = mock(Index.class);

    setupIndexIterator(indexMock1, indexMock2);

    // action
    vre.close();

    // verify
    verify(indexMock1).close();
    verify(indexMock2).close();
  }

  @Test
  public void closeTriesToCloseAllTheIndexesEvenIfOneThrowsAnException() throws IndexException {
    // setup
    Index indexMock1 = mock(Index.class);
    Index indexMock2 = mock(Index.class);

    setupIndexIterator(indexMock1, indexMock2);
    doThrow(IndexException.class).when(indexMock1).close();

    // action
    vre.close();

    // verify
    verify(indexMock1).close();
    verify(indexMock2).close();
  }

  @Test
  public void commitAllCallsCommitOnAllTheIndexes() throws IndexException {
    // setup
    Index indexMock1 = mock(Index.class);
    Index indexMock2 = mock(Index.class);

    setupIndexIterator(indexMock1, indexMock2);

    // action
    vre.commitAll();

    // verify
    verify(indexMock1).commit();
    verify(indexMock2).commit();
  }

  @Test(expected = IndexException.class)
  public void commitAllThrowsAnIndexExceptionWhenACommitOfAnIndexThrowsOne() throws IndexException {
    // setup
    Index indexMock1 = mock(Index.class);
    Index indexMock2 = mock(Index.class);

    setupIndexIterator(indexMock1, indexMock2);
    doThrow(IndexException.class).when(indexMock1).commit();

    try {
      // action
      vre.commitAll();
    } finally {
      // verify
      verify(indexMock1).commit();
      verifyNoMoreInteractions(indexMock2);
    }
  }

  @SuppressWarnings("unchecked")
  @Test
  public void addToIndexStatusAddsTheStatusOfAllTheIndexesToTheIndexStatus() throws IndexException {
    // setup
    setupScopeGetBaseEntityTypesWith(TYPE, OTHER_TYPE);

    Index indexMock1 = indexFoundFor(TYPE);
    Index indexMock2 = indexFoundFor(OTHER_TYPE);

    long indexMock1Count = 100l;
    when(indexMock1.getCount()).thenReturn(indexMock1Count);
    long indexMock2Count = 133l;
    when(indexMock2.getCount()).thenReturn(indexMock2Count);

    IndexStatus indexStatus = mock(IndexStatus.class);

    // action
    vre.addToIndexStatus(indexStatus);

    // verify
    verify(indexStatus).addCount(vre, TYPE, indexMock1Count);
    verify(indexStatus).addCount(vre, OTHER_TYPE, indexMock2Count);
  }

  @SuppressWarnings("unchecked")
  @Test
  public void addToIndexStatusAddsAllTheIndexStatussesThatDoNotThrowAnException() throws IndexException {
    // setup
    setupScopeGetBaseEntityTypesWith(TYPE, OTHER_TYPE);

    Index indexMock1 = indexFoundFor(TYPE);
    Index indexMock2 = indexFoundFor(OTHER_TYPE);

    doThrow(IndexException.class).when(indexMock1).getCount();
    long indexMock2Count = 133l;
    when(indexMock2.getCount()).thenReturn(indexMock2Count);

    IndexStatus indexStatus = mock(IndexStatus.class);

    // action
    vre.addToIndexStatus(indexStatus);

    // verify
    verify(indexStatus).addCount(vre, OTHER_TYPE, indexMock2Count);
    verifyNoMoreInteractions(indexStatus);
  }

  protected void setupScopeGetBaseEntityTypesWith(Class<? extends DomainEntity>... types) {
    Set<Class<? extends DomainEntity>> typeSet = Sets.newHashSet(types);
    when(scopeMock.getPrimitiveEntityTypes()).thenReturn(typeSet);

    for (Class<? extends DomainEntity> type : types) {
      when(scopeMock.inScope(type)).thenReturn(true);
    }
  }

  @SuppressWarnings("unchecked")
  @Test
  public void doRawSearchCallsDoRawSearchOfTheIndexCorrespondingWithTheType() throws Exception {
    // setup
    setupScopeGetBaseEntityTypesWith(TYPE);

    Index indexMock1 = indexFoundFor(TYPE);
    Iterable<Map<String, Object>> rawSearchResult = Lists.newArrayList();
    when(indexMock1.doRawSearch(QUERY, START, ROWS, FILTERS)).thenReturn(rawSearchResult);

    // action
    Iterable<Map<String, Object>> actualSearchResult = vre.doRawSearch(TYPE, QUERY, 0, 20, FILTERS);

    // verify
    assertThat(actualSearchResult, is(sameInstance(rawSearchResult)));

    verify(indexMock1).doRawSearch(QUERY, START, ROWS, FILTERS);
  }

  @SuppressWarnings("unchecked")
  @Test(expected = NotInScopeException.class)
  public void doRawSearchThrowsANotInScopeExceptionWhenTheTypeIsNotInTheScopeOfTheVRE() throws Exception {
    // setup
    setupScopeGetBaseEntityTypesWith(TYPE);

    // action
    vre.doRawSearch(OTHER_TYPE, QUERY, START, ROWS, FILTERS);
  }

  @SuppressWarnings("unchecked")
  @Test(expected = SearchException.class)
  public void doRawSearchThrowsASearchExceptionIfTheIndexThrowsOne() throws Exception {
    // setup
    setupScopeGetBaseEntityTypesWith(TYPE);

    Index indexMock1 = indexFoundFor(TYPE);
    when(indexMock1.doRawSearch(QUERY, START, ROWS, FILTERS)).thenThrow(new SearchException(new Exception()));

    // action
    vre.doRawSearch(TYPE, QUERY, 0, 20, FILTERS);
  }

  @SuppressWarnings("unchecked")
  @Test(expected = RawSearchUnavailableException.class)
  public void doRawSearchThrowsARawSearchUnavailableExceptionIfTheIndexThrowsOne() throws Exception {
    // setup
    setupScopeGetBaseEntityTypesWith(TYPE);

    Index indexMock1 = indexFoundFor(TYPE);
    when(indexMock1.doRawSearch(QUERY, START, ROWS, FILTERS)).thenThrow(new RawSearchUnavailableException(INDEX_NAME));

    // action
    vre.doRawSearch(TYPE, QUERY, 0, 20, FILTERS);
  }

  private Index indexFoundFor(Class<? extends DomainEntity> type) {
    Index index = mock(Index.class);

    when(indexCollectionMock.getIndexByType(type)).thenReturn(index);

    return index;
  }

  @Test
  public void getRawDataForCallsTheSelectedIndexesGetDataByIds() throws Exception {
    // setup
    setupScopeGetBaseEntityTypesWith(TYPE);

    List<String> ids = Lists.newArrayList();
    Index index = indexFoundFor(TYPE);
    List<Map<String, Object>> rawData = Lists.newArrayList();
    when(index.getDataByIds(ids, SORT)).thenReturn(rawData);

    // action
    List<Map<String, Object>> actualRawData = vre.getRawDataFor(TYPE, ids, SORT);

    // verify
    assertThat(actualRawData, is(sameInstance(rawData)));
  }

  @Test(expected = NotInScopeException.class)
  public void getRawDataForThrowsANotInScopeExceptionWhenTheTypeIsNoInScope() throws Exception {
    // setup
    setupScopeGetBaseEntityTypesWith(TYPE);

    // action
    vre.getRawDataFor(OTHER_TYPE, Lists.<String>newArrayList(), SORT);
  }


  @Test(expected = SearchException.class)
  public void getRawDataForThrowsASearchExceptionWhenTheIndexDoes() throws Exception {
    // setup
    setupScopeGetBaseEntityTypesWith(TYPE);
    Index index = indexFoundFor(TYPE);
    ArrayList<String> ids = Lists.<String>newArrayList();
    when(index.getDataByIds(ids, SORT)).thenThrow(new SearchException(new Exception()));

    // action
    vre.getRawDataFor(TYPE, ids, SORT);
  }

  @Test
  public void mapToScopeTypeTranslatesTheBaseEntityToTheTypeSpecificToTheVRE() throws Exception {
    // setup
    doReturn(TYPE).when(scopeMock).mapToScopeType(BASE_TYPE);

    // action
    Class<? extends DomainEntity> scopeType = vre.mapToScopeType(BASE_TYPE);

    // verify
    assertThat(scopeType, is(sameInstance(TYPE)));
  }

  @Test(expected = NotInScopeException.class)
  public void mapToScopeThrowsANotInScopeExceptionWhenThereIsNoTypeInScopeThatMatchesTheBaseType() throws Exception {
    // setup
    when(scopeMock.mapToScopeType(BASE_TYPE)).thenThrow(NotInScopeException.noTypeMatchesBaseType(BASE_TYPE));

    // action
    Class<? extends DomainEntity> scopeType = vre.mapToScopeType(BASE_TYPE);
  }
}
