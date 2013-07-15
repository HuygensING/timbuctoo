package nl.knaw.huygens.repository.search;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import nl.knaw.huygens.repository.index.LocalSolrServer;
import nl.knaw.huygens.repository.model.Document;
import nl.knaw.huygens.repository.model.Person;
import nl.knaw.huygens.repository.model.SearchResult;
import nl.knaw.huygens.solr.FacetCount;
import nl.knaw.huygens.solr.FacetCount.Option;
import nl.knaw.huygens.solr.FacetParameter;
import nl.knaw.huygens.solr.FacetedSearchParameters;

import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.FacetField.Count;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrException;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;

public class SearchManagerTest {
  private static final Class<Person> TYPE = Person.class;
  private static final String ID_FIELD_NAME = "id";
  private SearchManager instance;
  private LocalSolrServer solrInstance;
  private static final String TYPE_STRING = "person";
  private static final String SEARCH_TERM = "term";
  private static final String EXPECTED_TERM = String.format("facet_t_name:(%s)", SEARCH_TERM);

  @Before
  public void setUp() {
    solrInstance = mock(LocalSolrServer.class);
    instance = new SearchManager(solrInstance);
  }

  @Test
  public void testSearchOneResult() throws SolrServerException {
    List<String> documentIds = Lists.newArrayList("id1");
    List<String> facetFieldNames = Lists.newArrayList("facet_s_birthDate");
    int numberOfFacetValues = 1;

    testSearch(TYPE, documentIds, SEARCH_TERM, TYPE_STRING, facetFieldNames, numberOfFacetValues, Lists.<FacetParameter> newArrayList(), EXPECTED_TERM);
  }

  @Test
  public void testSearchMultipleResults() throws SolrServerException {
    List<String> documentIds = Lists.newArrayList("id1", "id2", "id3", "id4");
    List<String> facetFieldNames = Lists.newArrayList("facet_s_birthDate");
    int numberOfFacetValues = 1;

    testSearch(TYPE, documentIds, SEARCH_TERM, TYPE_STRING, facetFieldNames, numberOfFacetValues, Lists.<FacetParameter> newArrayList(), EXPECTED_TERM);
  }

  @Test
  public void testSearchNoResults() throws SolrServerException {
    List<String> documentIds = Lists.newArrayList();
    List<String> facetFieldNames = Lists.newArrayList("facet_s_birthDate");
    int numberOfFacetValues = 1;
    testSearch(TYPE, documentIds, SEARCH_TERM, TYPE_STRING, facetFieldNames, numberOfFacetValues, Lists.<FacetParameter> newArrayList(), EXPECTED_TERM);
  }

  @Test
  public void testSearchWithOneFacetOneValue() throws SolrServerException {
    List<String> documentIds = Lists.newArrayList("id1");
    List<String> facetFieldNames = Lists.newArrayList("facet_s_birthDate");
    int numberOfFacetValues = 1;

    List<FacetParameter> facetParameters = Lists.newArrayList(createFacetParam("param", "value"));

    String expectedTerm = String.format("+facet_t_name:(%s) +param:(value)", SEARCH_TERM);

    testSearch(TYPE, documentIds, SEARCH_TERM, TYPE_STRING, facetFieldNames, numberOfFacetValues, facetParameters, expectedTerm);
  }

  @Test
  public void testSearchWithOneFacetMultipleValues() throws SolrServerException {
    List<String> documentIds = Lists.newArrayList("id1");
    List<String> facetFieldNames = Lists.newArrayList("facet_s_birthDate");
    int numberOfFacetValues = 1;

    List<FacetParameter> facetParameters = Lists.newArrayList(createFacetParam("param", "value", "value1"));

    String expectedTerm = String.format("+facet_t_name:(%s) +param:(value value1)", SEARCH_TERM);

    testSearch(TYPE, documentIds, SEARCH_TERM, TYPE_STRING, facetFieldNames, numberOfFacetValues, facetParameters, expectedTerm);

  }

  @Test
  public void testSearchWithMultipleFacetsOneValue() throws SolrServerException {
    List<String> documentIds = Lists.newArrayList("id1");
    List<String> facetFieldNames = Lists.newArrayList("facet_s_birthDate");
    int numberOfFacetValues = 1;

    List<FacetParameter> facetParameters = Lists.newArrayList(createFacetParam("param", "value"), createFacetParam("param1", "values"));

    String expectedTerm = String.format("+facet_t_name:(%s) +param:(value) +param1:(values)", SEARCH_TERM);

    testSearch(TYPE, documentIds, SEARCH_TERM, TYPE_STRING, facetFieldNames, numberOfFacetValues, facetParameters, expectedTerm);
  }

  @Test
  public void testSearchWithMultipleFacetsMultipleValues() throws SolrServerException {
    List<String> documentIds = Lists.newArrayList("id1");
    List<String> facetFieldNames = Lists.newArrayList("facet_s_birthDate");
    int numberOfFacetValues = 1;

    List<FacetParameter> facetParameters = Lists.newArrayList(createFacetParam("param", "value", "value1"), createFacetParam("param1", "value1", "value2"));

    String expectedTerm = String.format("+facet_t_name:(%s) +param:(value value1) +param1:(value1 value2)", SEARCH_TERM);

    testSearch(TYPE, documentIds, SEARCH_TERM, TYPE_STRING, facetFieldNames, numberOfFacetValues, facetParameters, expectedTerm);

  }

  private void testSearch(Class<? extends Document> type, List<String> documentIds, String searchTerm, String typeString, List<String> facetNames, int numberOfFacetValues,
      List<FacetParameter> facetParameters, String expectedTerm) throws SolrServerException {
    FacetedSearchParameters searchParameters = new FacetedSearchParameters();
    searchParameters.setTerm(searchTerm);
    searchParameters.setTypeString(typeString);
    searchParameters.setFacetValues(facetParameters);

    SolrDocumentList docs = createSolrDocumentList(documentIds);
    List<FacetField> facetFields = createFacetFieldList(facetNames, numberOfFacetValues);
    setUpQueryResponse(docs, facetFields);

    List<FacetCount> facets = createFacetCountList(facetNames, numberOfFacetValues);
    SearchResult expected = createExpectedResult(TYPE_STRING, documentIds, expectedTerm, facets);

    SearchResult actual = instance.search(type, TYPE_STRING, searchParameters);

    verifySearchResult(expected, actual);
  }

  @SuppressWarnings("unchecked")
  @Test(expected = SolrException.class)
  public void testSearchSolrException() throws SolrServerException {
    when(solrInstance.getQueryResponse(anyString(), any(Collection.class), anyString(), anyString())).thenThrow(SolrException.class);

    FacetedSearchParameters searchParameters = new FacetedSearchParameters();
    searchParameters.setTerm(SEARCH_TERM);
    searchParameters.setTypeString(TYPE_STRING);

    instance.search(Person.class, TYPE_STRING, searchParameters);

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

  private List<FacetCount> createFacetCountList(List<String> facetFieldNames, int numberOfFacetValues) {
    List<FacetCount> facetCounts = Lists.newArrayList();

    for (String facetName : facetFieldNames) {
      FacetCount facetCount = new FacetCount();
      facetCount.setName(facetName);
      facetCount.setTitle(facetName);
      for (int j = 0; j < numberOfFacetValues; j++) {
        facetCount.addOption(new Option().setName(facetName + " " + j).setCount(j));
      }
      facetCounts.add(facetCount);
    }

    return facetCounts;
  }

  private List<FacetField> createFacetFieldList(List<String> facetFieldNames, int numberOfCounts) {
    List<FacetField> facetFields = Lists.newArrayList();

    for (String facetFieldName : facetFieldNames) {
      List<Count> counts = Lists.newArrayList();
      for (int j = 0; j < numberOfCounts; j++) {
        counts.add(createCount(10, "" + j));
      }
      facetFields.add(createFacetField(facetFieldName, counts));
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

  private FacetParameter createFacetParam(String name, String... values) {
    FacetParameter param = new FacetParameter();
    param.setName(name);
    param.setValues(Lists.newArrayList(values));
    return param;
  }

}
