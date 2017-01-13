package nl.knaw.huygens.timbuctoo.index.solr;

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
import nl.knaw.huygens.facetedsearch.FacetedSearchException;
import nl.knaw.huygens.facetedsearch.FacetedSearchLibrary;
import nl.knaw.huygens.facetedsearch.model.FacetedSearchResult;
import nl.knaw.huygens.facetedsearch.model.NoSuchFieldInIndexException;
import nl.knaw.huygens.facetedsearch.model.parameters.DefaultFacetedSearchParameters;
import nl.knaw.huygens.facetedsearch.model.parameters.FacetField;
import nl.knaw.huygens.facetedsearch.model.parameters.IndexDescription;
import nl.knaw.huygens.solr.AbstractSolrServer;
import nl.knaw.huygens.timbuctoo.index.IndexException;
import nl.knaw.huygens.timbuctoo.index.RawSearchUnavailableException;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.search.RawSearchResult;
import nl.knaw.huygens.timbuctoo.vre.SearchException;
import nl.knaw.huygens.timbuctoo.vre.SearchValidationException;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static nl.knaw.huygens.timbuctoo.index.solr.SolrQueryMatcher.likeSolrQuery;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@Ignore
public class SolrIndexTest {
  private static final String INDEX_NAME = "indexName";
  private static final String RAW_SEARCH_FIELD = "rawSearchField";
  private static final String QUERY = "query";
  private static final String MESSAGE = "Error on server";
  private static final short START = 0;
  private static final int ROWS = 10;
  public static final Map<String, Object> FILTERS = Maps.newHashMap();
  public static final String FILTER_VALUE = "filterValue";
  public static final String FILTER_NAME = "filterName";
  @Mock
  private List<? extends DomainEntity> variationsToAdd;
  private AbstractSolrServer solrServerMock;
  private SolrInputDocument solrInputDocumentMock;
  private SolrInputDocumentCreator documentCreatorMock;
  private FacetedSearchLibrary facetedSearchLibraryMock;
  private SolrIndex instance;
  private IndexDescription indexDescriptionMock;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    solrServerMock = mock(AbstractSolrServer.class);
    solrInputDocumentMock = mock(SolrInputDocument.class);
    documentCreatorMock = mock(SolrInputDocumentCreator.class);
    facetedSearchLibraryMock = mock(FacetedSearchLibrary.class);
    indexDescriptionMock = mock(IndexDescription.class);

    instance = new SolrIndex(INDEX_NAME, RAW_SEARCH_FIELD, indexDescriptionMock, documentCreatorMock, solrServerMock, facetedSearchLibraryMock);
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
  public void searchReturnsTheSearchResult() throws NoSuchFieldInIndexException, FacetedSearchException, SearchException, SearchValidationException {
    // setup
    DefaultFacetedSearchParameters searchParametersMock = mock(DefaultFacetedSearchParameters.class);
    FacetedSearchResult facetedSearchResult = new FacetedSearchResult();
    List<String> facetFields = Lists.newArrayList("test", "test2");

    // when
    when(searchParametersMock.getFacetFields()).thenReturn(facetFields);
    when(facetedSearchLibraryMock.search(searchParametersMock)).thenReturn(facetedSearchResult);

    // action
    FacetedSearchResult actualSearchResult = instance.search(searchParametersMock);

    // verify
    verify(facetedSearchLibraryMock).search(searchParametersMock);
    verify(searchParametersMock).getFacetFields();
    verifyNoMoreInteractions(searchParametersMock);

    assertThat(actualSearchResult, is(facetedSearchResult));
  }

  @Test
  public void searchAddsTheFacetFieldToReturnIfNoneAreDefined() throws NoSuchFieldInIndexException, FacetedSearchException, SearchException, SearchValidationException {
    // setup
    DefaultFacetedSearchParameters searchParametersMock = mock(DefaultFacetedSearchParameters.class);
    FacetedSearchResult facetedSearchResult = new FacetedSearchResult();
    List<FacetField> facetFields = Lists.newArrayList(new FacetField("test"), new FacetField("test2"));

    // when
    when(facetedSearchLibraryMock.search(searchParametersMock)).thenReturn(facetedSearchResult);
    when(indexDescriptionMock.getFacetFields()).thenReturn(facetFields);

    // action
    FacetedSearchResult actualSearchResult = instance.search(searchParametersMock);

    // verify
    verify(facetedSearchLibraryMock).search(searchParametersMock);
    verify(searchParametersMock).setFacetFields(facetFields);

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

  @SuppressWarnings("unchecked")
  @Test
  public void doRawSearchExecutesAQueryDirectlyOnTheSolrServerAndTranslatesItToAnIterableOfStringObjectMaps() throws SolrServerException, SearchException, RawSearchUnavailableException {
    // setup
    Map<String, Object> result1 = Maps.newHashMap();
    Map<String, Object> result2 = Maps.newHashMap();

    SolrQueryMatcher query = likeSolrQuery().withQuery(getSolrQuery(QUERY)).withStart(START).withRows(ROWS);
    setupQueryResponseForQueryWithResults(query, result1, result2);

    // action
    RawSearchResult searchResult = instance.doRawSearch(QUERY, START, ROWS, FILTERS);

    // verify
    assertThat(searchResult.getResults(), containsInAnyOrder(result1, result2));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void doRawSearchRemovesTheColonsFormTheQuery() throws SolrServerException, SearchException, RawSearchUnavailableException {
    // setup
    Map<String, Object> result1 = Maps.newHashMap();
    Map<String, Object> result2 = Maps.newHashMap();
    String otherQuery = "other:query";
    String cleanedUpOtherQuery = "other query";

    SolrQueryMatcher expectedQuery = likeSolrQuery().withQuery(getSolrQuery(cleanedUpOtherQuery)).withStart(START).withRows(ROWS);
    setupQueryResponseForQueryWithResults(expectedQuery, result1, result2);

    // action
    RawSearchResult searchResult = instance.doRawSearch(otherQuery, START, ROWS, FILTERS);

    // verify
    verify(solrServerMock).search(argThat(expectedQuery));
    assertThat(searchResult.getResults(), containsInAnyOrder(result1, result2));
  }

  private String getSolrQuery(String query) {
    return String.format("%s:%s", RAW_SEARCH_FIELD, query);
  }

  @SuppressWarnings("unchecked")
  @Test
  public void doRawSearchAddsTheAdditionalFiltersToTheQuery() throws SolrServerException, SearchException, RawSearchUnavailableException {
    // setup
    Map<String, Object> result1 = Maps.newHashMap();
    Map<String, Object> result2 = Maps.newHashMap();

    Map<String, Object> filters = Maps.newHashMap();
    filters.put(FILTER_NAME, FILTER_VALUE);
    SolrQueryMatcher expectedQuery = likeSolrQuery().withQuery(getSolrQueryWithAdditionalFilters(QUERY, filters)).withStart(START).withRows(ROWS);
    setupQueryResponseForQueryWithResults(expectedQuery, result1, result2);

    // action
    RawSearchResult searchResult = instance.doRawSearch(QUERY, START, ROWS, filters);

    // verify
    verify(solrServerMock).search(argThat(expectedQuery));
    assertThat(searchResult.getResults(), containsInAnyOrder(result1, result2));
  }

  private String getSolrQueryWithAdditionalFilters(String query, Map<String, Object> filters) {
    StringBuilder completeQuery = new StringBuilder(String.format("+(%s)",getSolrQuery(query)));
    for(Map.Entry<String, Object> filter: filters.entrySet()) {
      completeQuery.append(String.format(" +(%s:%s)", filter.getKey(), filter.getValue()));
    }


    return completeQuery.toString();
  }


  private void setupQueryResponseForQueryWithResults(SolrQueryMatcher query, Map<String, Object>... results) throws SolrServerException {
    QueryResponse queryResponse = mock(QueryResponse.class);
    SolrDocumentList solrDocuments = new SolrDocumentList();

    for (Map<String, Object> result : results) {
      solrDocuments.add(createDoc(result));
    }

    when(queryResponse.getResults()).thenReturn(solrDocuments);
    when(solrServerMock.search(argThat(query))).thenReturn(queryResponse);
  }

  private SolrDocument createDoc(Map<String, Object> result) {
    SolrDocument doc = mock(SolrDocument.class);
    when(doc.getFieldValueMap()).thenReturn(result);
    return doc;
  }

  @Test(expected = RawSearchUnavailableException.class)
  public void doRawSearchThrowsAnRawSearchUnavailableExceptionWhenNoRawSearchFieldIsConfigured() throws Exception {
    SolrIndex instance = new SolrIndex(INDEX_NAME, "", indexDescriptionMock, documentCreatorMock, solrServerMock, facetedSearchLibraryMock);

    instance.doRawSearch(QUERY, START, ROWS, FILTERS);
  }

  @Test(expected = SearchException.class)
  public void doRawSearchThrowsASearchExceptionWhenTheSolrServerThrowsASolrServerException() throws SolrServerException, SearchException, RawSearchUnavailableException {
    // setup
    when(solrServerMock.search(any(SolrQuery.class))).thenThrow(new SolrServerException(MESSAGE));

    // action
    instance.doRawSearch(getSolrQuery(QUERY), START, ROWS, FILTERS);
  }
}
