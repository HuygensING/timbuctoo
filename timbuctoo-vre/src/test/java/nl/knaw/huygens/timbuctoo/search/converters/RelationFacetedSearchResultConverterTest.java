package nl.knaw.huygens.timbuctoo.search.converters;

import static nl.knaw.huygens.timbuctoo.search.converters.SearchResultMatcher.likeRelationSearchResult;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.List;

import nl.knaw.huygens.timbuctoo.model.SearchResult;

import org.junit.Test;

import com.google.common.collect.Lists;

public class RelationFacetedSearchResultConverterTest extends FacetedSearchResultConverterTestBase {
  private List<String> targetIds;
  private List<String> sourceIds;

  @Test
  public void testConvert() {
    // setup
    targetIds = Lists.newArrayList("id1");
    sourceIds = Lists.newArrayList("id2");
    FacetedSearchResultConverter instance = new RelationFacetedSearchResultConverter(sourceIds, targetIds);

    // action
    SearchResult actualSearchResult = instance.convert(TYPE_STRING, facetedSearchResult);

    // verify
    assertThat(actualSearchResult, likeRelationSearchResult(TYPE_STRING, IDS, SEARCH_TERM, SORT, FACETS, sourceIds, targetIds));
    //verifySearchResult(expectedSearchResult, actualSearchResult);
  }
}
