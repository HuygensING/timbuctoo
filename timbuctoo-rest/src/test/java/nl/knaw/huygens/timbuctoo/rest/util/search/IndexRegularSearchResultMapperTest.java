package nl.knaw.huygens.timbuctoo.rest.util.search;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import nl.knaw.huygens.facetedsearch.model.Facet;
import nl.knaw.huygens.timbuctoo.Repository;
import nl.knaw.huygens.timbuctoo.config.TypeNames;
import nl.knaw.huygens.timbuctoo.model.DomainEntityDTO;
import nl.knaw.huygens.timbuctoo.model.RegularSearchResultDTO;
import nl.knaw.huygens.timbuctoo.model.SearchResult;
import nl.knaw.huygens.timbuctoo.rest.util.HATEOASURICreator;
import nl.knaw.huygens.timbuctoo.rest.util.RangeHelper;
import nl.knaw.huygens.timbuctoo.search.FullTextSearchFieldFinder;
import nl.knaw.huygens.timbuctoo.search.SortableFieldFinder;
import nl.knaw.huygens.timbuctoo.vre.NotInScopeException;
import nl.knaw.huygens.timbuctoo.vre.SearchException;
import nl.knaw.huygens.timbuctoo.vre.VRE;
import nl.knaw.huygens.timbuctoo.vre.VRECollection;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import test.rest.model.projecta.ProjectADomainEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static nl.knaw.huygens.timbuctoo.rest.util.search.RegularSearchResultDTOMatcher.likeRegularSearchResultDTO;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

public class IndexRegularSearchResultMapperTest {
  private static final Class<ProjectADomainEntity> DEFAULT_TYPE = ProjectADomainEntity.class;
  private static final String INTERNAL_TYPE_NAME = TypeNames.getInternalName(DEFAULT_TYPE);
  private static final String VRE_ID = "vreId";
  private static final List<Map<String, Object>> RAW_DATA = Lists.newArrayList();
  private static final String NEXT_RESULTS = "nextResults";
  private static final String PREV_RESULTS = "prevResults";
  private static final int NORMALIZED_START = 1;
  private static final int START = 0;
  private static final int ROWS = 10;
  private static final int NORMALIZED_ROWS = 4;
  private static final String QUERY_ID = "queryId";
  private static final List<Facet> FACETS = Lists.newArrayList();
  private static final Set<String> FULL_TEXT_SEARCH_FIELDS = Sets.newHashSet();
  private static final Set<String> SORTABLE_FIELDS = Sets.newHashSet();
  private static final String TERM = "term";
  private static final ArrayList<DomainEntityDTO> REFS = Lists.<DomainEntityDTO>newArrayList();
  private static final String VERSION = "v1";
  private IndexRegularSearchResultMapper instance;
  private Repository repository;
  private SortableFieldFinder sortableFieldFinder;
  private HATEOASURICreator uriCreator;
  private FullTextSearchFieldFinder fullTextSearchFieldFinder;
  private VRECollection vreCollection;
  private static final List<String> IDS = Lists.newArrayList("id1", "id2", "id3", "id4", "id5", "id6", "id7", "id8", "id9", "id10");
  private static final List<String> ID_SUBLIST = Lists.newArrayList("id2", "id3", "id4", "id5");
  private static final int NUM_FOUND = IDS.size();
  private RangeHelper rangeHelper;
  private SearchResult searchResult;
  private VRE vre;
  private DomainEntityDTOListFactory domainEntityDTOListFactory;

  @Before
  public void setup() throws Exception {
    repository = mock(Repository.class);
    setupSearchResult();
    setupVRE();
    setupURICreator();
    setupRangeHelper();
    setupFullTextSearchFieldFinder();
    setupSortableFieldFinder();
    setupRefCreatorFactory();

    instance = new IndexRegularSearchResultMapper(repository, sortableFieldFinder, uriCreator, fullTextSearchFieldFinder, vreCollection, rangeHelper, domainEntityDTOListFactory);
  }

  private void setupRefCreatorFactory() throws Exception {
    domainEntityDTOListFactory = mock(DomainEntityDTOListFactory.class);
    when(domainEntityDTOListFactory.createFor(DEFAULT_TYPE, RAW_DATA)).thenReturn(REFS);
  }

  private void setupSortableFieldFinder() {
    sortableFieldFinder = mock(SortableFieldFinder.class);
    when(sortableFieldFinder.findFields(DEFAULT_TYPE)).thenReturn(SORTABLE_FIELDS);
  }

  private void setupFullTextSearchFieldFinder() {
    fullTextSearchFieldFinder = mock(FullTextSearchFieldFinder.class);
    when(fullTextSearchFieldFinder.findFields(DEFAULT_TYPE)).thenReturn(FULL_TEXT_SEARCH_FIELDS);
  }

  private void setupVRE() throws Exception {
    vreCollection = mock(VRECollection.class);
    vre = mock(VRE.class);
    when(vreCollection.getVREById(VRE_ID)).thenReturn(vre);
    when(vre.getRawDataFor(DEFAULT_TYPE, ID_SUBLIST)).thenReturn(RAW_DATA);

  }

  private void setupSearchResult() {
    searchResult = new SearchResult();
    searchResult.setIds(IDS);
    searchResult.setSearchType(INTERNAL_TYPE_NAME);
    searchResult.setId(QUERY_ID);
    searchResult.setFacets(FACETS);
    searchResult.setTerm(TERM);
    searchResult.setVreId(VRE_ID);
  }

  private void setupRangeHelper() {
    rangeHelper = mock(RangeHelper.class);
    when(rangeHelper.mapToRange(START, 0, NUM_FOUND)).thenReturn(NORMALIZED_START);
    when(rangeHelper.mapToRange(ROWS, 0, NUM_FOUND - NORMALIZED_START)).thenReturn(NORMALIZED_ROWS);
  }

  private void setupURICreator() {
    uriCreator = mock(HATEOASURICreator.class);
    when(uriCreator.createNextResultsAsString(NORMALIZED_START, ROWS, NUM_FOUND, QUERY_ID, VERSION)).thenReturn(NEXT_RESULTS);
    when(uriCreator.createPrevResultsAsString(NORMALIZED_START, ROWS, QUERY_ID, VERSION)).thenReturn(PREV_RESULTS);
  }

  @Test
  public void createCreatesASearchResultDTOWithDomainEntityDTOsOfTheFoundResults() throws Exception {
    // action
    RegularSearchResultDTO searchResultDTO = instance.create(DEFAULT_TYPE, searchResult, START, ROWS, VERSION);

    // verify
    assertThat(searchResultDTO, likeRegularSearchResultDTO() //
      .withFacets(FACETS) //
      .withFullTextSearchFields(FULL_TEXT_SEARCH_FIELDS) //
      .withIds(IDS) //
      .withNextLink(NEXT_RESULTS) //
      .withPrevLink(PREV_RESULTS) //
      .withNumfound(NUM_FOUND) //
      .withRows(NORMALIZED_ROWS) //
      .withStart(NORMALIZED_START) //
      .withSortableFields(SORTABLE_FIELDS) //
      .withTerm(TERM)
      .withRefs(REFS));

    verify(vre).getRawDataFor(DEFAULT_TYPE, ID_SUBLIST);
  }

  @Test
  public void createRetrievesAllTheInfromationFromTheIndexAndHasNoInteractionWithTheRepository() throws Exception {
    // action
    instance.create(DEFAULT_TYPE, searchResult, START, ROWS, VERSION);

    // verify
    verifyZeroInteractions(repository);
  }

  @Rule
  public ExpectedException exception = ExpectedException.none();

  @Test
  public void createThrowsARuntimeExceptionWhenGetRawDataForThrowsANotInScopeException() throws Exception {
    // setup
    NotInScopeException notInScopeException = NotInScopeException.typeIsNotInScope(DEFAULT_TYPE, VRE_ID);
    when(vre.getRawDataFor(DEFAULT_TYPE, ID_SUBLIST)).thenThrow(notInScopeException);

    exception.expect(RuntimeException.class);
    exception.expectCause(is(notInScopeException));

    // action
    instance.create(DEFAULT_TYPE, searchResult, START, ROWS, VERSION);
  }

  @Test
  public void createThrowsARuntimeExceptionWhenGetRawDataForThrowsASearchException() throws Exception {
    // setup
    SearchException searchException = new SearchException(new Exception());
    when(vre.getRawDataFor(DEFAULT_TYPE, ID_SUBLIST)).thenThrow(searchException);

    exception.expect(RuntimeException.class);
    exception.expectCause(is(searchException));

    // action
    instance.create(DEFAULT_TYPE, searchResult, START, ROWS, VERSION);
  }

  @Test
  public void createThrowsARuntimeExceptionWhenTheDomainEntityDTOListFactoryThrowsASearchResultCreationException() throws Exception {
    // setup
    SearchResultCreationException searchResultCreationException = new SearchResultCreationException(DEFAULT_TYPE, new Exception());
    when(domainEntityDTOListFactory.createFor(DEFAULT_TYPE, RAW_DATA)).thenThrow(searchResultCreationException);

    exception.expect(RuntimeException.class);
    exception.expectCause(is(searchResultCreationException));

    // action
    instance.create(DEFAULT_TYPE, searchResult, START, ROWS, VERSION);
  }
}
