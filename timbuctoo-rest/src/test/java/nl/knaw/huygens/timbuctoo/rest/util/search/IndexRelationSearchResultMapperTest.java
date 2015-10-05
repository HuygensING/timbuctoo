package nl.knaw.huygens.timbuctoo.rest.util.search;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import nl.knaw.huygens.facetedsearch.model.Facet;
import nl.knaw.huygens.facetedsearch.model.parameters.SortDirection;
import nl.knaw.huygens.facetedsearch.model.parameters.SortParameter;
import nl.knaw.huygens.timbuctoo.Repository;
import nl.knaw.huygens.timbuctoo.config.TypeNames;
import nl.knaw.huygens.timbuctoo.model.RelationDTO;
import nl.knaw.huygens.timbuctoo.model.RelationSearchResultDTO;
import nl.knaw.huygens.timbuctoo.model.SearchResult;
import nl.knaw.huygens.timbuctoo.rest.util.HATEOASURICreator;
import nl.knaw.huygens.timbuctoo.rest.util.RangeHelper;
import nl.knaw.huygens.timbuctoo.search.SortableFieldFinder;
import nl.knaw.huygens.timbuctoo.vre.NotInScopeException;
import nl.knaw.huygens.timbuctoo.vre.SearchException;
import nl.knaw.huygens.timbuctoo.vre.VRE;
import nl.knaw.huygens.timbuctoo.vre.VRECollection;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import test.rest.model.projecta.ProjectARelation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static nl.knaw.huygens.timbuctoo.rest.util.search.RelationSearchResultDTOMatcher.likeRelationSearchResultDTO;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
  private static final List<Facet> FACETS = Lists.newArrayList();
  private static final String TERM = "term";
  public static final int NORMALIZED_ROWS = 4;
  public static final int NORMALIZED_START = 1;
  public static final HashSet<String> SORTABLE_FIELDS = Sets.newHashSet();
  public static final String NEXT_LINK = "nextLink";
  public static final String PREV_LINK = "prevLink";
  public static final ArrayList<RelationDTO> REFS = Lists.newArrayList();
  private static final List<Map<String, Object>> RAW_DATA = Lists.newArrayList();

  private Repository repository;
  private SortableFieldFinder sortableFieldFinder;
  private HATEOASURICreator uriCreator;
  private RelationDTOListFactory relationDTOFactory;
  private VRECollection vreCollection;
  private RangeHelper rangeHelper;
  private IndexRelationSearchResultMapper instance;
  private SearchResult searchResult;
  private VRE vre;

  @Before
  public void setup() throws Exception {
    repository = mock(Repository.class);
    setupSortableFieldFinder();
    setupURICreator();
    setupRelationDTOFactory();
    setupVRE();
    setupRangeHelper();

    setupSearchResult();

    instance = new IndexRelationSearchResultMapper(repository, sortableFieldFinder, uriCreator, relationDTOFactory, vreCollection, rangeHelper);
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
  }

  @Test
  public void createCreatesASearchResultDTOWithDomainEntityDTOsOfTheFoundResults() {
    // action
    RelationSearchResultDTO searchResultDTO = instance.create(TYPE, searchResult, START, ROWS, VERSION);

    // verify
    assertThat(searchResultDTO, likeRelationSearchResultDTO()
      .withRows(NORMALIZED_ROWS) //
      .withStart(NORMALIZED_START) //
      .withSortableFields(SORTABLE_FIELDS) //
      .withIds(IDS) //
      .withNextLink(NEXT_LINK) //
      .withPrevLink(PREV_LINK) //
      .withNumFound(NUM_FOUND) //
      .withRefs(REFS));
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
}
