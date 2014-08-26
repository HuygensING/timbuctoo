package nl.knaw.huygens.timbuctoo.search;

/*
 * #%L
 * Timbuctoo VRE
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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.List;
import java.util.Map;

import nl.knaw.huygens.facetedsearch.model.FacetedSearchResult;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class RelationSearchResultFilterTest {

  @Mock
  private FilterableSet<Map<String, Object>> filterableSetMock;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void testProcess() {
    // setup
    List<String> sourceSearchIds = Lists.newArrayList();
    List<String> targetSearchIds = Lists.newArrayList();
    CollectionConverter collectionConverterMock = mock(CollectionConverter.class);

    List<Map<String, Object>> rawResults = Lists.newArrayList();

    FacetedSearchResult searchResult = createFacetedSearchResult(rawResults);

    HashSet<Map<String, Object>> filteredRawResults = setUpFilteredResult(collectionConverterMock, rawResults);

    RelationFacetedSearchResultFilter instance = new RelationFacetedSearchResultFilter(collectionConverterMock, sourceSearchIds, targetSearchIds);

    // action
    instance.process(searchResult);

    // verify
    verify(filterableSetMock).filter(Mockito.<Predicate<Map<String, Object>>> any());
    verify(searchResult).setRawResults(Lists.newArrayList(filteredRawResults));

  }

  @Test
  public void testProcessWhenSourceIdsAreNull() {
    // setup
    List<String> sourceSearchIds = null;
    List<String> targetSearchIds = Lists.newArrayList();
    CollectionConverter collectionConverterMock = mock(CollectionConverter.class);

    List<Map<String, Object>> rawResults = Lists.newArrayList();

    FacetedSearchResult searchResult = createFacetedSearchResult(rawResults);

    HashSet<Map<String, Object>> filteredRawResults = setUpFilteredResult(collectionConverterMock, rawResults);

    RelationFacetedSearchResultFilter instance = new RelationFacetedSearchResultFilter(collectionConverterMock, sourceSearchIds, targetSearchIds);

    // action
    instance.process(searchResult);

    // verify
    verify(filterableSetMock).filter(Mockito.<Predicate<Map<String, Object>>> any());
    verify(searchResult).setRawResults(Lists.newArrayList(filteredRawResults));

  }

  @Test
  public void testProcessWhenTargetIdsAreNull() {
    // setup
    List<String> sourceSearchIds = Lists.newArrayList();
    List<String> targetSearchIds = null;
    CollectionConverter collectionConverterMock = mock(CollectionConverter.class);

    List<Map<String, Object>> rawResults = Lists.newArrayList();

    FacetedSearchResult searchResult = createFacetedSearchResult(rawResults);

    HashSet<Map<String, Object>> filteredRawResults = setUpFilteredResult(collectionConverterMock, rawResults);

    RelationFacetedSearchResultFilter instance = new RelationFacetedSearchResultFilter(collectionConverterMock, sourceSearchIds, targetSearchIds);

    // action
    instance.process(searchResult);

    // verify
    verify(filterableSetMock).filter(Mockito.<Predicate<Map<String, Object>>> any());
    verify(searchResult).setRawResults(Lists.newArrayList(filteredRawResults));
  }

  private FacetedSearchResult createFacetedSearchResult(List<Map<String, Object>> rawResults) {
    FacetedSearchResult searchResult = mock(FacetedSearchResult.class);
    when(searchResult.getRawResults()).thenReturn(rawResults);
    return searchResult;
  }

  private HashSet<Map<String, Object>> setUpFilteredResult(CollectionConverter collectionConverterMock, List<Map<String, Object>> rawResults) {
    HashSet<Map<String, Object>> filteredRawResults = Sets.<Map<String, Object>> newHashSet();

    when(collectionConverterMock.toFilterableSet(rawResults)).thenReturn(filterableSetMock);
    when(filterableSetMock.filter(Mockito.<Predicate<Map<String, Object>>> any())).thenReturn(filteredRawResults);
    return filteredRawResults;
  }
}
