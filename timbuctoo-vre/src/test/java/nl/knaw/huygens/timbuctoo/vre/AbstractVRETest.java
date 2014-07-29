package nl.knaw.huygens.timbuctoo.vre;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import nl.knaw.huygens.facetedsearch.model.FacetedSearchResult;
import nl.knaw.huygens.facetedsearch.model.parameters.DefaultFacetedSearchParameters;
import nl.knaw.huygens.timbuctoo.index.Index;
import nl.knaw.huygens.timbuctoo.index.IndexCollection;
import nl.knaw.huygens.timbuctoo.index.IndexException;
import nl.knaw.huygens.timbuctoo.index.model.ExplicitlyAnnotatedModel;
import nl.knaw.huygens.timbuctoo.model.SearchResult;
import nl.knaw.huygens.timbuctoo.search.FacetedSearchResultConverter;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;

public class AbstractVRETest {
  private static final String ID = "ID";
  private static final Class<ExplicitlyAnnotatedModel> TYPE = ExplicitlyAnnotatedModel.class;
  private static final String TYPE_STRING = "explicitlyannotatedmodel";
  private AbstractVRE instance;
  private IndexCollection indexCollectionMock;
  private DefaultFacetedSearchParameters searchParameters = new DefaultFacetedSearchParameters();
  private Index indexMock = mock(Index.class);
  private FacetedSearchResultConverter facetedSearchResultConverterMock = mock(FacetedSearchResultConverter.class);

  @Before
  public void setUp() {
    indexCollectionMock = mock(IndexCollection.class);
    when(indexCollectionMock.getIndexByType(TYPE)).thenReturn(indexMock);

    instance = new AbstractVRE(indexCollectionMock, facetedSearchResultConverterMock) {

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
        // TODO Auto-generated method stub
        return null;
      }
    };
  }

  @Test
  public void getIndexForTypeRedirectsTheCallToIndexCollection() {
    // action
    Index index = instance.getIndexForType(TYPE);

    // verify
    verify(indexCollectionMock).getIndexByType(TYPE);
    assertNotNull(index);
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
  public void testSearch() throws SearchException, SearchValidationException {
    FacetedSearchResult facetedSearchResult = new FacetedSearchResult();
    SearchResult searchResult = new SearchResult();

    when(indexMock.search(searchParameters)).thenReturn(facetedSearchResult);
    when(facetedSearchResultConverterMock.convert(TYPE_STRING, facetedSearchResult)).thenReturn(searchResult);

    // action
    SearchResult actualSearchResult = instance.search(TYPE, searchParameters);

    // verify
    assertThat(actualSearchResult, is(searchResult));
    verify(indexMock).search(searchParameters);
    verify(facetedSearchResultConverterMock).convert(TYPE_STRING, facetedSearchResult);
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
      verifyZeroInteractions(facetedSearchResultConverterMock);
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

  protected void setupIndexIterator(Index indexMock1, Index indexMock2) {
    when(indexCollectionMock.iterator()).thenReturn(Lists.newArrayList(indexMock1, indexMock2).iterator());
  }

  @Test(expected = IndexException.class)
  public void clearEntitiesShouldThrowAnExceptionWhenAnIndexThrowsAnException() throws IndexException {
    // setup
    Index indexMock1 = mock(Index.class);
    Index indexMock2 = mock(Index.class);

    setupIndexIterator(indexMock1, indexMock2);
    doThrow(IndexException.class).when(indexMock1).clear();

    // action
    instance.clearIndexes();

    // verify
    verify(indexMock1).clear();
    verifyZeroInteractions(indexMock2);
  }

}
