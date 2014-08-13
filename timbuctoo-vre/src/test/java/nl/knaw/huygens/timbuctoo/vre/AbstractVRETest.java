package nl.knaw.huygens.timbuctoo.vre;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import nl.knaw.huygens.facetedsearch.model.FacetedSearchResult;
import nl.knaw.huygens.facetedsearch.model.parameters.DefaultFacetedSearchParameters;
import nl.knaw.huygens.timbuctoo.index.Index;
import nl.knaw.huygens.timbuctoo.index.IndexCollection;
import nl.knaw.huygens.timbuctoo.index.IndexException;
import nl.knaw.huygens.timbuctoo.index.IndexStatus;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.SearchResult;
import nl.knaw.huygens.timbuctoo.search.FacetedSearchResultProcessor;
import nl.knaw.huygens.timbuctoo.search.converters.RegularFacetedSearchResultConverter;

import org.junit.Before;
import org.junit.Test;

import test.timbuctoo.index.model.ExplicitlyAnnotatedModel;
import test.timbuctoo.index.model.Type1;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class AbstractVRETest {
  private static final Class<Type1> OTHER_TYPE = Type1.class;
  private static final String ID = "ID";
  private static final Class<ExplicitlyAnnotatedModel> TYPE = ExplicitlyAnnotatedModel.class;
  private static final String TYPE_STRING = "explicitlyannotatedmodel";
  private AbstractVRE instance;
  private IndexCollection indexCollectionMock;
  private DefaultFacetedSearchParameters searchParameters = new DefaultFacetedSearchParameters();
  private Index indexMock = mock(Index.class);
  private RegularFacetedSearchResultConverter resultConverterMock = mock(RegularFacetedSearchResultConverter.class);
  private Scope scopeMock;

  @Before
  public void setUp() {
    indexCollectionMock = mock(IndexCollection.class);
    scopeMock = mock(Scope.class);
    when(indexCollectionMock.getIndexByType(TYPE)).thenReturn(indexMock);

    instance = new AbstractVRE(indexCollectionMock, resultConverterMock) {

      @Override
      public String getScopeId() {
        // TODO Auto-generated method stub
        return null;
      }

      @Override
      public String getName() {
        // TODO Auto-generated method stub
        return null;
      }

      @Override
      public String getDescription() {
        // TODO Auto-generated method stub
        return null;
      }

      @Override
      protected Scope createScope() throws IOException {
        return scopeMock;
      }
    };
  }

  @Test
  public void getIndexesShouldRerturnAllTheIndexesOfTheIndexCollection() {
    // setup
    Index indexMock1 = mock(Index.class);
    Index indexMock2 = mock(Index.class);

    when(indexCollectionMock.getAll()).thenReturn(Lists.newArrayList(indexMock1, indexMock2));

    // action
    Collection<Index> indexes = instance.getIndexes();

    // verify
    verify(indexCollectionMock).getAll();
    assertThat(indexes, contains(new Index[] { indexMock1, indexMock2 }));
  }

  @Test
  public void testDefaultSearch() throws SearchException, SearchValidationException {
    FacetedSearchResult facetedSearchResult = new FacetedSearchResult();
    SearchResult searchResult = new SearchResult();

    when(indexMock.search(searchParameters)).thenReturn(facetedSearchResult);
    when(resultConverterMock.convert(TYPE_STRING, facetedSearchResult)).thenReturn(searchResult);

    // action
    SearchResult actualSearchResult = instance.search(TYPE, searchParameters);

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
    SearchResult actualSearchResult = instance.search(TYPE, searchParameters, resultConverterMock);

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
    SearchResult actualSearchResult = instance.search(TYPE, searchParameters, resultConverterMock, resultProcessorMock1, resultProcessorMock2);

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

  @Test
  public void deleteFromIndexShouldDelegateTheCallToTheRightIndex() throws IndexException {
    // action
    instance.deleteFromIndex(TYPE, ID);

    // verify
    verify(indexMock).deleteById(ID);

  }

  private void testSearchIndexThrowsAnException(Class<? extends Exception> exceptionToThrow) throws SearchException, SearchValidationException {
    doThrow(exceptionToThrow).when(indexMock).search(searchParameters);

    try {
      // action
      instance.search(TYPE, searchParameters);
    } finally {
      verify(indexMock).search(searchParameters);
      verifyZeroInteractions(resultConverterMock);
    }
  }

  @Test(expected = IndexException.class)
  public void deleteFromIndexShouldThrowTheIndexExceptionsTheIndexThrows() throws IndexException {
    // setup
    doThrow(IndexException.class).when(indexMock).deleteById(ID);

    // action
    instance.deleteFromIndex(TYPE, ID);
  }

  @Test
  public void deleteFromIndexShouldRemoveMultipleItemsFromTheRightIndex() throws IndexException {
    // setup
    List<String> ids = Lists.newArrayList(ID, "id2", "id3");

    // action
    instance.deleteFromIndex(TYPE, ids);

    // verify
    verify(indexMock).deleteById(ids);
  }

  @Test(expected = IndexException.class)
  public void deleteMultipleFromIndexShouldThrowTheIndexExceptionsTheIndexThrows() throws IndexException {
    // setup
    List<String> ids = Lists.newArrayList(ID, "id2", "id3");
    doThrow(IndexException.class).when(indexMock).deleteById(ids);

    // action
    instance.deleteFromIndex(TYPE, ids);
  }

  @Test
  public void clearEntitiesShouldClearAllTheIndexesOfThisVRE() throws IndexException {
    // setup
    Index indexMock1 = mock(Index.class);
    Index indexMock2 = mock(Index.class);

    setupIndexIterator(indexMock1, indexMock2);

    // action
    instance.clearIndexes();

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
      instance.clearIndexes();
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

    ExplicitlyAnnotatedModel entityInScope = mock(TYPE);
    List<DomainEntity> filteredVariations = Lists.newArrayList();
    filteredVariations.add(entityInScope);

    when(scopeMock.filter(variations)).thenReturn(filteredVariations);

    // action
    instance.addToIndex(TYPE, variations);

    // verify
    verify(indexMock).add(filteredVariations);
  }

  @Test(expected = IndexException.class)
  public void addToIndexThrowsAnExceptionWhenIndexAddThrowsOne() throws IndexException {
    // setup
    List<DomainEntity> variations = Lists.newArrayList();

    ExplicitlyAnnotatedModel entityInScope = mock(TYPE);
    List<DomainEntity> filteredVariations = Lists.newArrayList();
    filteredVariations.add(entityInScope);

    when(scopeMock.filter(variations)).thenReturn(filteredVariations);
    doThrow(IndexException.class).when(indexMock).add(filteredVariations);

    try {
      // action
      instance.addToIndex(TYPE, variations);
    } finally {
      // verify
      verify(indexMock).add(filteredVariations);
    }
  }

  @Test
  public void updateIndexDeterminesTheIndexAndCallsItsUpdateFunctionWithAFilteredVariations() throws IndexException {
    // setup
    List<DomainEntity> variations = Lists.newArrayList();

    ExplicitlyAnnotatedModel entityInScope = mock(TYPE);
    List<DomainEntity> filteredVariations = Lists.newArrayList();
    filteredVariations.add(entityInScope);

    when(scopeMock.filter(variations)).thenReturn(filteredVariations);

    // action
    instance.updateIndex(TYPE, variations);

    // verify
    verify(indexMock).update(filteredVariations);
  }

  @Test(expected = IndexException.class)
  public void updateThrowsAnExceptionWhenIndexUpdateThrowsOne() throws IndexException {
    // setup
    List<DomainEntity> variations = Lists.newArrayList();

    ExplicitlyAnnotatedModel entityInScope = mock(TYPE);
    List<DomainEntity> filteredVariations = Lists.newArrayList();
    filteredVariations.add(entityInScope);

    when(scopeMock.filter(variations)).thenReturn(filteredVariations);
    doThrow(IndexException.class).when(indexMock).update(filteredVariations);

    try {
      // action
      instance.updateIndex(TYPE, variations);
    } finally {
      // verify
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
    instance.close();

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
    instance.close();

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
    instance.commitAll();

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
      instance.commitAll();
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
    Index indexMock1 = mock(Index.class);
    Index indexMock2 = mock(Index.class);

    setupScopeGetBaseEntityTypesWith(TYPE, OTHER_TYPE);

    when(indexCollectionMock.getIndexByType(TYPE)).thenReturn(indexMock1);
    when(indexCollectionMock.getIndexByType(OTHER_TYPE)).thenReturn(indexMock2);

    long indexMock1Count = 100l;
    when(indexMock1.getCount()).thenReturn(indexMock1Count);
    long indexMock2Count = 133l;
    when(indexMock2.getCount()).thenReturn(indexMock2Count);

    IndexStatus indexStatus = mock(IndexStatus.class);

    // action
    instance.addToIndexStatus(indexStatus);

    // verify
    verify(indexStatus).addCount(instance, TYPE, indexMock1Count);
    verify(indexStatus).addCount(instance, OTHER_TYPE, indexMock2Count);
  }

  @SuppressWarnings("unchecked")
  @Test
  public void addToIndexStatusAddsAllTheIndexStatussesThatDoNotThrowAnException() throws IndexException {
    // setup
    Index indexMock1 = mock(Index.class);
    Index indexMock2 = mock(Index.class);

    setupScopeGetBaseEntityTypesWith(TYPE, OTHER_TYPE);

    when(indexCollectionMock.getIndexByType(TYPE)).thenReturn(indexMock1);
    when(indexCollectionMock.getIndexByType(OTHER_TYPE)).thenReturn(indexMock2);

    doThrow(IndexException.class).when(indexMock1).getCount();
    long indexMock2Count = 133l;
    when(indexMock2.getCount()).thenReturn(indexMock2Count);

    IndexStatus indexStatus = mock(IndexStatus.class);

    // action
    instance.addToIndexStatus(indexStatus);

    // verify
    verify(indexStatus).addCount(instance, OTHER_TYPE, indexMock2Count);
    verifyNoMoreInteractions(indexStatus);
  }

  protected void setupScopeGetBaseEntityTypesWith(Class<? extends DomainEntity>... types) {
    Set<Class<? extends DomainEntity>> typeSet = Sets.newHashSet(types);
    when(scopeMock.getBaseEntityTypes()).thenReturn(typeSet);
  }
}
