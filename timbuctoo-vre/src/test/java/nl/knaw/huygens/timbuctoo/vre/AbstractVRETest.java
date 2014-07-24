package nl.knaw.huygens.timbuctoo.vre;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;

import nl.knaw.huygens.facetedsearch.model.FacetedSearchResult;
import nl.knaw.huygens.facetedsearch.model.parameters.DefaultFacetedSearchParameters;
import nl.knaw.huygens.timbuctoo.index.Index;
import nl.knaw.huygens.timbuctoo.index.model.ExplicitlyAnnotatedModel;
import nl.knaw.huygens.timbuctoo.model.SearchResult;
import nl.knaw.huygens.timbuctoo.search.FacetedSearchResultConverter;

import org.junit.Before;
import org.junit.Test;

public class AbstractVRETest {
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

}
