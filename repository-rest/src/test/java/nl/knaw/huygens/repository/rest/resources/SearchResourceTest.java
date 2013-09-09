package nl.knaw.huygens.repository.rest.resources;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

import nl.knaw.huygens.repository.facet.annotations.FacetCount;
import nl.knaw.huygens.repository.model.Document;
import nl.knaw.huygens.repository.model.Person;
import nl.knaw.huygens.repository.model.SearchResult;
import nl.knaw.huygens.repository.search.FacetDoesNotExistException;
import nl.knaw.huygens.repository.search.SearchManager;
import nl.knaw.huygens.repository.storage.StorageManager;
import nl.knaw.huygens.solr.FacetedSearchParameters;

import org.apache.solr.client.solrj.SolrServerException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.core.util.MultivaluedMapImpl;

public class SearchResourceTest extends WebServiceTestSetup {
  private static final Set<String> SORTABLE_FIELDS = Sets.newHashSet("test1", "test");
  private static final String TERM = "facet_t_name:Huygens";
  private static final String LOCATION_HEADER = "Location";
  private String typeString = "person";
  private String id = "QRY0000000001";

  @Before
  public void setUpSortableFields() {
    SearchManager searchManager = injector.getInstance(SearchManager.class);
    when(searchManager.findSortableFields(Matchers.<Class<? extends Document>> any())).thenReturn(SORTABLE_FIELDS);
  }

  @Test
  public void testPostSuccess() throws IOException, SolrServerException, FacetDoesNotExistException {
    SearchResult searchResult = createPostSearchResult();
    setupSearchManager(searchResult);
    FacetedSearchParameters searchParameters = createSearchParameters(typeString, id, TERM);

    WebResource resource = super.resource();
    String expected = String.format("%ssearch/%s", resource.getURI().toString(), id);
    ClientResponse response = resource.path("search").type(MediaType.APPLICATION_JSON).post(ClientResponse.class, searchParameters);
    String actual = response.getHeaders().getFirst(LOCATION_HEADER);

    assertEquals(ClientResponse.Status.CREATED, response.getClientResponseStatus());
    assertEquals(expected, actual);

    StorageManager storageManager = injector.getInstance(StorageManager.class);
    verify(storageManager).addDocument(SearchResult.class, searchResult);
  }

  @Test
  public void testPostSuccessWithoutSort() throws IOException, SolrServerException, FacetDoesNotExistException {
    SearchResult searchResult = createPostSearchResult();
    setupSearchManager(searchResult);
    FacetedSearchParameters searchParameters = createSearchParameters(typeString, null, TERM);

    WebResource resource = super.resource();
    String expected = String.format("%ssearch/%s", resource.getURI().toString(), id);
    ClientResponse response = resource.path("search").type(MediaType.APPLICATION_JSON).post(ClientResponse.class, searchParameters);
    String actual = response.getHeaders().getFirst(LOCATION_HEADER);

    StorageManager storageManager = injector.getInstance(StorageManager.class);
    verify(storageManager).addDocument(SearchResult.class, searchResult);

    assertEquals(ClientResponse.Status.CREATED, response.getClientResponseStatus());
    assertEquals(expected, actual);
  }

  @Test
  public void testPostTypeUnknown() throws SolrServerException, IOException, FacetDoesNotExistException {
    StorageManager storageManager = injector.getInstance(StorageManager.class);
    SearchManager searchManager = injector.getInstance(SearchManager.class);

    FacetedSearchParameters searchParameters = createSearchParameters("unknownType", null, TERM);

    WebResource resource = super.resource();
    ClientResponse clientResponse = resource.path("search").type(MediaType.APPLICATION_JSON).post(ClientResponse.class, searchParameters);

    verify(storageManager, never()).addDocument(Matchers.<Class<SearchResult>> any(), any(SearchResult.class));
    verify(searchManager, never()).search(Matchers.<Class<? extends Document>> any(), anyString(), any(FacetedSearchParameters.class));

    assertEquals(ClientResponse.Status.NOT_FOUND, clientResponse.getClientResponseStatus());
  }

  @Test
  public void testPostTypeStringNull() throws IOException, SolrServerException, FacetDoesNotExistException {
    StorageManager storageManager = injector.getInstance(StorageManager.class);
    SearchManager searchManager = injector.getInstance(SearchManager.class);

    FacetedSearchParameters searchParameters = createSearchParameters(null, null, TERM);

    WebResource resource = super.resource();
    ClientResponse clientResponse = resource.path("search").type(MediaType.APPLICATION_JSON).post(ClientResponse.class, searchParameters);

    verify(storageManager, never()).addDocument(Matchers.<Class<SearchResult>> any(), any(SearchResult.class));
    verify(searchManager, never()).search(Matchers.<Class<? extends Document>> any(), anyString(), any(FacetedSearchParameters.class));

    assertEquals(ClientResponse.Status.BAD_REQUEST, clientResponse.getClientResponseStatus());
  }

  @Test
  public void testPostQueryStringNull() throws IOException, SolrServerException, FacetDoesNotExistException {
    StorageManager storageManager = injector.getInstance(StorageManager.class);
    SearchManager searchManager = injector.getInstance(SearchManager.class);

    FacetedSearchParameters searchParameters = createSearchParameters(typeString, null, null);

    WebResource resource = super.resource();
    ClientResponse clientResponse = resource.path("search").type(MediaType.APPLICATION_JSON).post(ClientResponse.class, searchParameters);

    verify(storageManager, never()).addDocument(Matchers.<Class<SearchResult>> any(), any(SearchResult.class));
    verify(searchManager, never()).search(Matchers.<Class<? extends Document>> any(), anyString(), any(FacetedSearchParameters.class));

    assertEquals(ClientResponse.Status.BAD_REQUEST, clientResponse.getClientResponseStatus());
  }

  @Test
  public void testPostUnknownFacets() throws SolrServerException, FacetDoesNotExistException, IOException {
    SearchManager searchManager = injector.getInstance(SearchManager.class);
    doThrow(FacetDoesNotExistException.class).when(searchManager).search(Matchers.<Class<? extends Document>> any(), anyString(), any(FacetedSearchParameters.class));

    StorageManager storageManager = injector.getInstance(StorageManager.class);

    FacetedSearchParameters searchParameters = createSearchParameters(typeString, null, TERM);

    WebResource resource = super.resource();
    ClientResponse clientResponse = resource.path("search").type(MediaType.APPLICATION_JSON).post(ClientResponse.class, searchParameters);

    verify(storageManager, never()).addDocument(Matchers.<Class<SearchResult>> any(), any(SearchResult.class));
    assertEquals(ClientResponse.Status.BAD_REQUEST, clientResponse.getClientResponseStatus());

  }

  @Test
  public void testPostSearchManagerThrowsAnException() throws IOException, SolrServerException, FacetDoesNotExistException {
    SearchManager searchManager = injector.getInstance(SearchManager.class);
    doThrow(Exception.class).when(searchManager).search(Matchers.<Class<? extends Document>> any(), anyString(), any(FacetedSearchParameters.class));

    StorageManager storageManager = injector.getInstance(StorageManager.class);

    FacetedSearchParameters searchParameters = createSearchParameters(typeString, null, TERM);

    WebResource resource = super.resource();
    ClientResponse clientResponse = resource.path("search").type(MediaType.APPLICATION_JSON).post(ClientResponse.class, searchParameters);

    verify(storageManager, never()).addDocument(Matchers.<Class<SearchResult>> any(), any(SearchResult.class));
    assertEquals(ClientResponse.Status.INTERNAL_SERVER_ERROR, clientResponse.getClientResponseStatus());
  }

  @Test
  public void testPostStorageManagerThrowsAnException() throws IOException, SolrServerException, FacetDoesNotExistException {
    SearchResult searchResult = createPostSearchResult();
    setupSearchManager(searchResult);

    StorageManager storageManager = injector.getInstance(StorageManager.class);
    doThrow(IOException.class).when(storageManager).addDocument(Matchers.<Class<SearchResult>> any(), any(SearchResult.class));

    FacetedSearchParameters searchParameters = createSearchParameters(typeString, null, TERM);

    WebResource resource = super.resource();
    ClientResponse clientResponse = resource.path("search").type(MediaType.APPLICATION_JSON).post(ClientResponse.class, searchParameters);

    assertEquals(ClientResponse.Status.INTERNAL_SERVER_ERROR, clientResponse.getClientResponseStatus());
  }

  @Test
  public void testGetSuccess() {
    List<String> idList = Lists.newArrayList();
    List<Person> personList = Lists.newArrayList();
    StorageManager storageManager = injector.getInstance(StorageManager.class);

    createSearchResult(idList, personList, storageManager);

    List<FacetCount> facets = createFacets();

    setUpSearchResult(idList, storageManager, facets);

    WebResource resource = super.resource();

    String nextUri = String.format("%ssearch/%s?start=10&rows=10", resource.getURI(), id);
    Map<String, Object> expected = createExpectedResult(idList, personList, facets, 0, 10, SORTABLE_FIELDS, 10, nextUri, null);

    Map<String, Object> actual = resource.path("search").path(id).type(MediaType.APPLICATION_JSON_TYPE).accept(MediaType.APPLICATION_JSON_TYPE).get(new GenericType<Map<String, Object>>() {});

    compareResults(expected, actual);
  }

  @Test
  public void testGetSuccessWithStartAndRows() {
    List<String> idList = Lists.newArrayList();
    List<Person> personList = Lists.newArrayList();
    StorageManager storageManager = injector.getInstance(StorageManager.class);

    createSearchResult(idList, personList, storageManager);

    List<FacetCount> facets = createFacets();

    setUpSearchResult(idList, storageManager, facets);

    MultivaluedMap<String, String> queryParameters = new MultivaluedMapImpl();
    queryParameters.add("start", "20");
    queryParameters.add("rows", "20");

    WebResource resource = super.resource();

    String prevUri = String.format("%ssearch/%s?start=0&rows=20", resource.getURI(), id);
    String nextUri = String.format("%ssearch/%s?start=40&rows=20", resource.getURI(), id);
    Map<String, Object> expected = createExpectedResult(idList, personList, facets, 20, 20, SORTABLE_FIELDS, 20, nextUri, prevUri);

    Map<String, Object> actual = resource.path("search").path(id).queryParams(queryParameters).type(MediaType.APPLICATION_JSON_TYPE).accept(MediaType.APPLICATION_JSON_TYPE)
        .get(new GenericType<Map<String, Object>>() {});

    compareResults(expected, actual);
  }

  @Test
  public void testGetSuccessWithStartAndRowsMoreThanMax() {
    List<String> idList = Lists.newArrayList();
    List<Person> personList = Lists.newArrayList();
    StorageManager storageManager = injector.getInstance(StorageManager.class);

    createSearchResult(idList, personList, storageManager);

    List<FacetCount> facets = createFacets();

    setUpSearchResult(idList, storageManager, facets);

    MultivaluedMap<String, String> queryParameters = new MultivaluedMapImpl();
    queryParameters.add("start", "10");
    queryParameters.add("rows", "100");

    WebResource resource = super.resource();

    String prevUri = String.format("%ssearch/%s?start=0&rows=100", resource.getURI(), id);
    Map<String, Object> expected = createExpectedResult(idList, personList, facets, 10, 100, SORTABLE_FIELDS, 90, null, prevUri);

    Map<String, Object> actual = resource.path("search").path(id).queryParams(queryParameters).type(MediaType.APPLICATION_JSON_TYPE).accept(MediaType.APPLICATION_JSON_TYPE)
        .get(new GenericType<Map<String, Object>>() {});

    compareResults(expected, actual);
  }

  @Test
  public void testGetNoResults() {
    StorageManager storageManager = injector.getInstance(StorageManager.class);

    setUpSearchResult(Lists.<String> newArrayList(), storageManager, Lists.<FacetCount> newArrayList());

    Map<String, Object> expected = createExpectedResult(Lists.<String> newArrayList(), Lists.<Person> newArrayList(), Lists.<FacetCount> newArrayList(), 0, 0, SORTABLE_FIELDS, 0, null, null);

    WebResource resource = super.resource();
    Map<String, Object> actual = resource.path("search").path(id).type(MediaType.APPLICATION_JSON_TYPE).accept(MediaType.APPLICATION_JSON_TYPE).get(new GenericType<Map<String, Object>>() {});

    compareResults(expected, actual);
  }

  @Test
  public void testGetNoId() {
    WebResource resource = super.resource();
    ClientResponse response = resource.path("search").type(MediaType.APPLICATION_JSON_TYPE).accept(MediaType.APPLICATION_JSON_TYPE).get(ClientResponse.class);

    assertEquals(ClientResponse.Status.METHOD_NOT_ALLOWED, response.getClientResponseStatus());
  }

  @Test
  public void testGetUnknownId() {
    StorageManager storageManager = injector.getInstance(StorageManager.class);
    when(storageManager.getDocument(SearchResult.class, id)).thenReturn(null);

    WebResource resource = super.resource();
    ClientResponse response = resource.path("search").path(id).type(MediaType.APPLICATION_JSON_TYPE).accept(MediaType.APPLICATION_JSON_TYPE).get(ClientResponse.class);

    assertEquals(ClientResponse.Status.NOT_FOUND, response.getClientResponseStatus());
  }

  @Test
  public void testGetSearchTypeUnknown() {
    SearchResult searchResult = mock(SearchResult.class);
    searchResult.setId(id);
    String unknownType = "unknown";
    searchResult.setSearchType(unknownType);

    StorageManager storageManager = injector.getInstance(StorageManager.class);
    when(storageManager.getDocument(SearchResult.class, id)).thenReturn(searchResult);

    WebResource resource = super.resource();
    ClientResponse response = resource.path("search").path(id).type(MediaType.APPLICATION_JSON_TYPE).accept(MediaType.APPLICATION_JSON_TYPE).get(ClientResponse.class);

    assertEquals(ClientResponse.Status.BAD_REQUEST, response.getClientResponseStatus());
  }

  private void setupSearchManager(SearchResult searchResult) throws SolrServerException, FacetDoesNotExistException {
    SearchManager searchManager = injector.getInstance(SearchManager.class);
    when(searchManager.search(Matchers.<Class<? extends Document>> any(), anyString(), any(FacetedSearchParameters.class))).thenReturn(searchResult);
  }

  private SearchResult createPostSearchResult() {
    SearchResult searchResult = mock(SearchResult.class);
    when(searchResult.getId()).thenReturn(id);
    return searchResult;
  }

  private FacetedSearchParameters createSearchParameters(String typeString, String sort, String term) {
    FacetedSearchParameters searchParameters = new FacetedSearchParameters();
    searchParameters.setTypeString(typeString);
    searchParameters.setSort(sort);
    searchParameters.setTerm(term);
    return searchParameters;
  }

  private Map<String, Object> createExpectedResult(List<String> idList, List<Person> personList, List<FacetCount> facets, int start, int rows, Set<String> sortableFields, int returnedRows,
      String next, String prev) {
    Map<String, Object> expectedResult = Maps.newHashMap();
    int lastIndex = (start + rows) >= personList.size() ? personList.size() : (start + rows);

    expectedResult.put("results", personList.subList(start, Math.max(lastIndex, 0)));
    expectedResult.put("start", start); // start index of the results
    expectedResult.put("rows", returnedRows); // number of results in the response
    expectedResult.put("term", TERM); // search query
    expectedResult.put("facets", facets); // all applying facets
    expectedResult.put("numFound", idList.size()); // all found results
    expectedResult.put("ids", idList.subList(start, rows)); //only the ids of the objects in the in response.
    expectedResult.put("sortableFields", sortableFields);
    expectedResult.put("_next", next);
    expectedResult.put("_prev", prev);

    return expectedResult;
  }

  private List<FacetCount> createFacets() {
    List<FacetCount> facets = Lists.newArrayList();
    FacetCount.Option option1 = new FacetCount.Option().setCount(1).setName("17-5-1900");
    FacetCount.Option option2 = new FacetCount.Option().setCount(2).setName("21-6");
    FacetCount.Option option3 = new FacetCount.Option().setCount(97).setName("1780");
    FacetCount facet = new FacetCount().setName("facet_s_birthDate").setTitle("birthdate");
    facet.addOption(option1);
    facet.addOption(option2);
    facet.addOption(option3);
    facets.add(facet);
    return facets;
  }

  private void createSearchResult(final List<String> idList, final List<Person> personList, final StorageManager storageManager) {
    for (int i = 0; i < 100; i++) {
      String personId = "" + i;
      Person person = new Person();
      person.setId(personId);

      personList.add(person);

      idList.add(personId);
      when(storageManager.getDocument(Person.class, personId)).thenReturn(person);
    }
  }

  private void setUpSearchResult(List<String> idList, StorageManager storageManager, List<FacetCount> facets) {
    SearchResult result = mock(SearchResult.class);
    when(result.getTerm()).thenReturn(TERM);
    when(result.getId()).thenReturn(id);
    when(result.getSearchType()).thenReturn("person");
    when(result.getIds()).thenReturn(idList);
    when(result.getFacets()).thenReturn(facets);
    when(storageManager.getDocument(SearchResult.class, id)).thenReturn(result);
  }

  @SuppressWarnings("unchecked")
  private void compareResults(Map<String, Object> expected, Map<String, Object> actual) {
    assertEquals(((List<Person>) expected.get("results")).size(), ((List<Person>) actual.get("results")).size());
    assertEquals(expected.get("term"), actual.get("term"));
    assertEquals(expected.get("numFound"), actual.get("numFound"));
    assertEquals(expected.get("start"), actual.get("start"));
    assertEquals(expected.get("rows"), actual.get("rows"));
    assertEquals(((List<FacetCount>) expected.get("facets")).size(), ((List<FacetCount>) actual.get("facets")).size());
    assertEquals(((Collection<String>) expected.get("sortableFields")).size(), ((Collection<String>) actual.get("sortableFields")).size());
    assertEquals(expected.get("_next"), actual.get("_next"));
    assertEquals(expected.get("_prev"), actual.get("_prev"));
  }
}
