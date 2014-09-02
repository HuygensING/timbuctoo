package nl.knaw.huygens.timbuctoo.rest.util.search;

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

import static nl.knaw.huygens.timbuctoo.rest.util.search.RegularClientSearchResultMatcher.newRegularClientSearchResultMatcherBuilder;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import nl.knaw.huygens.facetedsearch.model.Facet;
import nl.knaw.huygens.timbuctoo.model.ClientEntityRepresentation;
import nl.knaw.huygens.timbuctoo.model.RegularClientSearchResult;
import nl.knaw.huygens.timbuctoo.model.SearchResult;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import test.rest.model.projecta.OtherDomainEntity;

import com.google.common.collect.Lists;

public class RegularClientSearchResultCreatorTest extends ClientSearchResultCreatorTest {

  private static final ArrayList<ClientEntityRepresentation> UNIMPORTANT_REF_LIST = Lists.newArrayList(new ClientEntityRepresentation("test", "test", "test", "test"));
  private static final Class<OtherDomainEntity> TYPE = OtherDomainEntity.class;
  private static final String TERM = "term";
  private static final ArrayList<Facet> FACET_LIST = Lists.newArrayList();

  private ClientEntityRepresentationCreator entityRefCreatorMock;
  private SearchResult defaultSearchResult;
  private RegularClientSearchResultCreator instance;

  @Before
  public void setUp() {
    initializeRepository();
    initializeHATEOASURICreator();
    initilizeSortableFieldFinder();

    entityRefCreatorMock = mock(ClientEntityRepresentationCreator.class);
    when(sortableFieldFinderMock.findFields(TYPE)).thenReturn(SORTABLE_FIELDS);

    instance = new RegularClientSearchResultCreator(repositoryMock, sortableFieldFinderMock, entityRefCreatorMock, hateoasURICreatorMock);

    defaultSearchResult = new SearchResult();
    defaultSearchResult.setId(QUERY_ID);
    defaultSearchResult.setIds(ID_LIST_WITH_TEN_IDS);
    defaultSearchResult.setTerm(TERM);
    defaultSearchResult.setFacets(FACET_LIST);
  }

  @Ignore
  @Test
  public void testCreateStartIsZero() throws InstantiationException, IllegalAccessException {
    int start = 0;
    int rows = 10;
    int normalizedStart = 0;
    int normalizedRows = 10;
    List<OtherDomainEntity> result = setupRepository(TYPE, ID_LIST_WITH_TEN_IDS);

    ClientSearchResultMatcher<RegularClientSearchResult> clientSearchResultMatcher = newRegularClientSearchResultMatcherBuilder() //
        .withTerm(TERM) //
        .withFacets(FACET_LIST) //
        .withNumFound(TEN_RESULTS_FOUND) //
        .withIds(ID_LIST_WITH_TEN_IDS) //
        .withRefs(UNIMPORTANT_REF_LIST) //
        .withResults(result) //
        .withStart(normalizedStart) //
        .withRows(normalizedRows) //
        .withSortableFields(SORTABLE_FIELDS) //
        .withNextLink(NULL_NEXT_LINK) //
        .withPrevLink(NULL_PREV_LINK) //
        .build();

    when(entityRefCreatorMock.createRefs(TYPE, result)).thenReturn(UNIMPORTANT_REF_LIST);

    assertThat(instance.create(TYPE, defaultSearchResult, start, rows), clientSearchResultMatcher);
  }

  @Ignore
  @Test
  public void testCreateStartIsGreaterThanZero() throws InstantiationException, IllegalAccessException {
    int start = 1;
    int rows = 9;
    int normalizedRows = 9;
    int normalizedStart = 1;

    final List<String> idsToGet = ID_LIST_WITH_TEN_IDS.subList(normalizedStart, 10);
    List<OtherDomainEntity> result = setupRepository(TYPE, idsToGet);

    final int previousStart = 0;
    when(hateoasURICreatorMock.createHATEOASURIAsString(previousStart, rows, QUERY_ID)).thenReturn(PREV_LINK);

    ClientSearchResultMatcher<RegularClientSearchResult> clientSearchResultMatcher = newRegularClientSearchResultMatcherBuilder() //
        .withTerm(TERM) //
        .withFacets(FACET_LIST) //
        .withNumFound(TEN_RESULTS_FOUND) //
        .withIds(idsToGet) //
        .withRefs(UNIMPORTANT_REF_LIST) //
        .withResults(result) //
        .withStart(normalizedStart) //
        .withRows(normalizedRows) //
        .withSortableFields(SORTABLE_FIELDS) //
        .withNextLink(NULL_NEXT_LINK) //
        .withPrevLink(PREV_LINK) //
        .build();

    when(entityRefCreatorMock.createRefs(TYPE, result)).thenReturn(UNIMPORTANT_REF_LIST);

    assertThat(instance.create(TYPE, defaultSearchResult, start, rows), clientSearchResultMatcher);
  }

  @Ignore
  @Test
  public void testCreateStartIsSmallerThanZero() throws InstantiationException, IllegalAccessException {
    int start = -1;
    int rows = 10;
    final int normalizedStart = 0;
    int normalizedRows = 10;
    final List<String> idsToGet = ID_LIST_WITH_TEN_IDS.subList(normalizedStart, 10);

    List<OtherDomainEntity> result = setupRepository(TYPE, idsToGet);

    ClientSearchResultMatcher<RegularClientSearchResult> clientSearchResultMatcher = newRegularClientSearchResultMatcherBuilder() //
        .withTerm(TERM) //
        .withFacets(FACET_LIST) //
        .withNumFound(TEN_RESULTS_FOUND) //
        .withIds(idsToGet) //
        .withRefs(UNIMPORTANT_REF_LIST) //
        .withResults(result) //
        .withStart(normalizedStart) //
        .withRows(normalizedRows) //
        .withSortableFields(SORTABLE_FIELDS) //
        .withNextLink(NULL_NEXT_LINK) //
        .withPrevLink(NULL_PREV_LINK) //
        .build();

    when(entityRefCreatorMock.createRefs(TYPE, result)).thenReturn(UNIMPORTANT_REF_LIST);

    assertThat(instance.create(TYPE, defaultSearchResult, start, rows), clientSearchResultMatcher);
  }

  @Ignore
  @Test
  public void testCreateStartAndRowsIsSmallerThatMax() throws InstantiationException, IllegalAccessException {
    int start = 0;
    int rows = 5;
    final int normalizedStart = 0;
    int normalizedRows = 5;

    final List<String> idsToGet = ID_LIST_WITH_TEN_IDS.subList(normalizedStart, 5);
    List<OtherDomainEntity> result = setupRepository(TYPE, idsToGet);

    int nextStart = 5;
    when(hateoasURICreatorMock.createHATEOASURIAsString(nextStart, rows, QUERY_ID)).thenReturn(NEXT_LINK);

    ClientSearchResultMatcher<RegularClientSearchResult> clientSearchResultMatcher = newRegularClientSearchResultMatcherBuilder() //
        .withTerm(TERM) //
        .withFacets(FACET_LIST) //
        .withNumFound(TEN_RESULTS_FOUND) //
        .withIds(idsToGet) //
        .withRefs(UNIMPORTANT_REF_LIST) //
        .withResults(result) //
        .withStart(normalizedStart) //
        .withRows(normalizedRows) //
        .withSortableFields(SORTABLE_FIELDS) //
        .withNextLink(NEXT_LINK) //
        .withPrevLink(NULL_PREV_LINK) //
        .build();

    when(entityRefCreatorMock.createRefs(TYPE, result)).thenReturn(UNIMPORTANT_REF_LIST);

    assertThat(instance.create(TYPE, defaultSearchResult, start, rows), clientSearchResultMatcher);
  }

  @Ignore
  @Test
  public void testCreateWithRowsMoreThanMax() throws InstantiationException, IllegalAccessException {
    int start = 0;
    int rows = 20;

    final int normalizedStart = 0;
    final int normalizedRows = 10;
    final List<String> idsToGet = ID_LIST_WITH_TEN_IDS.subList(normalizedStart, 10);
    List<OtherDomainEntity> result = setupRepository(TYPE, idsToGet);

    ClientSearchResultMatcher<RegularClientSearchResult> clientSearchResultMatcher = newRegularClientSearchResultMatcherBuilder() //
        .withTerm(TERM) //
        .withFacets(FACET_LIST) //
        .withNumFound(TEN_RESULTS_FOUND) //
        .withIds(idsToGet) //
        .withRefs(UNIMPORTANT_REF_LIST) //
        .withResults(result) //
        .withStart(normalizedStart) //
        .withRows(normalizedRows) //
        .withSortableFields(SORTABLE_FIELDS) //
        .withNextLink(NULL_NEXT_LINK) //
        .withPrevLink(NULL_PREV_LINK) //
        .build();

    when(entityRefCreatorMock.createRefs(TYPE, result)).thenReturn(UNIMPORTANT_REF_LIST);

    assertThat(instance.create(TYPE, defaultSearchResult, start, rows), clientSearchResultMatcher);
  }

  @Test
  public void testCreateStartIsGreaterThanNumFound() throws InstantiationException, IllegalAccessException {
    int start = 11;
    int rows = 10;
    final int normalizedStart = 10;
    int normalizedRows = 0;

    final List<String> idsToGet = Lists.newArrayList();
    List<OtherDomainEntity> result = setupRepository(TYPE, idsToGet);

    int prevStart = 0;
    when(hateoasURICreatorMock.createHATEOASURIAsString(prevStart, rows, QUERY_ID)).thenReturn(PREV_LINK);

    ClientSearchResultMatcher<RegularClientSearchResult> clientSearchResultMatcher = newRegularClientSearchResultMatcherBuilder() //
        .withTerm(TERM) //
        .withFacets(FACET_LIST) //
        .withNumFound(TEN_RESULTS_FOUND) //
        .withIds(idsToGet) //
        .withRefs(UNIMPORTANT_REF_LIST) //
        .withResults(result) //
        .withStart(normalizedStart) //
        .withRows(normalizedRows) //
        .withSortableFields(SORTABLE_FIELDS) //
        .withNextLink(NULL_NEXT_LINK) //
        .withPrevLink(PREV_LINK) //
        .build();

    when(entityRefCreatorMock.createRefs(TYPE, result)).thenReturn(UNIMPORTANT_REF_LIST);

    assertThat(instance.create(TYPE, defaultSearchResult, start, rows), clientSearchResultMatcher);
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

    final ArrayList<ClientEntityRepresentation> emptyRefList = Lists.newArrayList();
    when(entityRefCreatorMock.createRefs(TYPE, result)).thenReturn(emptyRefList);

    ClientSearchResultMatcher<RegularClientSearchResult> clientSearchResultMatcher = newRegularClientSearchResultMatcherBuilder() //
        .withTerm(TERM) //
        .withFacets(FACET_LIST) //
        .withNumFound(numberOfResultsFound) //
        .withIds(idsToGet) //
        .withRefs(emptyRefList) //
        .withResults(result) //
        .withStart(normalizedStart) //
        .withRows(normalizedRows) //
        .withSortableFields(SORTABLE_FIELDS) //
        .withNextLink(NULL_NEXT_LINK) //
        .withPrevLink(NULL_PREV_LINK) //
        .build();

    SearchResult emptySearchResult = new SearchResult();
    emptySearchResult.setId(QUERY_ID);
    emptySearchResult.setTerm(TERM);
    emptySearchResult.setFacets(FACET_LIST);

    assertThat(instance.create(TYPE, emptySearchResult, start, rows), clientSearchResultMatcher);
  }

}
