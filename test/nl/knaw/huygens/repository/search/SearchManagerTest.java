package nl.knaw.huygens.repository.search;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyCollectionOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import nl.knaw.huygens.repository.index.LocalSolrServer;
import nl.knaw.huygens.repository.model.Document;
import nl.knaw.huygens.repository.model.Person;
import nl.knaw.huygens.repository.model.SearchResult;
import nl.knaw.huygens.solr.FacetCount;
import nl.knaw.huygens.solr.FacetCount.Option;
import nl.knaw.huygens.solr.FacetInfo;
import nl.knaw.huygens.solr.FacetParameter;
import nl.knaw.huygens.solr.FacetType;
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
import org.mockito.Matchers;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class SearchManagerTest {
  private static final Class<Person> TYPE = Person.class;
  private static final String ID_FIELD_NAME = "id";
  private static final String TYPE_STRING = "person";
  private static final String SEARCH_TERM = "term";
  private static final String EXPECTED_TERM = String.format("facet_t_name:(%s)", SEARCH_TERM);

  private SearchManager instance;
  private LocalSolrServer solrInstance;
  private FacetFinder facetFinder;

  @Before
  public void setUp() {
    solrInstance = mock(LocalSolrServer.class);
    facetFinder = mock(FacetFinder.class);
    instance = new SearchManager(solrInstance, facetFinder);
  }

  @Test
  public void testSearchOneResult() throws SolrServerException, FacetDoesNotExistException {
    List<String> documentIds = Lists.newArrayList("id1");
    List<String> facetFieldNames = Lists.newArrayList("facet_s_birthDate");
    int numberOfFacetValues = 1;

    testSearch(TYPE, documentIds, SEARCH_TERM, TYPE_STRING, facetFieldNames, numberOfFacetValues, Lists.<FacetParameter> newArrayList(), EXPECTED_TERM);
  }

  @Test
  public void testSearchMultipleResults() throws SolrServerException, FacetDoesNotExistException {
    List<String> documentIds = Lists.newArrayList("id1", "id2", "id3", "id4");
    List<String> facetFieldNames = Lists.newArrayList("facet_s_birthDate");
    int numberOfFacetValues = 1;

    testSearch(TYPE, documentIds, SEARCH_TERM, TYPE_STRING, facetFieldNames, numberOfFacetValues, Lists.<FacetParameter> newArrayList(), EXPECTED_TERM);
  }

  @Test
  public void testSearchNoResults() throws SolrServerException, FacetDoesNotExistException {
    List<String> documentIds = Lists.newArrayList();
    List<String> facetFieldNames = Lists.newArrayList("facet_s_birthDate");
    int numberOfFacetValues = 1;
    testSearch(TYPE, documentIds, SEARCH_TERM, TYPE_STRING, facetFieldNames, numberOfFacetValues, Lists.<FacetParameter> newArrayList(), EXPECTED_TERM);
  }

  @Test
  public void testSearchWithOneFacetOneValue() throws SolrServerException, FacetDoesNotExistException {
    List<String> documentIds = Lists.newArrayList("id1");
    List<String> facetFieldNames = Lists.newArrayList("facet_s_birthDate");
    int numberOfFacetValues = 1;

    List<FacetParameter> facetParameters = Lists.newArrayList(createFacetParam("facet_s_birthDate", "value"));

    String expectedTerm = String.format("+facet_t_name:(%s) +facet_s_birthDate:(value)", SEARCH_TERM);

    testSearch(TYPE, documentIds, SEARCH_TERM, TYPE_STRING, facetFieldNames, numberOfFacetValues, facetParameters, expectedTerm);
  }

  @Test
  public void testSearchWithOneFacetMultipleValues() throws SolrServerException, FacetDoesNotExistException {
    List<String> documentIds = Lists.newArrayList("id1");
    List<String> facetFieldNames = Lists.newArrayList("facet_s_birthDate");
    int numberOfFacetValues = 1;

    List<FacetParameter> facetParameters = Lists.newArrayList(createFacetParam("facet_s_birthDate", "value", "value1"));

    String expectedTerm = String.format("+facet_t_name:(%s) +facet_s_birthDate:(value value1)", SEARCH_TERM);

    testSearch(TYPE, documentIds, SEARCH_TERM, TYPE_STRING, facetFieldNames, numberOfFacetValues, facetParameters, expectedTerm);

  }

  @Test
  public void testSearchWithMultipleFacetsOneValue() throws SolrServerException, FacetDoesNotExistException {
    List<String> documentIds = Lists.newArrayList("id1");
    List<String> facetFieldNames = Lists.newArrayList("facet_s_birthDate", "facet_s_deathDate");
    int numberOfFacetValues = 1;

    List<FacetParameter> facetParameters = Lists.newArrayList(createFacetParam("facet_s_birthDate", "value"), createFacetParam("facet_s_deathDate", "values"));

    String expectedTerm = String.format("+facet_t_name:(%s) +facet_s_birthDate:(value) +facet_s_deathDate:(values)", SEARCH_TERM);

    testSearch(TYPE, documentIds, SEARCH_TERM, TYPE_STRING, facetFieldNames, numberOfFacetValues, facetParameters, expectedTerm);
  }

  @Test
  public void testSearchWithMultipleFacetsMultipleValues() throws SolrServerException, FacetDoesNotExistException {
    List<String> documentIds = Lists.newArrayList("id1");
    List<String> facetFieldNames = Lists.newArrayList("facet_s_birthDate", "facet_s_deathDate");
    int numberOfFacetValues = 1;

    List<FacetParameter> facetParameters = Lists.newArrayList(createFacetParam("facet_s_birthDate", "value", "value1"), createFacetParam("facet_s_deathDate", "value1", "value2"));

    String expectedTerm = String.format("+facet_t_name:(%s) +facet_s_birthDate:(value value1) +facet_s_deathDate:(value1 value2)", SEARCH_TERM);

    testSearch(TYPE, documentIds, SEARCH_TERM, TYPE_STRING, facetFieldNames, numberOfFacetValues, facetParameters, expectedTerm);

  }

  private void testSearch(Class<? extends Document> type, List<String> documentIds, String searchTerm, String typeString, List<String> facetNames, int numberOfFacetValues,
      List<FacetParameter> facetParameters, String expectedTerm) throws SolrServerException, FacetDoesNotExistException {
    FacetedSearchParameters searchParameters = new FacetedSearchParameters();
    searchParameters.setTerm(searchTerm);
    searchParameters.setTypeString(typeString);
    searchParameters.setFacetValues(facetParameters);

    SolrDocumentList docs = createSolrDocumentList(documentIds);
    List<FacetField> facetFields = createFacetFieldList(facetNames, numberOfFacetValues);
    setUpQueryResponse(docs, facetFields);

    setupFacetFinder(facetNames);

    List<FacetCount> facets = createFacetCountList(facetNames, numberOfFacetValues);
    SearchResult expected = createExpectedResult(TYPE_STRING, documentIds, expectedTerm, facets);

    SearchResult actual = instance.search(type, TYPE_STRING, searchParameters);

    verifySearchResult(expected, actual);
  }

  private void setupFacetFinder(List<String> facetNames) {
    Map<String, FacetInfo> facetInfos = Maps.newHashMap();
    for (String facetName : facetNames) {
      facetInfos.put(facetName, new FacetInfo().setName(facetName).setTitle("test").setType(FacetType.LIST));
    }

    when(facetFinder.findFacets(Matchers.<Class<? extends Document>> any())).thenReturn(facetInfos);
  }

  @Test(expected = SolrException.class)
  public void testSearchSolrException() throws SolrServerException, FacetDoesNotExistException {
    doThrow(SolrException.class).when(solrInstance).getQueryResponse(anyString(), anyCollectionOf(String.class), anyString(), anyString());

    FacetedSearchParameters searchParameters = new FacetedSearchParameters();
    searchParameters.setTerm(SEARCH_TERM);
    searchParameters.setTypeString(TYPE_STRING);

    instance.search(Person.class, TYPE_STRING, searchParameters);
  }

  @Test(expected = FacetDoesNotExistException.class)
  public void testSearchFacetDoesNotExistException() throws SolrServerException, FacetDoesNotExistException {
    FacetedSearchParameters searchParameters = new FacetedSearchParameters();
    searchParameters.setTerm(SEARCH_TERM);
    searchParameters.setTypeString(TYPE_STRING);
    searchParameters.setFacetValues(Lists.newArrayList(new FacetParameter().setName("unknown")));

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
