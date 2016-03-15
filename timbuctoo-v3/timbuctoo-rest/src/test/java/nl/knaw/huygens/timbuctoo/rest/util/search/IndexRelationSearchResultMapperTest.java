package nl.knaw.huygens.timbuctoo.rest.util.search;

/*
 * #%L
 * Timbuctoo REST api
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
import com.google.common.collect.Sets;
import nl.knaw.huygens.facetedsearch.model.DefaultFacet;
import nl.knaw.huygens.facetedsearch.model.Facet;
import nl.knaw.huygens.facetedsearch.model.parameters.SortDirection;
import nl.knaw.huygens.facetedsearch.model.parameters.SortParameter;
import nl.knaw.huygens.timbuctoo.Repository;
import nl.knaw.huygens.timbuctoo.config.TypeNames;
import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.model.ModelException;
import nl.knaw.huygens.timbuctoo.model.RelationDTO;
import nl.knaw.huygens.timbuctoo.model.RelationSearchResultDTOV2_1;
import nl.knaw.huygens.timbuctoo.model.SearchResult;
import nl.knaw.huygens.timbuctoo.model.SearchResultDTO;
import nl.knaw.huygens.timbuctoo.rest.util.HATEOASURICreator;
import nl.knaw.huygens.timbuctoo.rest.util.RangeHelper;
import nl.knaw.huygens.timbuctoo.search.FullTextSearchFieldFinder;
import nl.knaw.huygens.timbuctoo.search.SortableFieldFinder;
import nl.knaw.huygens.timbuctoo.search.converters.RelationSearchParametersConverter;
import nl.knaw.huygens.timbuctoo.vre.NotInScopeException;
import nl.knaw.huygens.timbuctoo.vre.SearchException;
import nl.knaw.huygens.timbuctoo.vre.VRE;
import nl.knaw.huygens.timbuctoo.vre.VRECollection;
import nl.knaw.huygens.timbuctoo.vre.VREException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import test.rest.model.projecta.ProjectADomainEntity;
import test.rest.model.projecta.ProjectAPerson;
import test.rest.model.projecta.ProjectARelation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static nl.knaw.huygens.timbuctoo.rest.util.search.DefaultFacetMatcher.likeDefaultFacet;
import static nl.knaw.huygens.timbuctoo.rest.util.search.FacetOptionMatcher.likeFacetOption;
import static nl.knaw.huygens.timbuctoo.rest.util.search.RelationSearchResultDTOV2_1Matcher.likeRelationSearchResultDTOV2_1;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static test.rest.model.projecta.ProjectADomainEntity.FULL_TEXT_SEARCH_FIELD;

public class IndexRelationSearchResultMapperTest {

  private static final Class<ProjectARelation> TYPE = ProjectARelation.class;
  private static final String INTERNAL_TYPE_NAME = TypeNames.getInternalName(TYPE);
  private static final String VRE_ID = "vreId";
  private static final int START = 0;
  private static final int ROWS = 10;
  private static final String VERSION = "v1";
  private static final List<String> IDS = Lists.newArrayList("id1", "id2", "id3", "id4", "id5", "id6", "id7", "id8", "id9", "id10");
  private static final List<String> ID_SUB_LIST = Lists.newArrayList("id2", "id3", "id4", "id5");
  public static final int NUM_FOUND = IDS.size();
  private static final List<SortParameter> SORT = Lists.newArrayList(new SortParameter("field", SortDirection.ASCENDING));
  private static final String QUERY_ID = "queryId";
  public static final DefaultFacet FACET = new DefaultFacet("test", "test");
  private static final List<Facet> FACETS = Lists.newArrayList(FACET);
  private static final String TERM = "term";
  public static final int NORMALIZED_ROWS = 4;
  public static final int NORMALIZED_START = 1;
  public static final HashSet<String> SORTABLE_FIELDS = Sets.newHashSet();
  public static final String NEXT_LINK = "nextLink";
  public static final String PREV_LINK = "prevLink";
  public static final ArrayList<RelationDTO> REFS = Lists.newArrayList(new RelationDTO());
  private static final List<Map<String, Object>> RAW_DATA = Lists.newArrayList(Maps.newHashMap());
  public static final Class<ProjectAPerson> SOURCE_TYPE = ProjectAPerson.class;
  public static final String SOURCE_TYPE_STRING = TypeNames.getInternalName(SOURCE_TYPE);
  public static final Class<ProjectADomainEntity> TARGET_TYPE = ProjectADomainEntity.class;
  public static final String TARGET_TYPE_STRING = TypeNames.getInternalName(TARGET_TYPE);
  public static final String RELATION_1 = "relation1";
  public static final String RELATION_2 = "relation2";
  public static final ArrayList<String> POSSIBLE_RELATIONS = Lists.newArrayList(RELATION_1, RELATION_2);

  private Repository repository;
  private SortableFieldFinder sortableFieldFinder;
  private HATEOASURICreator uriCreator;
  private RelationDTOListFactory relationDTOFactory;
  private VRECollection vreCollection;
  private RangeHelper rangeHelper;
  private IndexRelationSearchResultMapper instance;
  private SearchResult searchResult;
  private VRE vre;
  private TypeRegistry registry;
  private FullTextSearchFieldFinder fullTextSearchFieldFinder;

  @Before
  public void setup() throws Exception {
    repository = mock(Repository.class);
    setupSortableFieldFinder();
    setupURICreator();
    setupVRE();
    setupRelationDTOFactory();
    setupRangeHelper();
    setupSearchResult();
    setupRegistry();
    setupFullTextSearchFieldFinder();

    this.instance = new IndexRelationSearchResultMapper(repository, sortableFieldFinder, uriCreator, relationDTOFactory, vreCollection, rangeHelper, fullTextSearchFieldFinder, registry);
  }

  private void setupFullTextSearchFieldFinder() {
    fullTextSearchFieldFinder = mock(FullTextSearchFieldFinder.class);
    when(fullTextSearchFieldFinder.findFields(TARGET_TYPE)).thenReturn(Sets.newHashSet(FULL_TEXT_SEARCH_FIELD));
  }

  private void setupRegistry() throws ModelException {
    registry = TypeRegistry.getInstance();
    registry.init(TARGET_TYPE.getPackage().getName());
  }

  private void setupRelationDTOFactory() throws Exception {
    relationDTOFactory = mock(RelationDTOListFactory.class);
    when(relationDTOFactory.create(vre, TYPE, RAW_DATA)).thenReturn(REFS);
  }

  private void setupSortableFieldFinder() {
    sortableFieldFinder = mock(SortableFieldFinder.class);
    when(sortableFieldFinder.findFields(TYPE)).thenReturn(SORTABLE_FIELDS);
  }

  private void setupVRE() throws Exception {
    vreCollection = mock(VRECollection.class);
    vre = mock(VRE.class);
    when(vreCollection.getVREById(VRE_ID)).thenReturn(vre);
    when(vre.getRawDataFor(TYPE, ID_SUB_LIST, SORT)).thenReturn(RAW_DATA);
    when(vre.getRelationTypeNamesBetween(SOURCE_TYPE, TARGET_TYPE)).thenReturn(POSSIBLE_RELATIONS);
  }

  private void setupRangeHelper() {
    rangeHelper = mock(RangeHelper.class);
    when(rangeHelper.mapToRange(START, 0, NUM_FOUND)).thenReturn(NORMALIZED_START);
    when(rangeHelper.mapToRange(ROWS, 0, NUM_FOUND - NORMALIZED_START)).thenReturn(NORMALIZED_ROWS);
  }

  private void setupURICreator() {
    uriCreator = mock(HATEOASURICreator.class);
    when(uriCreator.createNextResultsAsString(NORMALIZED_START, ROWS, NUM_FOUND, QUERY_ID, VERSION)).thenReturn(NEXT_LINK);
    when(uriCreator.createPrevResultsAsString(NORMALIZED_START, ROWS, QUERY_ID, VERSION)).thenReturn(PREV_LINK);
  }


  private void setupSearchResult() {
    searchResult = new SearchResult();
    searchResult.setIds(IDS);
    searchResult.setSearchType(INTERNAL_TYPE_NAME);
    searchResult.setId(QUERY_ID);
    searchResult.setFacets(FACETS);
    searchResult.setTerm(TERM);
    searchResult.setVreId(VRE_ID);
    searchResult.setSort(SORT);
    searchResult.setSourceType(SOURCE_TYPE_STRING);
    searchResult.setTargetType(TARGET_TYPE_STRING);
  }

  @Test
  public void createCreatesASearchResultDTOWithDomainEntityDTOsOfTheFoundResults() throws SearchResultCreationException {
    // action
    SearchResultDTO searchResultDTO = instance.create(TYPE, searchResult, START, ROWS, VERSION);

    // verify
    assertThat(searchResultDTO, is(instanceOf(RelationSearchResultDTOV2_1.class)));

    assertThat((RelationSearchResultDTOV2_1) searchResultDTO, likeRelationSearchResultDTOV2_1()
      .withTerm(TERM) //
      .withRows(NORMALIZED_ROWS) //
      .withStart(NORMALIZED_START) //
      .withSortableFields(SORTABLE_FIELDS) //
      .withoutIds() //
      .withNextLink(NEXT_LINK) //
      .withPrevLink(PREV_LINK) //
      .withNumFound(NUM_FOUND) //
      .withFacet(FACET) //
      .withRefs(REFS));
  }

  @Test
  public void createAddsTheReceptionRelationsBetweenTheSourceAndTheTargetTypeOfTheVREAsFacet() {
    // action
    SearchResultDTO searchResultDTO = instance.create(TYPE, searchResult, START, ROWS, VERSION);

    assertThat(searchResultDTO, is(instanceOf(RelationSearchResultDTOV2_1.class)));

    assertThat(((RelationSearchResultDTOV2_1) searchResultDTO).getFacets(), //
      hasItem(likeDefaultFacet() //
        .withName(RelationSearchParametersConverter.RELATION_FACET) //
        .withOptions( //
          likeFacetOption().withName(RELATION_1), //
          likeFacetOption().withName(RELATION_2))));
  }

  @Test
  public void createAddsTheFullTextSearchFieldsOfTheTargetType() {
    // action
    SearchResultDTO searchResultDTO = instance.create(TYPE, searchResult, START, ROWS, VERSION);

    // verify
    assertThat(searchResultDTO, is(instanceOf(RelationSearchResultDTOV2_1.class)));

    assertThat((RelationSearchResultDTOV2_1) searchResultDTO, likeRelationSearchResultDTOV2_1()
      .withFullTextSearchField(FULL_TEXT_SEARCH_FIELD));
  }

  @Rule
  public ExpectedException exception = ExpectedException.none();

  @Test
  public void createThrowsARuntimeExceptionWhenGetRawDataForThrowsANotInScopeException() throws Exception {
    // setup
    NotInScopeException notInScopeException = NotInScopeException.typeIsNotInScope(TYPE, VRE_ID);
    when(vre.getRawDataFor(TYPE, ID_SUB_LIST, SORT)).thenThrow(notInScopeException);

    exception.expect(RuntimeException.class);
    exception.expectCause(is(notInScopeException));

    // action
    instance.create(TYPE, searchResult, START, ROWS, VERSION);
  }

  @Test
  public void createThrowsARuntimeExceptionWhenGetRawDataForThrowsASearchException() throws Exception {
    // setup
    SearchException searchException = new SearchException(new Exception());
    when(vre.getRawDataFor(TYPE, ID_SUB_LIST, SORT)).thenThrow(searchException);

    exception.expect(RuntimeException.class);
    exception.expectCause(is(searchException));

    // action
    instance.create(TYPE, searchResult, START, ROWS, VERSION);
  }

  @Test
  public void createThrowsARuntimeExceptionWhenTheRelationDTOListFactoryThrowsASearchResultCreationException() throws Exception {
    // setup
    SearchResultCreationException searchResultCreationException = new SearchResultCreationException(TYPE, new Exception());
    when(relationDTOFactory.create(vre, TYPE, RAW_DATA)).thenThrow(searchResultCreationException);

    exception.expect(RuntimeException.class);
    exception.expectCause(is(searchResultCreationException));

    // action
    instance.create(TYPE, searchResult, START, ROWS, VERSION);
  }

  @Test
  public void createThrowsARuntimeExceptionWhenVREsGetRelationTypeNamesBetweenThrowsAVREException() throws VREException {
    // setup
    when(vre.getRelationTypeNamesBetween(SOURCE_TYPE, TARGET_TYPE)).thenThrow(new VREException(new Exception()));

    exception.expect(RuntimeException.class);
    exception.expectCause(is(instanceOf(VREException.class)));

    // action
    instance.create(TYPE, searchResult, START, ROWS, VERSION);

  }
}
