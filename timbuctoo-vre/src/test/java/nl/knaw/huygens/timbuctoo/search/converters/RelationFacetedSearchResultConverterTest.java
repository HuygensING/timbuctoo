package nl.knaw.huygens.timbuctoo.search.converters;

import static nl.knaw.huygens.timbuctoo.search.converters.SearchResultMatcher.likeRelationSearchResult;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.List;

import nl.knaw.huygens.timbuctoo.model.SearchResult;

import org.junit.Test;

import com.google.common.collect.Lists;

public class RelationFacetedSearchResultConverterTest extends FacetedSearchResultConverterTestBase {

  @Test
  public void testConvert() {
    // setup
    List<String> targetIds = Lists.newArrayList("id1");
    List<String> sourceIds = Lists.newArrayList("id2");
    List<String> relationTypeIds = Lists.newArrayList("id3");
    FacetedSearchResultConverter instance = new RelationFacetedSearchResultConverter(sourceIds, targetIds, relationTypeIds);

    // action
    SearchResult actualSearchResult = instance.convert(TYPE_STRING, facetedSearchResult);

    // verify
    assertThat(actualSearchResult, likeRelationSearchResult(TYPE_STRING, IDS, SEARCH_TERM, SORT, FACETS, sourceIds, targetIds, relationTypeIds));
  }
}
