package nl.knaw.huygens.timbuctoo.index;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import nl.knaw.huygens.facetedsearch.FacetedSearchException;
import nl.knaw.huygens.facetedsearch.FacetedSearchLibrary;
import nl.knaw.huygens.facetedsearch.model.FacetedSearchResult;
import nl.knaw.huygens.facetedsearch.model.NoSuchFieldInIndexException;
import nl.knaw.huygens.facetedsearch.model.parameters.DefaultFacetedSearchParameters;
import nl.knaw.huygens.solr.AbstractSolrServer;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;

import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.google.common.collect.Lists;

public class SolrIndexTest {
  @Mock
  private List<? extends DomainEntity> variationsToAdd;
  private AbstractSolrServer solrServerMock;
  private SolrInputDocument solrInputDocumentMock;
  private SolrInputDocumentCreator documentCreatorMock;
  private FacetedSearchLibrary facetedSearchLibraryMock;
  private SolrIndex instance;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    solrServerMock = mock(AbstractSolrServer.class);
    solrInputDocumentMock = mock(SolrInputDocument.class);
    documentCreatorMock = mock(SolrInputDocumentCreator.class);
    facetedSearchLibraryMock = mock(FacetedSearchLibrary.class);

    instance = new SolrIndex("indexName", documentCreatorMock, solrServerMock, facetedSearchLibraryMock);
  }

  @Test
  public void testAdd() throws SolrServerException, IOException, IndexException {
    // when
    when(documentCreatorMock.create(variationsToAdd)).thenReturn(solrInputDocumentMock);

    // action
    instance.add(variationsToAdd);

    // verify
    verifyTheIndexIsUpdated();

  }

  private void verifyTheIndexIsUpdated() throws SolrServerException, IOException {
    InOrder inOrder = Mockito.inOrder(documentCreatorMock, solrServerMock);
    inOrder.verify(documentCreatorMock).create(variationsToAdd);
    inOrder.verify(solrServerMock).add(solrInputDocumentMock);
  }

  @Test(expected = IndexException.class)
  public void testAddWhenSolrServerThrowsASolrServerException() throws SolrServerException, IOException, IndexException {
    testAddWhenSolrServerThrowsAnException(SolrServerException.class);
  }

  @Test(expected = IndexException.class)
  public void testAddWhenSolrServerThrowsAnIOException() throws SolrServerException, IOException, IndexException {
    testAddWhenSolrServerThrowsAnException(IOException.class);
  }

  private void testAddWhenSolrServerThrowsAnException(Class<? extends Exception> exceptionToThrow) throws SolrServerException, IOException, IndexException {
    // when
    when(documentCreatorMock.create(variationsToAdd)).thenReturn(solrInputDocumentMock);
    doThrow(exceptionToThrow).when(solrServerMock).add(solrInputDocumentMock);

    // action
    try {
      instance.add(variationsToAdd);
    } finally {
      // verify
      verifyTheIndexIsUpdated();
    }
  }

  @Test
  public void testAddWithNullVariationList() throws IndexException {
    // action
    instance.add(null);

    // verify
    verifyZeroInteractions(documentCreatorMock, solrServerMock);
  }

  @Test
  public void testAddWithEmptyVariationList() throws IndexException {
    // action
    instance.add(Lists.<DomainEntity> newArrayList());

    // verify
    verifyZeroInteractions(documentCreatorMock, solrServerMock);
  }

  @Test
  public void testUpdate() throws IndexException, SolrServerException, IOException {
    // when
    when(documentCreatorMock.create(variationsToAdd)).thenReturn(solrInputDocumentMock);

    // action
    instance.update(variationsToAdd);

    // verify
    verifyTheIndexIsUpdated();
  }

  @Test
  public void testUpdateWithNullVariationList() throws IndexException {
    // action
    instance.update(null);

    // verify
    verifyZeroInteractions(documentCreatorMock, solrServerMock);
  }

  @Test
  public void testUpdateWithEmptyVariationList() throws IndexException {
    // action
    instance.update(Lists.<DomainEntity> newArrayList());

    // verify
    verifyZeroInteractions(documentCreatorMock, solrServerMock);
  }

  @Test
  public void testDelete() throws SolrServerException, IOException, IndexException {
    String id = "ID";
    // action
    instance.deleteById(id);

    // verify
    verify(solrServerMock).deleteById(id);
  }

  @Test(expected = IndexException.class)
  public void testDeleteSolrServerThrowsSolrServerException() throws SolrServerException, IOException, IndexException {
    testDeleteSolrServerThrowsException(SolrServerException.class);
  }

  @Test(expected = IndexException.class)
  public void testDeleteSolrServerThrowsIOException() throws SolrServerException, IOException, IndexException {
    testDeleteSolrServerThrowsException(IOException.class);
  }

  private void testDeleteSolrServerThrowsException(Class<? extends Exception> exceptionToThrow) throws SolrServerException, IOException, IndexException {
    String id = "ID";

    // when
    doThrow(exceptionToThrow).when(solrServerMock).deleteById(id);

    try {
      // action
      instance.deleteById(id);
    } finally {
      // verify
      verify(solrServerMock).deleteById(id);
    }
  }

  @Test
  public void testDeleteByIdWithNullVariationList() throws IndexException {
    String test = null;

    // action
    instance.deleteById(test);

    // verify
    verifyZeroInteractions(documentCreatorMock, solrServerMock);
  }

  @Test
  public void testDeleteMultipleItems() throws SolrServerException, IOException, IndexException {
    // setup
    List<String> ids = Lists.newArrayList("id1", "id2", "id3");

    // action
    instance.deleteById(ids);

    // verify
    verify(solrServerMock).deleteById(ids);
  }

  @Test(expected = IndexException.class)
  public void testDeleteMultipleItemsSolrServerThrowsIOException() throws SolrServerException, IOException, IndexException {
    testDeleteMultipleItemsSolrServerThrowsException(IOException.class);
  }

  @Test(expected = IndexException.class)
  public void testDeleteMultipleItemsSolrServerThrowsSolrServerException() throws SolrServerException, IOException, IndexException {
    testDeleteMultipleItemsSolrServerThrowsException(SolrServerException.class);
  }

  private void testDeleteMultipleItemsSolrServerThrowsException(Class<? extends Exception> exceptionToThrow) throws SolrServerException, IOException, IndexException {
    List<String> ids = Lists.newArrayList("id1", "id2", "id3");

    // when
    doThrow(exceptionToThrow).when(solrServerMock).deleteById(ids);

    try {
      // action
      instance.deleteById(ids);
    } finally {
      // verify
      verify(solrServerMock).deleteById(ids);
    }
  }

  @Test
  public void testDeleteMultipleItemsByIdWithNullVariationList() throws IndexException {
    List<String> nullList = null;

    // action
    instance.deleteById(nullList);

    // verify
    verifyZeroInteractions(documentCreatorMock, solrServerMock);
  }

  @Test
  public void testDeleteMultipleByIdWithEmptyList() throws IndexException {
    // action
    final ArrayList<String> emptyList = Lists.<String> newArrayList();
    instance.deleteById(emptyList);

    // verify
    verifyZeroInteractions(documentCreatorMock, solrServerMock);
  }

  @Test
  public void testClear() throws SolrServerException, IOException, IndexException {
    // action
    instance.clear();

    // verify
    InOrder inOrder = inOrder(solrServerMock);
    inOrder.verify(solrServerMock).deleteByQuery("*:*");
    inOrder.verify(solrServerMock).commit();
  }

  @Test(expected = IndexException.class)
  public void testClearDeleteByQueryThrowsSolrServerException() throws SolrServerException, IOException, IndexException {
    testClearDeleteByQueryThrowsException(SolrServerException.class);
  }

  @Test(expected = IndexException.class)
  public void testClearDeleteByQueryThrowsIOException() throws SolrServerException, IOException, IndexException {
    testClearDeleteByQueryThrowsException(IOException.class);
  }

  private void testClearDeleteByQueryThrowsException(Class<? extends Exception> exception) throws SolrServerException, IOException, IndexException {
    //when
    doThrow(exception).when(solrServerMock).deleteByQuery("*:*");

    try {
      // action
      instance.clear();
    } finally {
      // verify
      verify(solrServerMock).deleteByQuery("*:*");
      verifyNoMoreInteractions(solrServerMock);
    }
  }

  @Test(expected = IndexException.class)
  public void testClearCommitThrowsAnSolrServerException() throws SolrServerException, IOException, IndexException {
    testClearCommitThrowsAnException(SolrServerException.class);
  }

  @Test(expected = IndexException.class)
  public void testClearCommitThrowsAnIOException() throws SolrServerException, IOException, IndexException {
    testClearCommitThrowsAnException(IOException.class);
  }

  private void testClearCommitThrowsAnException(Class<? extends Exception> exception) throws SolrServerException, IOException, IndexException {
    //when
    doThrow(exception).when(solrServerMock).commit();

    try {
      // action
      instance.clear();
    } finally {
      // verify
      verify(solrServerMock).deleteByQuery("*:*");
      verify(solrServerMock).commit();
    }
  }

  @Test
  public void testGetCount() throws SolrServerException, IndexException {
    // setup
    QueryResponse queryResponseMock = mock(QueryResponse.class);
    SolrDocumentList resultsMock = mock(SolrDocumentList.class);
    long numFound = 42l;

    // when
    when(solrServerMock.search(SolrIndex.COUNT_QUERY)).thenReturn(queryResponseMock);
    when(queryResponseMock.getResults()).thenReturn(resultsMock);
    when(resultsMock.getNumFound()).thenReturn(numFound);

    // action
    long actualNumFound = instance.getCount();

    // verify
    InOrder inOrder = inOrder(solrServerMock, resultsMock);
    inOrder.verify(solrServerMock).search(SolrIndex.COUNT_QUERY);
    inOrder.verify(resultsMock).getNumFound();

    assertThat(actualNumFound, equalTo(numFound));
  }

  @Test(expected = IndexException.class)
  public void testGetCountSolrServerThrowsSolrException() throws SolrServerException, IndexException {
    // when
    doThrow(SolrServerException.class).when(solrServerMock).search(SolrIndex.COUNT_QUERY);

    try {
      // action
      instance.getCount();
    } finally {
      verify(solrServerMock).search(SolrIndex.COUNT_QUERY);
    }
  }

  @Test
  public void testCommit() throws SolrServerException, IOException, IndexException {
    // action
    instance.commit();

    // verify
    verify(solrServerMock).commit();
  }

  @Test(expected = IndexException.class)
  public void testCommitSolrServerMockThrowsAnSolrServerException() throws SolrServerException, IOException, IndexException {
    testCommitSolrServerThrowsAnException(SolrServerException.class);
  }

  @Test(expected = IndexException.class)
  public void testCommitSolrServerMockThrowsAnIOException() throws SolrServerException, IOException, IndexException {
    testCommitSolrServerThrowsAnException(IOException.class);
  }

  private void testCommitSolrServerThrowsAnException(Class<? extends Exception> exceptionToBeThrown) throws SolrServerException, IOException, IndexException {
    // when
    doThrow(exceptionToBeThrown).when(solrServerMock).commit();

    try {
      // action
      instance.commit();
    } finally {
      // verify
      verify(solrServerMock).commit();
    }
  }

  @Test
  public void testClose() throws SolrServerException, IOException, IndexException {
    // action
    instance.close();

    // verify
    verify(solrServerMock).commit();
    verify(solrServerMock).shutdown();
  }

  @Test(expected = IndexException.class)
  public void testCloseCommitThrowsSolrServerException() throws SolrServerException, IOException, IndexException {
    testCloseCommitThrowsAnException(SolrServerException.class);
  }

  @Test(expected = IndexException.class)
  public void testCloseCommitThrowsIOException() throws SolrServerException, IOException, IndexException {
    testCloseCommitThrowsAnException(IOException.class);
  }

  private void testCloseCommitThrowsAnException(Class<? extends Exception> exceptionToBeThrown) throws SolrServerException, IOException, IndexException {
    // when
    doThrow(exceptionToBeThrown).when(solrServerMock).commit();

    try {
      // action
      instance.close();
    } finally {
      // verify
      verify(solrServerMock).commit();
      verify(solrServerMock).shutdown();
    }
  }

  @Test(expected = IndexException.class)
  public void testCloseShutdownThrowsSolrServerException() throws SolrServerException, IOException, IndexException {
    testCloseShutdownThrowsAnException(SolrServerException.class);
  }

  @Test(expected = IndexException.class)
  public void testCloseShutdownThrowsIOException() throws SolrServerException, IOException, IndexException {
    testCloseShutdownThrowsAnException(IOException.class);
  }

  private void testCloseShutdownThrowsAnException(Class<? extends Exception> exceptionToBeThrown) throws SolrServerException, IOException, IndexException {
    // when
    doThrow(exceptionToBeThrown).when(solrServerMock).shutdown();

    try {
      // action
      instance.close();
    } finally {
      // verify
      verify(solrServerMock).commit();
      verify(solrServerMock).shutdown();
    }
  }

  @Test
  public void testSearch() throws NoSuchFieldInIndexException, FacetedSearchException, SearchException, SearchValidationException {
    // setup
    DefaultFacetedSearchParameters searchParameters = new DefaultFacetedSearchParameters();
    FacetedSearchResult facetedSearchResult = new FacetedSearchResult();

    // when
    when(facetedSearchLibraryMock.search(searchParameters)).thenReturn(facetedSearchResult);

    // action
    FacetedSearchResult actualSearchResult = instance.search(searchParameters);

    // verify
    verify(facetedSearchLibraryMock).search(searchParameters);

    assertThat(actualSearchResult, is(facetedSearchResult));
  }

  @Test(expected = SearchValidationException.class)
  public void testSearchFacetedSearchLibraryThrowsNoSuchFieldInIndexException() throws NoSuchFieldInIndexException, FacetedSearchException, SearchException, SearchValidationException {
    testSeachFacetedSearchLibraryThrowsAnException(NoSuchFieldInIndexException.class);
  }

  @Test(expected = SearchException.class)
  public void testSearchFacetedSearchLibraryThrowsFacetedSearchException() throws NoSuchFieldInIndexException, FacetedSearchException, SearchException, SearchValidationException {
    testSeachFacetedSearchLibraryThrowsAnException(FacetedSearchException.class);
  }

  private void testSeachFacetedSearchLibraryThrowsAnException(Class<? extends Exception> exceptionToThrow) throws NoSuchFieldInIndexException, FacetedSearchException, SearchException,
      SearchValidationException {
    // setup
    DefaultFacetedSearchParameters searchParameters = new DefaultFacetedSearchParameters();

    // when
    doThrow(exceptionToThrow).when(facetedSearchLibraryMock).search(searchParameters);

    try {
      // action
      instance.search(searchParameters);
    } finally {
      // verify
      verify(facetedSearchLibraryMock).search(searchParameters);
    }

  }
}
