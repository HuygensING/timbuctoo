package nl.knaw.huygens.timbuctoo.search;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;
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

    FacetedSearchResult searchResult = mock(FacetedSearchResult.class);
    when(searchResult.getRawResults()).thenReturn(rawResults);

    HashSet<Map<String, Object>> filteredRawResults = Sets.<Map<String, Object>> newHashSet();

    when(collectionConverterMock.toFilterableSet(rawResults)).thenReturn(filterableSetMock);
    when(filterableSetMock.filter(Mockito.<Predicate<Map<String, Object>>> any())).thenReturn(filteredRawResults);

    RelationFacetedSearchResultFilter instance = new RelationFacetedSearchResultFilter(collectionConverterMock, sourceSearchIds, targetSearchIds);

    // action
    FacetedSearchResult actualSearchResult = instance.process(searchResult);

    // verify
    verify(filterableSetMock).filter(Mockito.<Predicate<Map<String, Object>>> any());
    verify(searchResult).setRawResults(Lists.newArrayList(filteredRawResults));
    assertThat(actualSearchResult, notNullValue(FacetedSearchResult.class));

  }
}
