package nl.knaw.huygens.timbuctoo.rest.util.search;

/*
 * #%L
 * Timbuctoo REST api
 * =======
 * Copyright (C) 2012 - 2015 Huygens ING
 * =======
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either NULL_VERSION 3 of the
 * License, or (at your option) any later NULL_VERSION.
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
import nl.knaw.huygens.timbuctoo.model.RelationDTO;
import nl.knaw.huygens.timbuctoo.model.SearchResult;
import org.junit.Before;
import org.junit.Test;
import test.rest.model.TestRelation;

import java.util.List;

import static nl.knaw.huygens.timbuctoo.rest.util.search.RelationSearchResultDTOMatcher.likeRelationSearchResultDTO;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RelationSearchResultMapperTest extends SearchResultMapperTest {

  private static final String TYPE_STRING = "testrelation";
  private static final List<RelationDTO> REFS = Lists.newArrayList(new RelationDTO(TYPE_STRING, "xtype", "id", "relationName", null, null));
  public static final String VERSION = "version";

  private RelationSearchResultMapper instance;
  private RelationMapper relationMapperMock;

  @Before
  public void setup() {
    initializeRepository();
    initializeHATEOASURICreator();
    initilizeSortableFieldFinder();
    initializeVRECollection();

    relationMapperMock = mock(RelationMapper.class);
    instance = new RelationSearchResultMapper(repositoryMock, sortableFieldFinderMock, hateoasURICreatorMock, relationMapperMock, vreCollectionMock);
  }

  @Test
  public void testCreateStartIsZero() throws InstantiationException, IllegalAccessException {
    int start = 0;
    int rows = 10;

    Class<TestRelation> type = TestRelation.class;
    SearchResult searchResult = newSearchResult(QUERY_ID, VRE_ID);
    searchResult.setIds(ID_LIST_WITH_TEN_IDS);

    List<String> idsToGet = ID_LIST_WITH_TEN_IDS;
    List<TestRelation> result = setupRepository(type, idsToGet);
    RelationSearchResultDTOMatcher likeRelationClientResult = likeRelationSearchResultDTO()//
        .withIds(ID_LIST_WITH_TEN_IDS) //
        .withNumFound(TEN_RESULTS_FOUND) //
        .withRefs(REFS) //
        .withResults(result) //
        .withStart(start) //
        .withRows(rows) //
        .withSortableFields(SORTABLE_FIELDS);

    when(relationMapperMock.createRefs(vreMock, type, result)).thenReturn(REFS);

    testCreate(start, rows, type, searchResult, likeRelationClientResult, VERSION);
  }

  @Test
  public void testCreateStartIsGreaterThanZero() throws InstantiationException, IllegalAccessException {
    int start = 1;
    int previousStart = 0;
    int rows = 9;

    Class<TestRelation> type = TestRelation.class;
    SearchResult searchResult = newSearchResult(QUERY_ID, VRE_ID);
    searchResult.setIds(ID_LIST_WITH_TEN_IDS);

    List<String> idsToGet = ID_LIST_WITH_TEN_IDS.subList(1, 10);
    List<TestRelation> result = setupRepository(type, idsToGet);
    RelationSearchResultDTOMatcher likeRelationClientResult = likeRelationSearchResultDTO()//
        .withIds(ID_LIST_WITH_TEN_IDS) //
        .withNumFound(TEN_RESULTS_FOUND) //
        .withRefs(REFS) //
        .withResults(result) //
        .withStart(start) //
        .withRows(rows) //
        .withSortableFields(SORTABLE_FIELDS) //
        .withPrevLink(PREV_LINK) ;

    when(relationMapperMock.createRefs(vreMock, type, result)).thenReturn(REFS);
    when(hateoasURICreatorMock.createPrevResultsAsString(start, rows, QUERY_ID, VERSION)).thenReturn(PREV_LINK);

    testCreate(start, rows, type, searchResult, likeRelationClientResult, VERSION);
  }

  @Test
  public void testCreateStartIsSmallerThanZero() throws InstantiationException, IllegalAccessException {
    int start = -1;
    int normalizedStart = 0;
    int rows = 10;

    Class<TestRelation> type = TestRelation.class;
    SearchResult searchResult = newSearchResult(QUERY_ID, VRE_ID);
    searchResult.setIds(ID_LIST_WITH_TEN_IDS);

    List<String> idsToGet = ID_LIST_WITH_TEN_IDS;
    List<TestRelation> result = setupRepository(type, idsToGet);
    RelationSearchResultDTOMatcher likeRelationClientResult = likeRelationSearchResultDTO()
        .withIds(ID_LIST_WITH_TEN_IDS) //
        .withNumFound(TEN_RESULTS_FOUND) //
        .withRefs(REFS) //
        .withResults(result) //
        .withStart(normalizedStart) //
        .withRows(rows) //
        .withSortableFields(SORTABLE_FIELDS);

    when(relationMapperMock.createRefs(vreMock, type, result)).thenReturn(REFS);

    testCreate(start, rows, type, searchResult, likeRelationClientResult, VERSION);
  }

  @Test
  public void testCreateStartAndRowsIsSmallerThatMax() throws InstantiationException, IllegalAccessException {
    int start = 0;
    int nextStart = 5;
    int rows = 5;

    Class<TestRelation> type = TestRelation.class;
    SearchResult searchResult = newSearchResult(QUERY_ID, VRE_ID);
    searchResult.setIds(ID_LIST_WITH_TEN_IDS);

    List<String> idsToGet = ID_LIST_WITH_TEN_IDS.subList(0, 5);
    List<TestRelation> result = setupRepository(type, idsToGet);
    RelationSearchResultDTOMatcher likeRelationClientResult = likeRelationSearchResultDTO()//
        .withIds(ID_LIST_WITH_TEN_IDS) //
        .withNumFound(TEN_RESULTS_FOUND) //
        .withRefs(REFS) //
        .withResults(result) //
        .withStart(start) //
        .withRows(rows) //
        .withSortableFields(SORTABLE_FIELDS) //
        .withNextLink(NEXT_LINK);

    when(relationMapperMock.createRefs(vreMock, type, result)).thenReturn(REFS);
    when(hateoasURICreatorMock.createPrevResultsAsString(start, rows, QUERY_ID, VERSION)).thenReturn(PREV_LINK);
    when(hateoasURICreatorMock.createNextResultsAsString(start, rows, ID_LIST_WITH_TEN_IDS.size(), QUERY_ID, VERSION)).thenReturn(NEXT_LINK);

    testCreate(start, rows, type, searchResult, likeRelationClientResult, VERSION);
  }

  @Test
  public void testCreateWithRowsMoreThanMax() throws InstantiationException, IllegalAccessException {
    int start = 0;
    int rows = 15;
    int normalizedRows = 10;

    Class<TestRelation> type = TestRelation.class;
    SearchResult searchResult = newSearchResult(QUERY_ID, VRE_ID);
    searchResult.setIds(ID_LIST_WITH_TEN_IDS);

    List<String> idsToGet = ID_LIST_WITH_TEN_IDS;
    List<TestRelation> result = setupRepository(type, idsToGet);
    RelationSearchResultDTOMatcher likeRelationClientResult = likeRelationSearchResultDTO()//
        .withIds(idsToGet) //
        .withNumFound(TEN_RESULTS_FOUND) //
        .withRefs(REFS) //
        .withResults(result) //
        .withStart(start) //
        .withRows(normalizedRows) //
        .withSortableFields(SORTABLE_FIELDS);
    when(relationMapperMock.createRefs(vreMock, type, result)).thenReturn(REFS);

    testCreate(start, rows, type, searchResult, likeRelationClientResult, VERSION);
  }

  @Test
  public void testCreateStartGreaterThanNumFound() throws InstantiationException, IllegalAccessException {
    int start = 11;
    int rows = 10;
    int normalizedStart = 10;
    int normalizedRows = 0;

    Class<TestRelation> type = TestRelation.class;
    SearchResult searchResult = newSearchResult(QUERY_ID, VRE_ID);
    searchResult.setIds(ID_LIST_WITH_TEN_IDS);

    List<String> idsToGet = ID_LIST_WITH_TEN_IDS.subList(normalizedStart, 10);
    List<TestRelation> result = setupRepository(type, idsToGet);
    RelationSearchResultDTOMatcher likeRelationClientResult = likeRelationSearchResultDTO()//
        .withIds(ID_LIST_WITH_TEN_IDS) //
        .withNumFound(TEN_RESULTS_FOUND) //
        .withRefs(REFS) //
        .withResults(result) //
        .withStart(normalizedStart) //
        .withRows(normalizedRows) //
        .withSortableFields(SORTABLE_FIELDS) //
        .withPrevLink(PREV_LINK);

    int prevStart = 0;
    when(relationMapperMock.createRefs(vreMock, type, result)).thenReturn(REFS);
    when(hateoasURICreatorMock.createPrevResultsAsString(normalizedStart, rows, QUERY_ID, VERSION)).thenReturn(PREV_LINK);

    testCreate(start, rows, type, searchResult, likeRelationClientResult, VERSION);
  }

  @Test
  public void testCreateWithSearchResultWithoutIds() throws InstantiationException, IllegalAccessException {
    int start = 0;
    int rows = 0;

    Class<TestRelation> type = TestRelation.class;
    SearchResult searchResult = newSearchResult(QUERY_ID, VRE_ID);

    List<String> idsToGet = Lists.newArrayList();
    List<TestRelation> result = setupRepository(type, idsToGet);
    List<RelationDTO> emptyRefList = Lists.newArrayList();

    RelationSearchResultDTOMatcher likeRelationClientResult = likeRelationSearchResultDTO()
        .withIds(idsToGet) //
        .withNumFound(0) //
        .withRefs(emptyRefList) //
        .withResults(result) //
        .withStart(start) //
        .withRows(rows) //
        .withSortableFields(SORTABLE_FIELDS);

    testCreate(start, rows, type, searchResult, likeRelationClientResult, VERSION);
  }

  private SearchResult newSearchResult(String id, String vreId) {
    SearchResult result = new SearchResult();
    result.setId(id);
    result.setVreId(vreId);
    return result;
  }

  private void testCreate(int start, int rows, Class<TestRelation> type, SearchResult searchResult, RelationSearchResultDTOMatcher likeRelationClientResult, String version) {
    assertThat(instance.create(type, searchResult, start, rows, version), likeRelationClientResult);
  }

}
