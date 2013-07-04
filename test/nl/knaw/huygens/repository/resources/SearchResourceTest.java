package nl.knaw.huygens.repository.resources;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.List;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

import nl.knaw.huygens.repository.config.DocTypeRegistry;
import nl.knaw.huygens.repository.managers.SearchManager;
import nl.knaw.huygens.repository.managers.StorageManager;
import nl.knaw.huygens.repository.model.Person;
import nl.knaw.huygens.repository.model.SearchResult;

import org.apache.solr.client.solrj.SolrServerException;
import org.junit.Ignore;
import org.junit.Test;

import com.google.common.collect.Lists;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.core.util.MultivaluedMapImpl;

public class SearchResourceTest extends WebServiceTestSetup {
  private static final String LOCATION_HEADER = "Location";
  private String typeString = "person";
  private String id = "QRY0000000001";

  @Test
  public void testPostSuccess() throws IOException, SolrServerException {

    SearchResult searchResult = createSearchResult();

    setupDocTypeRegistry();

    setupSearchManager(searchResult);

    MultivaluedMap<String, String> formData = new MultivaluedMapImpl();
    formData.add("type", typeString);
    formData.add("q", "facet_t_name:Huygens");
    formData.add("sort", "id");

    WebResource resource = super.resource();
    String expected = String.format("%ssearch/%s", resource.getURI().toString(), id);
    ClientResponse response = resource.path("search").type(MediaType.APPLICATION_FORM_URLENCODED_TYPE).post(ClientResponse.class, formData);
    String actual = response.getHeaders().getFirst(LOCATION_HEADER);

    StorageManager storageManager = injector.getInstance(StorageManager.class);
    verify(storageManager).addDocument(SearchResult.class, searchResult);

    assertEquals(ClientResponse.Status.CREATED, response.getClientResponseStatus());
    assertEquals(expected, actual);
  }

  @Test
  public void testPostSuccessWithoutSort() throws IOException, SolrServerException {
    SearchResult searchResult = createSearchResult();

    setupDocTypeRegistry();

    setupSearchManager(searchResult);

    MultivaluedMap<String, String> formData = new MultivaluedMapImpl();
    formData.add("type", typeString);
    formData.add("q", "facet_t_name:Huygens");

    WebResource resource = super.resource();
    String expected = String.format("%ssearch/%s", resource.getURI().toString(), id);
    ClientResponse response = resource.path("search").type(MediaType.APPLICATION_FORM_URLENCODED_TYPE).post(ClientResponse.class, formData);
    String actual = response.getHeaders().getFirst(LOCATION_HEADER);

    StorageManager storageManager = injector.getInstance(StorageManager.class);
    verify(storageManager).addDocument(SearchResult.class, searchResult);

    assertEquals(ClientResponse.Status.CREATED, response.getClientResponseStatus());
    assertEquals(expected, actual);
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testPostTypeUnknown() throws SolrServerException, IOException {
    StorageManager storageManager = injector.getInstance(StorageManager.class);
    SearchManager searchManager = injector.getInstance(SearchManager.class);

    MultivaluedMap<String, String> formData = new MultivaluedMapImpl();
    formData.add("type", "unknowntype");
    formData.add("q", "facet_t_name:Huygens");

    WebResource resource = super.resource();
    ClientResponse clientResponse = resource.path("search").type(MediaType.APPLICATION_FORM_URLENCODED_TYPE).post(ClientResponse.class, formData);

    verify(storageManager, never()).addDocument(any(Class.class), any(SearchResult.class));
    verify(searchManager, never()).search(anyString(), anyString(), anyString());

    assertEquals(ClientResponse.Status.NOT_FOUND, clientResponse.getClientResponseStatus());
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testPostTypeStringEmpty() throws IOException, SolrServerException {
    StorageManager storageManager = injector.getInstance(StorageManager.class);
    SearchManager searchManager = injector.getInstance(SearchManager.class);

    MultivaluedMap<String, String> formData = new MultivaluedMapImpl();
    formData.add("q", "facet_t_name:Huygens");

    WebResource resource = super.resource();
    ClientResponse clientResponse = resource.path("search").type(MediaType.APPLICATION_FORM_URLENCODED_TYPE).post(ClientResponse.class, formData);

    verify(storageManager, never()).addDocument(any(Class.class), any(SearchResult.class));
    verify(searchManager, never()).search(anyString(), anyString(), anyString());

    assertEquals(ClientResponse.Status.BAD_REQUEST, clientResponse.getClientResponseStatus());
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testPostQueryStringEmpty() throws IOException, SolrServerException {
    StorageManager storageManager = injector.getInstance(StorageManager.class);
    SearchManager searchManager = injector.getInstance(SearchManager.class);

    MultivaluedMap<String, String> formData = new MultivaluedMapImpl();
    formData.add("type", "unknowntype");

    WebResource resource = super.resource();
    ClientResponse clientResponse = resource.path("search").type(MediaType.APPLICATION_FORM_URLENCODED_TYPE).post(ClientResponse.class, formData);

    verify(storageManager, never()).addDocument(any(Class.class), any(SearchResult.class));
    verify(searchManager, never()).search(anyString(), anyString(), anyString());

    assertEquals(ClientResponse.Status.BAD_REQUEST, clientResponse.getClientResponseStatus());
  }

  @Test
  @Ignore("Facets not yet implemented")
  public void testPostFacetUnknown() {
    fail("Yet to be implemented");
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testGetSuccess() {
    List<String> idList = Lists.newArrayList();
    StorageManager storageManager = injector.getInstance(StorageManager.class);

    for (int i = 0; i < 100; i++) {
      String personId = "" + i;
      Person person = new Person();
      person.setId(personId);

      idList.add(personId);
      when(storageManager.getDocument(Person.class, personId)).thenReturn(person);
    }

    SearchResult result = mock(SearchResult.class);
    when(result.getId()).thenReturn(id);
    when(result.getSearchType()).thenReturn("person");
    when(result.getIds()).thenReturn(idList);
    when(storageManager.getDocument(SearchResult.class, id)).thenReturn(result);

    setupDocTypeRegistry();

    WebResource resource = super.resource();
    List<Person> actual = resource.path("search").path(id).type(MediaType.APPLICATION_JSON_TYPE).accept(MediaType.APPLICATION_JSON_TYPE).get(new GenericType<List<Person>>() {});

    assertEquals(10, actual.size());
  }

  public void testGetSuccessWithStartAndRows() {
    List<String> idList = Lists.newArrayList();
    StorageManager storageManager = injector.getInstance(StorageManager.class);

    for (int i = 0; i < 100; i++) {
      String personId = "" + i;
      Person person = new Person();
      person.setId(personId);

      idList.add(personId);
      when(storageManager.getDocument(Person.class, personId)).thenReturn(person);
    }

    SearchResult result = mock(SearchResult.class);
    when(result.getId()).thenReturn(id);
    when(result.getSearchType()).thenReturn("person");
    when(result.getIds()).thenReturn(idList);
    when(storageManager.getDocument(SearchResult.class, id)).thenReturn(result);

    setupDocTypeRegistry();

    MultivaluedMap<String, String> queryParameters = new MultivaluedMapImpl();
    queryParameters.add("start", "10");
    queryParameters.add("rows", "100");

    WebResource resource = super.resource();
    List<Person> actual = resource.path("search").path(id).type(MediaType.APPLICATION_JSON_TYPE).accept(MediaType.APPLICATION_JSON_TYPE).get(new GenericType<List<Person>>() {});

    assertEquals(90, actual.size());
  }

  @Test
  public void testGetNoResults() {
    StorageManager storageManager = injector.getInstance(StorageManager.class);

    when(storageManager.getDocument(SearchResult.class, id)).thenReturn(null);

    setupDocTypeRegistry();

    WebResource resource = super.resource();
    ClientResponse response = resource.path("search").path(id).type(MediaType.APPLICATION_JSON_TYPE).accept(MediaType.APPLICATION_JSON_TYPE).get(ClientResponse.class);

    assertEquals(ClientResponse.Status.NOT_FOUND, response.getClientResponseStatus());
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
    when(storageManager.getDocument(SearchResult.class, "unknown")).thenReturn(null);

    WebResource resource = super.resource();
    ClientResponse response = resource.path("search").path("unknown").type(MediaType.APPLICATION_JSON_TYPE).accept(MediaType.APPLICATION_JSON_TYPE).get(ClientResponse.class);

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

    DocTypeRegistry docTypeRegistry = injector.getInstance(DocTypeRegistry.class);
    when(docTypeRegistry.getClassFromWebServiceTypeString(unknownType)).thenReturn(null);

    WebResource resource = super.resource();
    ClientResponse response = resource.path("search").path(id).type(MediaType.APPLICATION_JSON_TYPE).accept(MediaType.APPLICATION_JSON_TYPE).get(ClientResponse.class);

    assertEquals(ClientResponse.Status.INTERNAL_SERVER_ERROR, response.getClientResponseStatus());
  }

  private void setupSearchManager(SearchResult searchResult) throws SolrServerException {
    SearchManager searchManager = injector.getInstance(SearchManager.class);
    when(searchManager.search(anyString(), anyString(), anyString())).thenReturn(searchResult);
  }

  private void setupDocTypeRegistry() {
    DocTypeRegistry registry = injector.getInstance(DocTypeRegistry.class);
    doReturn(Person.class).when(registry).getClassFromWebServiceTypeString(typeString);
  }

  private SearchResult createSearchResult() {
    SearchResult searchResult = mock(SearchResult.class);
    when(searchResult.getId()).thenReturn(id);
    return searchResult;
  }

}
