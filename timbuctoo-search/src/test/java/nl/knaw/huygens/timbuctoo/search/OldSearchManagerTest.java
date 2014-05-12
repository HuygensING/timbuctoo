package nl.knaw.huygens.timbuctoo.search;

/*
 * #%L
 * Timbuctoo search
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import nl.knaw.huygens.solr.FacetParameter;
import nl.knaw.huygens.solr.SearchParameters;
import nl.knaw.huygens.timbuctoo.facet.FacetCount;
import nl.knaw.huygens.timbuctoo.facet.FacetCount.Option;
import nl.knaw.huygens.timbuctoo.index.IndexManager;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.Person;
import nl.knaw.huygens.timbuctoo.model.SearchResult;
import nl.knaw.huygens.timbuctoo.model.dcar.DCARPerson;
import nl.knaw.huygens.timbuctoo.search.model.ClassWithMupltipleFullTestSearchFields;
import nl.knaw.huygens.timbuctoo.vre.Scope;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.FacetField.Count;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Matchers;

import com.google.common.collect.Lists;

@Ignore("remove when the new search manager is implemented.")
@Deprecated
public class OldSearchManagerTest {

  private static final ArrayList<String> FULL_TEXT_SEARCH_NAMES = Lists.newArrayList("dynamic_t_name");
  private static final Class<Person> TYPE = Person.class;
  private static final String ID_FIELD_NAME = "id";
  private static final String TYPE_STRING = "person";
  private static final String SEARCH_TERM = "term";
  private static final String EXPECTED_TERM = String.format("(dynamic_t_name:%s)", SEARCH_TERM);

  private Scope scope;
  private OldSearchManager instance;
  private IndexManager indexManager;

  @Before
  public void setUp() {
    scope = mock(Scope.class);
    when(scope.getId()).thenReturn("scope");
    indexManager = mock(IndexManager.class);
    instance = new OldSearchManager(indexManager);
  }

  @Test
  public void testSearchOneResult() throws Exception {
    List<String> ids = Lists.newArrayList("id1");
    List<String> facetFieldNames = Lists.newArrayList("dynamic_s_birthDate");
    int numberOfFacetValues = 1;

    testSearch(TYPE, ids, SEARCH_TERM, TYPE_STRING, facetFieldNames, FULL_TEXT_SEARCH_NAMES, numberOfFacetValues, Lists.<FacetParameter> newArrayList(), EXPECTED_TERM);
  }

  @Test
  public void testSearchSubType() throws Exception {
    List<String> ids = Lists.newArrayList("id1");
    List<String> facetFieldNames = Lists.newArrayList("dynamic_s_birthDate");

    testSearch(DCARPerson.class, ids, SEARCH_TERM, "dcarperson", facetFieldNames, FULL_TEXT_SEARCH_NAMES, 1, Lists.<FacetParameter> newArrayList(), EXPECTED_TERM);
  }

  @Test
  public void testSearchMultipleResults() throws Exception {
    List<String> ids = Lists.newArrayList("id1", "id2", "id3", "id4");
    List<String> facetFieldNames = Lists.newArrayList("dynamic_s_birthDate");
    int numberOfFacetValues = 1;

    testSearch(TYPE, ids, SEARCH_TERM, TYPE_STRING, facetFieldNames, FULL_TEXT_SEARCH_NAMES, numberOfFacetValues, Lists.<FacetParameter> newArrayList(), EXPECTED_TERM);
  }

  @Test
  public void testSearchWildCard() throws Exception {
    List<String> ids = Lists.newArrayList("id1");
    List<String> facetFieldNames = Lists.newArrayList("dynamic_s_birthDate");
    int numberOfFacetValues = 1;

    String expectedTerm = "*:*";
    String searchTerm = "*";
    testSearch(TYPE, ids, searchTerm, TYPE_STRING, facetFieldNames, FULL_TEXT_SEARCH_NAMES, numberOfFacetValues, Lists.<FacetParameter> newArrayList(), expectedTerm);
  }

  @Test
  public void testSearchMultipleTerms() throws Exception {
    List<String> ids = Lists.newArrayList("id1");
    List<String> facetFieldNames = Lists.newArrayList("dynamic_s_birthDate");
    int numberOfFacetValues = 1;

    String expectedTerm = "(dynamic_t_name:(test 123))";
    String searchTerm = "test 123";
    testSearch(TYPE, ids, searchTerm, TYPE_STRING, facetFieldNames, FULL_TEXT_SEARCH_NAMES, numberOfFacetValues, Lists.<FacetParameter> newArrayList(), expectedTerm);
  }

  @Test
  public void testSearchMultipleFields() throws Exception {
    List<String> ids = Lists.newArrayList("id1", "id2", "id3", "id4");
    List<String> facetFieldNames = Lists.newArrayList("dynamic_t_simple");
    int numberOfFacetValues = 1;

    List<String> fullTextSearchFields = Lists.newArrayList("dynamic_t_simple", "dynamic_t_simple1");
    String expectedTerm = String.format("(dynamic_t_simple1:%s dynamic_t_simple:%s)", SEARCH_TERM, SEARCH_TERM);

    testSearch(ClassWithMupltipleFullTestSearchFields.class, ids, SEARCH_TERM, "classwithmupltiplefulltestsearchfields", facetFieldNames, fullTextSearchFields, numberOfFacetValues,
        Lists.<FacetParameter> newArrayList(), expectedTerm);
  }

  @Test
  public void testSearchNoResults() throws Exception {
    List<String> ids = Lists.newArrayList();
    List<String> facetFieldNames = Lists.newArrayList("dynamic_s_birthDate");
    int numberOfFacetValues = 1;
    testSearch(TYPE, ids, SEARCH_TERM, TYPE_STRING, facetFieldNames, FULL_TEXT_SEARCH_NAMES, numberOfFacetValues, Lists.<FacetParameter> newArrayList(), EXPECTED_TERM);
  }

  @Test
  public void testSearchWithOneFacetOneValue() throws Exception {
    List<String> ids = Lists.newArrayList("id1");
    List<String> facetFieldNames = Lists.newArrayList("dynamic_s_birthDate");
    int numberOfFacetValues = 1;

    List<FacetParameter> facetParameters = Lists.newArrayList(createFacetParam("dynamic_s_birthDate", "value"));

    String expectedTerm = String.format("+(dynamic_t_name:%s) +dynamic_s_birthDate:value", SEARCH_TERM);

    testSearch(TYPE, ids, SEARCH_TERM, TYPE_STRING, facetFieldNames, FULL_TEXT_SEARCH_NAMES, numberOfFacetValues, facetParameters, expectedTerm);
  }

  @Test
  public void testSearchWithOneFacetMultipleValues() throws Exception {
    List<String> ids = Lists.newArrayList("id1");
    List<String> facetFieldNames = Lists.newArrayList("dynamic_s_birthDate");
    int numberOfFacetValues = 1;

    List<FacetParameter> facetParameters = Lists.newArrayList(createFacetParam("dynamic_s_birthDate", "value", "value1"));

    String expectedTerm = String.format("+(dynamic_t_name:%s) +dynamic_s_birthDate:(value value1)", SEARCH_TERM);

    testSearch(TYPE, ids, SEARCH_TERM, TYPE_STRING, facetFieldNames, FULL_TEXT_SEARCH_NAMES, numberOfFacetValues, facetParameters, expectedTerm);
  }

  @Test
  public void testSearchWithMultipleFacetsOneValue() throws Exception {
    List<String> ids = Lists.newArrayList("id1");
    List<String> facetFieldNames = Lists.newArrayList("dynamic_s_birthDate", "dynamic_s_deathDate");
    int numberOfFacetValues = 1;

    List<FacetParameter> facetParameters = Lists.newArrayList(createFacetParam("dynamic_s_birthDate", "value"), createFacetParam("dynamic_s_deathDate", "values"));

    String expectedTerm = String.format("+(dynamic_t_name:%s) +dynamic_s_birthDate:value +dynamic_s_deathDate:values", SEARCH_TERM);

    testSearch(TYPE, ids, SEARCH_TERM, TYPE_STRING, facetFieldNames, FULL_TEXT_SEARCH_NAMES, numberOfFacetValues, facetParameters, expectedTerm);
  }

  @Test
  public void testSearchWithMultipleFacetsMultipleValues() throws Exception {
    List<String> ids = Lists.newArrayList("id1");
    List<String> facetFieldNames = Lists.newArrayList("dynamic_s_birthDate", "dynamic_s_deathDate");
    int numberOfFacetValues = 1;

    List<FacetParameter> facetParameters = Lists.newArrayList(createFacetParam("dynamic_s_birthDate", "value", "value1"), createFacetParam("dynamic_s_deathDate", "value1", "value2"));

    String expectedTerm = String.format("+(dynamic_t_name:%s) +dynamic_s_birthDate:(value value1) +dynamic_s_deathDate:(value1 value2)", SEARCH_TERM);

    testSearch(TYPE, ids, SEARCH_TERM, TYPE_STRING, facetFieldNames, FULL_TEXT_SEARCH_NAMES, numberOfFacetValues, facetParameters, expectedTerm);
  }

  private void testSearch(Class<? extends DomainEntity> type, List<String> ids, String searchTerm, String typeString, List<String> facetNames, List<String> fullTextSearchNames,
      int numberOfFacetValues, List<FacetParameter> facetParameters, String expectedTerm) throws Exception {
    SearchParameters searchParameters = new SearchParameters();
    searchParameters.setTerm(searchTerm);
    searchParameters.setTypeString(typeString);
    searchParameters.setFacetValues(facetParameters);

    SolrDocumentList docs = createSolrDocumentList(ids);
    List<FacetField> facetFields = createFacetFieldList(facetNames, numberOfFacetValues);
    setUpQueryResponse(docs, facetFields);

    List<FacetCount> facets = createFacetCountList(facetNames, numberOfFacetValues);
    SearchResult expected = createExpectedResult(typeString, ids, expectedTerm, facets);

    SearchResult actual = instance.search(scope, type, searchParameters);
    verifySearchResult(expected, actual);
  }

  @Test(expected = NoSuchFacetException.class)
  public void testSearchThrowsExceptionForMissingFacet() throws Exception {
    SearchParameters searchParameters = new SearchParameters();
    searchParameters.setTerm(SEARCH_TERM);
    searchParameters.setTypeString(TYPE_STRING);
    searchParameters.setFacetValues(Lists.newArrayList(new FacetParameter().setName("unknown")));

    instance.search(scope, Person.class, searchParameters);
  }

  private void verifySearchResult(SearchResult expected, SearchResult actual) {
    assertEquals(expected.getIds().size(), actual.getIds().size());
    assertEquals(expected.getFacets().size(), actual.getFacets().size());
    assertEquals(expected.getFacets().size(), actual.getFacets().size());
    for (int i = 0; i < expected.getFacets().size(); i++) {
      //      assertEquals(expected.getFacets().get(i).getOptions().size(), actual.getFacets().get(i).getOptions().size());
    }

    assertEquals(expected.getTerm(), actual.getTerm());
    assertEquals(expected.getSearchType(), actual.getSearchType());
    assertNotNull(actual.getDate());
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
    //    expected.setFacets(Lists.newArrayList(facets));
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

  private void setUpQueryResponse(SolrDocumentList docs, List<FacetField> facetFields) throws Exception {
    QueryResponse response = mock(QueryResponse.class);
    when(response.getResults()).thenReturn(docs);
    when(response.getFacetFields()).thenReturn(facetFields);
    when(indexManager.search(any(Scope.class), Matchers.<Class<? extends DomainEntity>> any(), any(SolrQuery.class))).thenReturn(response);
  }

  private SolrDocumentList createSolrDocumentList(List<String> ids) {
    SolrDocumentList docs = mock(SolrDocumentList.class);
    List<SolrDocument> solrDocuments = createSolrDocuments(ids);
    Iterator<SolrDocument> iterator = solrDocuments.iterator();
    when(docs.iterator()).thenReturn(iterator);
    return docs;
  }

  private List<SolrDocument> createSolrDocuments(List<String> ids) {
    List<SolrDocument> documents = Lists.newArrayList();
    for (String documentId : ids) {
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
