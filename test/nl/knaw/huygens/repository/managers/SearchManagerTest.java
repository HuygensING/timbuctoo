package nl.knaw.huygens.repository.managers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import nl.knaw.huygens.repository.index.LocalSolrServer;
import nl.knaw.huygens.repository.model.SearchResult;
import nl.knaw.huygens.solr.FacetCount;
import nl.knaw.huygens.solr.FacetCount.Option;
import nl.knaw.huygens.solr.FacetedSearchParameters;

import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.FacetField.Count;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrException;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.google.common.collect.Lists;

public class SearchManagerTest {
  private static final String ID_FIELD_NAME = "id";
  private SearchManager instance;
  private LocalSolrServer solrInstance;
  private static final String TYPE_STRING = "person";
  private static final String SEARCH_TERM = "term";

  @Before
  public void setUp() {
    solrInstance = mock(LocalSolrServer.class);
    instance = new SearchManager(solrInstance);
  }

  @Test
  public void searchOneResult() throws SolrServerException {
    List<String> documentIds = Lists.newArrayList("id1");
    int numberOfFacets = 1;
    int numberOfFacetValues = 1;

    testSearch(documentIds, numberOfFacets, numberOfFacetValues);
  }

  @Test
  public void searchMultipleResults() throws SolrServerException {
    List<String> documentIds = Lists.newArrayList("id1", "id2", "id3", "id4");
    int numberOfFacets = 1;
    int numberOfFacetValues = 1;

    testSearch(documentIds, numberOfFacets, numberOfFacetValues);
  }

  @Test
  public void searchNoResults() throws SolrServerException {
    List<String> documentIds = Lists.newArrayList();
    int numberOfFacets = 1;
    int numberOfFacetValues = 1;
    testSearch(documentIds, numberOfFacets, numberOfFacetValues);
  }

  @Test
  @Ignore("Facet support not yet implemented")
  public void searchWithFacetsOneResult() {
    fail("Yet to be implemented");
  }

  @Test
  @Ignore("Facet support not yet implemented")
  public void searchWithFacetsMultipleResults() {
    fail("Yet to be implemented");
  }

  private void testSearch(List<String> documentIds, int numberOfFacets, int numberOfFacetValues) throws SolrServerException {
    FacetedSearchParameters searchParameters = new FacetedSearchParameters();
    searchParameters.setTerm(SEARCH_TERM);
    searchParameters.setTypeString(TYPE_STRING);

    SolrDocumentList docs = createSolrDocumentList(documentIds);
    List<FacetField> facetFields = createFacetFieldList(numberOfFacets, numberOfFacetValues);
    setUpQueryResponse(docs, facetFields);

    List<FacetCount> facets = createFacetCountList(numberOfFacets, numberOfFacetValues);
    SearchResult expected = createExpectedResult(TYPE_STRING, documentIds, SEARCH_TERM, facets);

    SearchResult actual = instance.search(TYPE_STRING, searchParameters);

    verifySearchResult(expected, actual);
  }

  @SuppressWarnings("unchecked")
  @Test(expected = SolrException.class)
  public void searchSolrException() throws SolrServerException {
    when(solrInstance.getQueryResponse(anyString(), any(Collection.class), anyString(), anyString())).thenThrow(SolrException.class);

    FacetedSearchParameters searchParameters = new FacetedSearchParameters();
    searchParameters.setTerm(SEARCH_TERM);
    searchParameters.setTypeString(TYPE_STRING);

    instance.search(TYPE_STRING, searchParameters);

  }

  private void verifySearchResult(SearchResult expected, SearchResult actual) {
    assertEquals(expected.getIds().size(), actual.getIds().size());
    assertEquals(expected.getFacets().size(), actual.getFacets().size());
    assertEquals(expected.getFacets().size(), actual.getFacets().size());
    for (int i = 0; i < expected.getFacets().size(); i++) {
      assertEquals(expected.getFacets().get(i).getOptions().size(), actual.getFacets().get(i).getOptions().size());
    }

    assertEquals(expected.getTerm(), actual.getTerm());
    assertEquals(expected.getSearchType(), actual.getSearchType());
  }

  private FacetField createFacetField(String facetName, List<Count> counts) {
    FacetField facetField = mock(FacetField.class);
    when(facetField.getName()).thenReturn(facetName);
    when(facetField.getValues()).thenReturn(counts);
    return facetField;
  }

  private Count createCount(long facetValueCount, String facetValueName) {
    Count count = mock(Count.class);
    when(count.getCount()).thenReturn(facetValueCount);
    when(count.getName()).thenReturn(facetValueName);
    return count;
  }

  private SearchResult createExpectedResult(String typeString, List<String> ids, String searchTerm, List<FacetCount> facets) {
    SearchResult expected = new SearchResult();
    expected.setIds(ids);
    expected.setTerm(searchTerm);
    expected.setSearchType(typeString);
    expected.setFacets(Lists.newArrayList(facets));
    return expected;
  }

  private List<FacetCount> createFacetCountList(int numberOfFacets, int numberOfFacetValues) {
    List<FacetCount> facetCounts = Lists.newArrayList();

    for (int i = 0; i < numberOfFacets; i++) {
      FacetCount facetCount = new FacetCount();
      facetCount.setName("" + i);
      facetCount.setTitle("" + i);
      for (int j = 0; j < numberOfFacetValues; j++) {
        facetCount.addOption(new Option().setName(i + " " + j).setCount(i + j));
      }
      facetCounts.add(facetCount);
    }

    return facetCounts;
  }

  private List<FacetField> createFacetFieldList(int numberOfFacetFields, int numberOfCounts) {
    List<FacetField> facetFields = Lists.newArrayList();

    for (int i = 0; i < numberOfFacetFields; i++) {
      List<Count> counts = Lists.newArrayList();
      for (int j = 0; j < numberOfCounts; j++) {
        counts.add(createCount(i + j, "" + j));
      }
      facetFields.add(createFacetField("" + i, counts));
    }

    return facetFields;
  }

  @SuppressWarnings("unchecked")
  private void setUpQueryResponse(SolrDocumentList docs, List<FacetField> facetFields) throws SolrServerException {
    QueryResponse response = mock(QueryResponse.class);
    when(response.getResults()).thenReturn(docs);
    when(response.getFacetFields()).thenReturn(facetFields);

    when(solrInstance.getQueryResponse(anyString(), any(List.class), anyString(), anyString())).thenReturn(response);
  }

  private SolrDocumentList createSolrDocumentList(List<String> documentIds) {
    SolrDocumentList docs = mock(SolrDocumentList.class);

    List<SolrDocument> solrDocuments = createSolrDocuments(documentIds);

    Iterator<SolrDocument> iterator = solrDocuments.iterator();

    when(docs.iterator()).thenReturn(iterator);

    return docs;
  }

  private List<SolrDocument> createSolrDocuments(List<String> documentIds) {
    List<SolrDocument> documents = Lists.newArrayList();
    for (String documentId : documentIds) {
      SolrDocument doc = mock(SolrDocument.class);
      when(doc.getFieldValue(ID_FIELD_NAME)).thenReturn(documentId);
      documents.add(doc);
    }
    return documents;
  }

}
