package nl.knaw.huygens.timbuctoo.rest.resources;

/*
 * #%L
 * Timbuctoo REST api
 * =======
 * Copyright (C) 2012 - 2014 Huygens ING
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

import static nl.knaw.huygens.timbuctoo.rest.util.CustomHeaders.VRE_ID_KEY;
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

import nl.knaw.huygens.solr.SearchParameters;
import nl.knaw.huygens.timbuctoo.Repository;
import nl.knaw.huygens.timbuctoo.config.Configuration;
import nl.knaw.huygens.timbuctoo.facet.FacetCount;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.Person;
import nl.knaw.huygens.timbuctoo.model.SearchResult;
import nl.knaw.huygens.timbuctoo.search.NoSuchFacetException;
import nl.knaw.huygens.timbuctoo.search.SearchManager;
import nl.knaw.huygens.timbuctoo.vre.VRE;
import nl.knaw.huygens.timbuctoo.vre.VREManager;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.ClientResponse.Status;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.core.util.MultivaluedMapImpl;

public class SearchResourceTest extends WebServiceTestSetup {

  private static final Set<String> SORTABLE_FIELDS = Sets.newHashSet("test1", "test");
  private static final String SCOPE_ID = "base";
  private static final String TERM = "dynamic_t_name:Huygens";
  private static final String LOCATION_HEADER = "Location";
  private static final String TYPE_STRING = "person";
  private static final String ID = "QURY0000000001";

  private VREManager vreManager;
  private SearchManager searchManager;

  @Before
  public void setupSearchManager() {
    searchManager = injector.getInstance(SearchManager.class);
    when(searchManager.findSortableFields(Matchers.<Class<? extends DomainEntity>> any())).thenReturn(SORTABLE_FIELDS);
  }

  private void setSearchResult(SearchResult searchResult) throws Exception {
    when(searchManager.search(any(VRE.class), Matchers.<Class<? extends DomainEntity>> any(), any(SearchParameters.class))).thenReturn(searchResult);
  }

  private void setupPublicUrl(String url) {
    when(injector.getInstance(Configuration.class).getSetting("public_url")).thenReturn(url);
  }

  private void setUpVREManager(boolean isTypeInScope, boolean isVREKnown) {
    vreManager = injector.getInstance(VREManager.class);

    if (isVREKnown) {
      VRE vre = mock(VRE.class);
      when(vre.getName()).thenReturn(VRE_ID);
      when(vre.getScopeId()).thenReturn(SCOPE_ID);
      when(vre.inScope(Mockito.<Class<? extends DomainEntity>> any())).thenReturn(isTypeInScope);

      when(vreManager.getVREById(anyString())).thenReturn(vre);
      when(vreManager.getDefaultVRE()).thenReturn(vre);
    } else {
      when(vreManager.getVREById(anyString())).thenReturn(null);
    }
  }

  private WebResource.Builder getResourceBuilder() {
    return resource().path("search").type(MediaType.APPLICATION_JSON);
  }

  @Test
  public void testPostSuccess() throws Exception {
    setUpVREManager(true, true);

    SearchParameters params = new SearchParameters().setTypeString(TYPE_STRING).setSort(ID).setTerm(TERM);
    SearchResult searchResult = mock(SearchResult.class);;
    setSearchResult(searchResult);
    when(repository.addSystemEntity(SearchResult.class, searchResult)).thenReturn(ID);

    WebResource resource = super.resource();
    String expected = String.format("%ssearch/%s", resource.getURI().toString(), ID);
    ClientResponse response = resource.path("search").type(MediaType.APPLICATION_JSON).header(VRE_ID_KEY, VRE_ID).post(ClientResponse.class, params);
    String actual = response.getHeaders().getFirst(LOCATION_HEADER);

    assertEquals(Status.CREATED, response.getClientResponseStatus());
    assertEquals(expected, actual);
    verify(vreManager).getVREById(anyString());
  }

  @Test
  public void testPostSuccessWithoutSort() throws Exception {
    setUpVREManager(true, true);

    SearchParameters params = new SearchParameters().setTypeString(TYPE_STRING).setTerm(TERM);
    SearchResult searchResult = mock(SearchResult.class);
    setSearchResult(searchResult);
    when(repository.addSystemEntity(SearchResult.class, searchResult)).thenReturn(ID);

    WebResource resource = super.resource();
    String expected = String.format("%ssearch/%s", resource.getURI().toString(), ID);
    ClientResponse response = resource.path("search").type(MediaType.APPLICATION_JSON).header(VRE_ID_KEY, VRE_ID).post(ClientResponse.class, params);
    String actual = response.getHeaders().getFirst(LOCATION_HEADER);

    assertEquals(Status.CREATED, response.getClientResponseStatus());
    assertEquals(expected, actual);
    verify(vreManager).getVREById(anyString());
  }

  @Test
  public void testPostTypeUnknown() throws Exception {
    setUpVREManager(true, true);

    SearchParameters params = new SearchParameters().setTypeString("unknown").setTerm(TERM);

    ClientResponse response = getResourceBuilder().header(VRE_ID_KEY, VRE_ID).post(ClientResponse.class, params);

    assertEquals(Status.BAD_REQUEST, response.getClientResponseStatus());
    verify(vreManager).getVREById(anyString());
    verify(repository, never()).addSystemEntity(Matchers.<Class<SearchResult>> any(), any(SearchResult.class));
    verify(searchManager, never()).search(any(VRE.class), Matchers.<Class<? extends DomainEntity>> any(), any(SearchParameters.class));
  }

  @Test
  public void testPostTypeStringNull() throws Exception {
    setUpVREManager(true, true);

    SearchParameters params =  new SearchParameters().setTerm(TERM);

    ClientResponse response = getResourceBuilder().header(VRE_ID_KEY, VRE_ID).post(ClientResponse.class, params);

    assertEquals(Status.BAD_REQUEST, response.getClientResponseStatus());
    verify(vreManager).getVREById(anyString());
    verify(repository, never()).addSystemEntity(Matchers.<Class<SearchResult>> any(), any(SearchResult.class));
    verify(searchManager, never()).search(any(VRE.class), Matchers.<Class<? extends DomainEntity>> any(), any(SearchParameters.class));
  }

  @Test
  public void testPostTypeOutOfScope() throws Exception {
    setUpVREManager(false, true);

    SearchParameters params = new SearchParameters().setTypeString(TYPE_STRING).setTerm(TERM);

    ClientResponse response = getResourceBuilder().header(VRE_ID_KEY, VRE_ID).post(ClientResponse.class, params);

    assertEquals(Status.BAD_REQUEST, response.getClientResponseStatus());
    verify(vreManager).getVREById(anyString());
    verify(repository, never()).addSystemEntity(Matchers.<Class<SearchResult>> any(), any(SearchResult.class));
    verify(searchManager, never()).search(any(VRE.class), Matchers.<Class<? extends DomainEntity>> any(), any(SearchParameters.class));
  }

  @Test
  public void testPostVREIdNull() throws Exception {
    setUpVREManager(true, true);

    SearchParameters params = new SearchParameters().setTypeString(TYPE_STRING).setTerm(TERM);
    SearchResult searchResult = mock(SearchResult.class);
    setSearchResult(searchResult);
    when(repository.addSystemEntity(SearchResult.class, searchResult)).thenReturn(ID);

    WebResource resource = super.resource();
    String expected = String.format("%ssearch/%s", resource.getURI().toString(), ID);
    ClientResponse response = resource.path("search").type(MediaType.APPLICATION_JSON).post(ClientResponse.class, params);
    String actual = response.getHeaders().getFirst(LOCATION_HEADER);

    assertEquals(Status.CREATED, response.getClientResponseStatus());
    assertEquals(expected, actual);
    verify(vreManager).getDefaultVRE();
  }

  @Test
  public void testPostVREUnknown() throws Exception {
    setUpVREManager(true, false);

    SearchParameters params = new SearchParameters().setTypeString(TYPE_STRING).setTerm(TERM);

    ClientResponse response = getResourceBuilder().header(VRE_ID_KEY, VRE_ID).post(ClientResponse.class, params);

    assertEquals(Status.NOT_FOUND, response.getClientResponseStatus());
    verify(repository, never()).addSystemEntity(Matchers.<Class<SearchResult>> any(), any(SearchResult.class));
    verify(searchManager, never()).search(any(VRE.class), Matchers.<Class<? extends DomainEntity>> any(), any(SearchParameters.class));
  }

  @Test
  public void testPostQueryStringNull() throws Exception {
    setUpVREManager(true, true);

    SearchParameters params = new SearchParameters().setTerm(TERM);

    ClientResponse response = getResourceBuilder().header(VRE_ID_KEY, VRE_ID).post(ClientResponse.class, params);

    assertEquals(Status.BAD_REQUEST, response.getClientResponseStatus());
    verify(repository, never()).addSystemEntity(Matchers.<Class<SearchResult>> any(), any(SearchResult.class));
    verify(searchManager, never()).search(any(VRE.class), Matchers.<Class<? extends DomainEntity>> any(), any(SearchParameters.class));
  }

  @Test
  public void testPostUnknownFacets() throws Exception {
    setUpVREManager(true, true);

    SearchParameters params = new SearchParameters().setTypeString(TYPE_STRING).setTerm(TERM);
    doThrow(NoSuchFacetException.class).when(searchManager).search(any(VRE.class), Matchers.<Class<? extends DomainEntity>> any(), any(SearchParameters.class));

    ClientResponse response = getResourceBuilder().header(VRE_ID_KEY, VRE_ID).post(ClientResponse.class, params);

    assertEquals(Status.BAD_REQUEST, response.getClientResponseStatus());
    verify(repository, never()).addSystemEntity(Matchers.<Class<SearchResult>> any(), any(SearchResult.class));
  }

  @Test
  public void testPostSearchManagerThrowsAnException() throws Exception {
    setUpVREManager(true, true);

    SearchParameters params = new SearchParameters().setTypeString(TYPE_STRING).setTerm(TERM);
    doThrow(Exception.class).when(searchManager).search(any(VRE.class), Matchers.<Class<? extends DomainEntity>> any(), any(SearchParameters.class));

    ClientResponse response = getResourceBuilder().header(VRE_ID_KEY, VRE_ID).post(ClientResponse.class, params);

    assertEquals(Status.INTERNAL_SERVER_ERROR, response.getClientResponseStatus());
    verify(repository, never()).addSystemEntity(Matchers.<Class<SearchResult>> any(), any(SearchResult.class));
  }

  @Test
  public void testPostStorageManagerThrowsAnException() throws Exception {
    setUpVREManager(true, true);

    SearchParameters params = new SearchParameters().setTypeString(TYPE_STRING).setTerm(TERM);
    SearchResult searchResult = mock(SearchResult.class);
    setSearchResult(searchResult);
    doThrow(IOException.class).when(repository).addSystemEntity(Matchers.<Class<SearchResult>> any(), any(SearchResult.class));

    ClientResponse response = getResourceBuilder().header(VRE_ID_KEY, VRE_ID).post(ClientResponse.class, params);

    assertEquals(Status.INTERNAL_SERVER_ERROR, response.getClientResponseStatus());
    verify(vreManager).getVREById(anyString());
  }

  @Ignore("redefine test when new searchmanager is ready")
  @Test
  public void testGetSuccess() {
    List<String> idList = Lists.newArrayList();
    List<Person> personList = Lists.newArrayList();

    int startIndex = 0;
    int numberOfRows = 10;
    createSearchResultOf100Persons(repository, idList, personList);

    List<FacetCount> facets = createFacets();
    setUpSearchResult(idList, repository, facets);

    WebResource resource = super.resource();
    setupPublicUrl(resource.getURI().toString());

    String nextUri = String.format("%ssearch/%s?start=10&rows=10", resource.getURI(), ID);
    int returnedRows = 10;
    Map<String, Object> expected = createExpectedResult(idList, personList, facets, startIndex, numberOfRows, SORTABLE_FIELDS, returnedRows, nextUri, null);

    Map<String, Object> actual = resource.path("search").path(ID).type(MediaType.APPLICATION_JSON_TYPE).accept(MediaType.APPLICATION_JSON_TYPE).get(new GenericType<Map<String, Object>>() {});

    compareResults(expected, actual);
  }

  @Ignore("redefine test when new searchmanager is ready")
  @Test
  public void testGetSuccessWithStartAndRows() {
    List<String> idList = Lists.newArrayList();
    List<Person> personList = Lists.newArrayList();

    int startIndex = 20;
    int numberOfRows = 20;
    createSearchResultOf100Persons(repository, idList, personList);

    List<FacetCount> facets = createFacets();

    setUpSearchResult(idList, repository, facets);

    MultivaluedMap<String, String> queryParameters = new MultivaluedMapImpl();
    queryParameters.add("start", "20");
    queryParameters.add("rows", "20");

    WebResource resource = super.resource();
    setupPublicUrl(resource.getURI().toString());

    String prevUri = String.format("%ssearch/%s?start=0&rows=20", resource.getURI(), ID);
    String nextUri = String.format("%ssearch/%s?start=40&rows=20", resource.getURI(), ID);
    int returnedRows = 20;
    Map<String, Object> expected = createExpectedResult(idList, personList, facets, startIndex, numberOfRows, SORTABLE_FIELDS, returnedRows, nextUri, prevUri);

    Map<String, Object> actual = resource.path("search").path(ID).queryParams(queryParameters).type(MediaType.APPLICATION_JSON_TYPE).accept(MediaType.APPLICATION_JSON_TYPE)
        .get(new GenericType<Map<String, Object>>() {});

    compareResults(expected, actual);
  }

  @Ignore("redefine test when new searchmanager is ready")
  @Test
  public void testGetSuccessWithStartAndRowsMoreThanMax() {
    List<String> idList = Lists.newArrayList();
    List<Person> personList = Lists.newArrayList();

    int startIndex = 10;
    int numberOfRows = 100;
    createSearchResultOf100Persons(repository, idList, personList);

    List<FacetCount> facets = createFacets();

    setUpSearchResult(idList, repository, facets);

    MultivaluedMap<String, String> queryParameters = new MultivaluedMapImpl();
    queryParameters.add("start", "10");
    queryParameters.add("rows", "100");

    WebResource resource = super.resource();
    setupPublicUrl(resource.getURI().toString());

    String prevUri = String.format("%ssearch/%s?start=0&rows=100", resource.getURI(), ID);
    int returnedRows = 90;
    Map<String, Object> expected = createExpectedResult(idList, personList, facets, startIndex, numberOfRows, SORTABLE_FIELDS, returnedRows, null, prevUri);

    Map<String, Object> actual = resource.path("search").path(ID).queryParams(queryParameters).type(MediaType.APPLICATION_JSON_TYPE).accept(MediaType.APPLICATION_JSON_TYPE)
        .get(new GenericType<Map<String, Object>>() {});

    compareResults(expected, actual);
  }

  @Test
  public void testGetNoResults() {
    setUpSearchResult(null, repository, Lists.<FacetCount> newArrayList());

    Map<String, Object> expected = createExpectedResult(Lists.<String> newArrayList(), Lists.<Person> newArrayList(), Lists.<FacetCount> newArrayList(), 0, 0, SORTABLE_FIELDS, 0, null, null);

    WebResource resource = super.resource();
    Map<String, Object> actual = resource.path("search").path(ID).type(MediaType.APPLICATION_JSON_TYPE).accept(MediaType.APPLICATION_JSON_TYPE).get(new GenericType<Map<String, Object>>() {});

    compareResults(expected, actual);
  }

  @Test
  public void testGetNoId() {
    WebResource resource = super.resource();
    ClientResponse response = resource.path("search").type(MediaType.APPLICATION_JSON_TYPE).accept(MediaType.APPLICATION_JSON_TYPE).get(ClientResponse.class);

    assertEquals(Status.METHOD_NOT_ALLOWED, response.getClientResponseStatus());
  }

  @Test
  public void testGetUnknownId() {
    when(repository.getEntity(SearchResult.class, ID)).thenReturn(null);

    WebResource resource = super.resource();
    ClientResponse response = resource.path("search").path(ID).type(MediaType.APPLICATION_JSON_TYPE).accept(MediaType.APPLICATION_JSON_TYPE).get(ClientResponse.class);

    assertEquals(Status.NOT_FOUND, response.getClientResponseStatus());
  }

  @Test
  public void testGetSearchTypeUnknown() {
    SearchResult searchResult = mock(SearchResult.class);
    searchResult.setId(ID);
    String unknownType = "unknown";
    searchResult.setSearchType(unknownType);

    when(repository.getEntity(SearchResult.class, ID)).thenReturn(searchResult);

    WebResource resource = super.resource();
    ClientResponse response = resource.path("search").path(ID).type(MediaType.APPLICATION_JSON_TYPE).accept(MediaType.APPLICATION_JSON_TYPE).get(ClientResponse.class);

    assertEquals(Status.BAD_REQUEST, response.getClientResponseStatus());
  }

  private Map<String, Object> createExpectedResult(List<String> idList, List<Person> personList, List<FacetCount> facets, int start, int rows, Set<String> sortableFields, int returnedRows,
      String next, String prev) {
    Map<String, Object> result = Maps.newHashMap();
    int lastIndex = (start + rows) >= personList.size() ? personList.size() : (start + rows);

    result.put("results", personList.subList(start, Math.max(lastIndex, 0)));
    result.put("start", start); // start index of the results
    result.put("rows", returnedRows); // number of results in the response
    result.put("term", TERM); // search query
    result.put("facets", facets); // all applying facets
    result.put("numFound", idList.size()); // all found results
    result.put("ids", idList.subList(start, rows)); //only the ids of the objects in the in response.
    result.put("sortableFields", sortableFields);
    result.put("_next", next);
    result.put("_prev", prev);

    return result;
  }

  private List<FacetCount> createFacets() {
    List<FacetCount> facets = Lists.newArrayList();
    FacetCount.Option option1 = new FacetCount.Option().setCount(1).setName("17-5-1900");
    FacetCount.Option option2 = new FacetCount.Option().setCount(2).setName("21-6");
    FacetCount.Option option3 = new FacetCount.Option().setCount(97).setName("1780");
    FacetCount facet = new FacetCount().setName("dynamic_s_birthDate").setTitle("birthdate");
    facet.addOption(option1);
    facet.addOption(option2);
    facet.addOption(option3);
    facets.add(facet);
    return facets;
  }

  private void createSearchResultOf100Persons(Repository repository, final List<String> idList, final List<Person> personList) {
    for (int i = 0; i < 100; i++) {
      String personId = "" + i;
      Person person = new Person();
      person.setId(personId);
      personList.add(person);
      idList.add(personId);
      when(repository.getEntity(Person.class, personId)).thenReturn(person);
    }
  }

  private void setUpSearchResult(List<String> idList, Repository repository, List<FacetCount> facets) {
    SearchResult result = mock(SearchResult.class);
    when(result.getTerm()).thenReturn(TERM);
    when(result.getId()).thenReturn(ID);
    when(result.getSearchType()).thenReturn("person");
    when(result.getIds()).thenReturn(idList);
    //    when(result.getFacets()).thenReturn(facets);
    when(repository.getEntity(SearchResult.class, ID)).thenReturn(result);
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
