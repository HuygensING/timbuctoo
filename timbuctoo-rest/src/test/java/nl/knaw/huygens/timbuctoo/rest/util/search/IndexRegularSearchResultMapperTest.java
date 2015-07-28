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
import nl.knaw.huygens.timbuctoo.vre.VRE;
import nl.knaw.huygens.timbuctoo.vre.VRECollection;
import org.junit.Before;
import org.junit.Test;
import test.rest.model.projecta.ProjectADomainEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

public class IndexRegularSearchResultMapperTest {
  protected static final Class<ProjectADomainEntity> DEFAULT_TYPE = ProjectADomainEntity.class;
  protected static final String INTERNAL_TYPE_NAME = TypeNames.getInternalName(DEFAULT_TYPE);
  public static final String VRE_ID = "vreId";
  public static final List<Map<String, Object>> RAW_DATA = Lists.newArrayList();
  public static final String NEXT_RESULTS = "nextResults";
  public static final String PREV_RESULTS = "prevResults";
  public static final int NORMALIZED_START = 0;
  public static final int START = 0;
  public static final int ROWS = 10;
  public static final int NORMALIZED_ROWS = 10;
  public static final String QUERY_ID = "queryId";
  public static final List<Facet> FACETS = Lists.newArrayList();
  public static final Set<String> FULL_TEXT_SEARCH_FIELDS = Sets.newHashSet();
  public static final Set<String> SORTABLE_FIELDS = Sets.newHashSet();
  public static final String TERM = "term";
  public static final ArrayList<DomainEntityDTO> REFS = Lists.<DomainEntityDTO>newArrayList();
  private IndexRegularSearchResultMapper instance;
  private Repository repository;
  private SortableFieldFinder sortableFieldFinder;
  private HATEOASURICreator uriCreator;
  private FullTextSearchFieldFinder fullTextSearchFieldFinder;
  private VRECollection vreCollection;
  public static final List<String> IDS = Lists.newArrayList("id1", "id2", "id3", "id4", "id5", "id6", "id7", "id8", "id9", "id10");
  public static final int NUM_FOUND = IDS.size();
  private RangeHelper rangeHelper;
  private SearchResult searchResult;
  private VRE vre;
  private DomainEntityDTOFactory domainEntityDTOFactory;

  @Before
  public void setup() {
    repository = mock(Repository.class);
    setupSearchResult();
    setupVRE();
    setupURICreator();
    setupRangeHelper();
    setupFullTextSearchFieldFinder();
    setupSortableFieldFinder();
    setupRefCreatorFactory();

    instance = new IndexRegularSearchResultMapper(repository, sortableFieldFinder, uriCreator, fullTextSearchFieldFinder, vreCollection, rangeHelper, domainEntityDTOFactory);
  }

  private void setupRefCreatorFactory() {
    domainEntityDTOFactory = mock(DomainEntityDTOFactory.class);
    when(domainEntityDTOFactory.createFor(RAW_DATA)).thenReturn(REFS);
  }

  public void setupSortableFieldFinder() {
    sortableFieldFinder = mock(SortableFieldFinder.class);
    when(sortableFieldFinder.findFields(DEFAULT_TYPE)).thenReturn(SORTABLE_FIELDS);
  }

  public void setupFullTextSearchFieldFinder() {
    fullTextSearchFieldFinder = mock(FullTextSearchFieldFinder.class);
    when(fullTextSearchFieldFinder.findFields(DEFAULT_TYPE)).thenReturn(FULL_TEXT_SEARCH_FIELDS);
  }

  public void setupVRE() {
    vreCollection = mock(VRECollection.class);
    vre = mock(VRE.class);
    when(vreCollection.getVREById(VRE_ID)).thenReturn(vre);
    when(vre.getRawDataFor(DEFAULT_TYPE, IDS)).thenReturn(RAW_DATA);
  }

  public void setupSearchResult() {
    searchResult = new SearchResult();
    searchResult.setIds(IDS);
    searchResult.setSearchType(INTERNAL_TYPE_NAME);
    searchResult.setId(QUERY_ID);
    searchResult.setFacets(FACETS);
    searchResult.setTerm(TERM);
    searchResult.setVreId(VRE_ID);
  }

  public void setupRangeHelper() {
    rangeHelper = mock(RangeHelper.class);
    when(rangeHelper.mapToRange(START, 0, NUM_FOUND)).thenReturn(NORMALIZED_START);
    when(rangeHelper.mapToRange(ROWS, 0, NUM_FOUND - NORMALIZED_START)).thenReturn(NORMALIZED_ROWS);
  }

  public void setupURICreator() {
    uriCreator = mock(HATEOASURICreator.class);
    when(uriCreator.createNextResultsAsString(START, ROWS, NUM_FOUND, QUERY_ID)).thenReturn(NEXT_RESULTS);
    when(uriCreator.createPrevResultsAsString(START, ROWS, QUERY_ID)).thenReturn(PREV_RESULTS);
  }

  @Test
  public void createCreatesASearchResultDTOWithDomainEntityDTOsOfTheFoundResults() {
    // action
    RegularSearchResultDTO searchResultDTO = instance.create(DEFAULT_TYPE, searchResult, START, ROWS);

    // verify
    assertThat(searchResultDTO, RegularClientSearchResultMatcher.likeRegularSearchResultDTO() //
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
  }


  @Test
  public void createRetrievesAllTheInfromationFromTheIndexAndHasNoInteractionWithTheRepository() {
    instance.create(DEFAULT_TYPE, searchResult, START, ROWS);

    verifyZeroInteractions(repository);
  }
}
