package nl.knaw.huygens.timbuctoo.rest.resources;

import static nl.knaw.huygens.timbuctoo.rest.resources.RegularClientSearchResultMatcher.newClientSearchResultMatcher;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import nl.knaw.huygens.facetedsearch.model.Facet;
import nl.knaw.huygens.timbuctoo.Repository;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.ClientEntityRepresentation;
import nl.knaw.huygens.timbuctoo.model.RegularClientSearchResult;
import nl.knaw.huygens.timbuctoo.model.SearchResult;
import nl.knaw.huygens.timbuctoo.rest.model.projecta.OtherDomainEntity;
import nl.knaw.huygens.timbuctoo.search.SortableFieldFinder;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class RegularClientSearchResultCreatorTest {
  private static final String NEXT_LINK = "http://www.test.com/next";
  private static final String PREV_LINK = "http://www.test.com/prev";
  private static final HashSet<String> SORTABLE_FIELDS = Sets.newHashSet();
  private static final ArrayList<ClientEntityRepresentation> UNIMPORTANT_REF_LIST = Lists.newArrayList();
  private static final Class<OtherDomainEntity> TYPE = OtherDomainEntity.class;
  private static final String TERM = "term";
  private static final ArrayList<Facet> FACET_LIST = Lists.newArrayList();
  private static final ArrayList<String> ID_LIST_WITH_TEN_IDS = Lists.newArrayList("id1", "id2", "id3", "id4", "id5", "id6", "id7", "id8", "id9", "id10");
  private static final int NUMBER_OF_RESULTS_FOUND = ID_LIST_WITH_TEN_IDS.size();
  private static final String QUERY_ID = "queryId";
  private Repository repositoryMock;
  private SortableFieldFinder sortableFieldFinderMock;
  private EntityRefCreator entityRefCreatorMock;
  private RegularClientSearchResultCreator instance;
  private SearchResult searchResult;
  private String nullPrevLink;
  private String nullNextLink;
  private HATEOASURICreator hateoasURICreatorMock;

  @Before
  public void setUp() {
    hateoasURICreatorMock = mock(HATEOASURICreator.class);
    repositoryMock = mock(Repository.class);
    sortableFieldFinderMock = mock(SortableFieldFinder.class);
    entityRefCreatorMock = mock(EntityRefCreator.class);
    when(sortableFieldFinderMock.findFields(TYPE)).thenReturn(SORTABLE_FIELDS);

    instance = new RegularClientSearchResultCreator(repositoryMock, sortableFieldFinderMock, entityRefCreatorMock, hateoasURICreatorMock);

    searchResult = new SearchResult();
    searchResult.setId(QUERY_ID);
    searchResult.setIds(ID_LIST_WITH_TEN_IDS);
    searchResult.setTerm(TERM);
    searchResult.setFacets(FACET_LIST);
  }

  @Test
  public void testCreateStartIsZero() throws InstantiationException, IllegalAccessException {
    int start = 0;
    int rows = 10;
    int normalizedStart = 0;

    final ArrayList<String> idsToGet = ID_LIST_WITH_TEN_IDS;
    List<OtherDomainEntity> result = setupRepository(TYPE, idsToGet);
    int normalizedRows = 10;
    testCreate(TYPE, start, rows, normalizedStart, normalizedRows, idsToGet, result, nullNextLink, nullPrevLink, NUMBER_OF_RESULTS_FOUND);

  }

  @Test
  public void testCreateStartIsGreaterThanZero() throws InstantiationException, IllegalAccessException {
    int start = 1;
    int rows = 9;

    final List<String> idsToGet = ID_LIST_WITH_TEN_IDS.subList(start, 10);
    List<OtherDomainEntity> result = setupRepository(TYPE, idsToGet);

    final int previousStart = 0;
    when(hateoasURICreatorMock.createHATEOASURIAsString(previousStart, rows, QUERY_ID)).thenReturn(PREV_LINK);
    int normalizedRows = 9;
    testCreate(TYPE, start, rows, start, normalizedRows, idsToGet, result, nullNextLink, PREV_LINK, NUMBER_OF_RESULTS_FOUND);
  }

  @Test
  public void testCreateStartIsSmallerThanZero() throws InstantiationException, IllegalAccessException {
    int start = -1;
    int rows = 10;

    final int normalizedStart = 0;
    final List<String> idsToGet = ID_LIST_WITH_TEN_IDS.subList(normalizedStart, 10);
    List<OtherDomainEntity> result = setupRepository(TYPE, idsToGet);

    int normalizedRows = 10;
    testCreate(TYPE, start, rows, normalizedStart, normalizedRows, idsToGet, result, nullNextLink, nullPrevLink, NUMBER_OF_RESULTS_FOUND);
  }

  @Test
  public void testCreateStartAndRowsIsSmallerThatMax() throws InstantiationException, IllegalAccessException {
    int start = 0;
    int rows = 5;

    final int normalizedStart = 0;
    final List<String> idsToGet = ID_LIST_WITH_TEN_IDS.subList(normalizedStart, 5);
    List<OtherDomainEntity> result = setupRepository(TYPE, idsToGet);

    int nextStart = 5;
    when(hateoasURICreatorMock.createHATEOASURIAsString(nextStart, rows, QUERY_ID)).thenReturn(NEXT_LINK);

    int normalizedRows = 5;
    testCreate(TYPE, start, rows, normalizedStart, normalizedRows, idsToGet, result, NEXT_LINK, nullPrevLink, NUMBER_OF_RESULTS_FOUND);
  }

  @Test
  public void testCreateWithRowsMoreThanMax() throws InstantiationException, IllegalAccessException {
    int start = 0;
    int rows = 20;

    final int normalizedStart = 0;
    final int normalizedRows = 10;
    final List<String> idsToGet = ID_LIST_WITH_TEN_IDS.subList(normalizedStart, 10);
    List<OtherDomainEntity> result = setupRepository(TYPE, idsToGet);

    testCreate(TYPE, start, rows, normalizedStart, normalizedRows, idsToGet, result, nullNextLink, nullPrevLink, NUMBER_OF_RESULTS_FOUND);
  }

  private <T extends DomainEntity> void testCreate(Class<T> type, int start, int rows, final int normalizedStart, int normalizedRows, final List<String> idsToGet, List<T> result, String nextLink,
      String prevLink, int numberOfResultsFound) {
    when(entityRefCreatorMock.createRefs(type, result)).thenReturn(UNIMPORTANT_REF_LIST);

    RegularClientSearchResultMatcher clientSearchResultMatcher = newClientSearchResultMatcher() //
        .withTerm(TERM) //
        .withFacets(FACET_LIST) //
        .withNumFound(numberOfResultsFound) //
        .withIds(idsToGet) //
        .withRefs(UNIMPORTANT_REF_LIST) //
        .withResults(result) //
        .withStart(normalizedStart) //
        .withRows(normalizedRows) //
        .withSortableFields(SORTABLE_FIELDS) //
        .withNextLink(nextLink) //
        .withPrevLink(prevLink) //
        .build();

    // action
    RegularClientSearchResult clientSearchResult = instance.create(TYPE, searchResult, start, rows);

    // verify
    assertThat(clientSearchResult, clientSearchResultMatcher);
  }

  @Test
  public void testCreateWithSearchResultWithoutIds() throws InstantiationException, IllegalAccessException {
    final int numberOfResultsFound = 0;
    int start = 0;
    int rows = 10;

    final int normalizedStart = 0;
    final int normalizedRows = 0;
    final List<String> idsToGet = Lists.newArrayList();
    List<OtherDomainEntity> result = setupRepository(TYPE, idsToGet);

    when(entityRefCreatorMock.createRefs(TYPE, result)).thenReturn(UNIMPORTANT_REF_LIST);

    RegularClientSearchResultMatcher clientSearchResultMatcher = newClientSearchResultMatcher() //
        .withTerm(TERM) //
        .withFacets(FACET_LIST) //
        .withNumFound(numberOfResultsFound) //
        .withIds(idsToGet) //
        .withRefs(UNIMPORTANT_REF_LIST) //
        .withResults(result) //
        .withStart(normalizedStart) //
        .withRows(normalizedRows) //
        .withSortableFields(SORTABLE_FIELDS) //
        .withNextLink(nullNextLink) //
        .withPrevLink(nullPrevLink) //
        .build();

    SearchResult searchResult = new SearchResult();
    searchResult.setId(QUERY_ID);
    List<String> idList = Lists.newArrayList();
    searchResult.setIds(idList);
    searchResult.setTerm(TERM);
    searchResult.setFacets(FACET_LIST);

    // action
    RegularClientSearchResult clientSearchResult = instance.create(TYPE, searchResult, start, rows);

    // verify
    assertThat(clientSearchResult, clientSearchResultMatcher);
  }

  private <T extends DomainEntity> List<T> setupRepository(Class<T> type, List<String> idList) throws InstantiationException, IllegalAccessException {
    List<T> domainEntities = Lists.newArrayList();

    for (String id : idList) {

      final T domainEntity = type.newInstance();
      domainEntities.add(domainEntity);
      doReturn(domainEntity).when(repositoryMock).getEntity(type, id);
    }

    return domainEntities;

  }

}
