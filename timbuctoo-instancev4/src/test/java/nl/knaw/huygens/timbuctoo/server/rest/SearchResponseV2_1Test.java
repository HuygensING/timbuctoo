package nl.knaw.huygens.timbuctoo.server.rest;

import com.google.common.collect.Lists;
import org.junit.Test;

import java.util.List;

import static nl.knaw.huygens.timbuctoo.server.rest.SearchResponseV2_1Matcher.likeSearchResponse;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class SearchResponseV2_1Test {

  public static final WwPersonSearchDescription DESCRIPTION = new WwPersonSearchDescription();

  @Test
  public void fromReturnsASearchResponseV2_1WithTheFullTextSearchFieldsOfTheDescription() {
    SearchResult searchResult = new SearchResult(Lists.newArrayList());

    SearchResponseV2_1 searchResponse = SearchResponseV2_1.from(DESCRIPTION, searchResult, 10, 0);

    assertThat(searchResponse, is(likeSearchResponse()
      .withFullTextSearchFields(DESCRIPTION.getFullTextSearchFields())));
  }

  @Test
  public void fromReturnsASearchResponseV2_1WithTheSortableFieldsOfTheDescription() {
    SearchResult searchResult = new SearchResult(Lists.newArrayList());

    SearchResponseV2_1 searchResponse = SearchResponseV2_1.from(DESCRIPTION, searchResult, 10, 0);

    assertThat(searchResponse, is(likeSearchResponse()
      .withSortableFields(DESCRIPTION.getSortableFields())));
  }

  @Test
  public void fromReturnsASearchResponseV2_1WithTheRefsFromTheSearchResult() {
    List<EntityRef> refs = Lists.newArrayList(new EntityRef("type", "id"));
    SearchResult searchResult = new SearchResult(refs);

    SearchResponseV2_1 searchResponse = SearchResponseV2_1.from(DESCRIPTION, searchResult, 10, 0);

    assertThat(searchResponse, is(likeSearchResponse().withRefs(refs)));
  }

  @Test
  public void fromReturnsASearchResponseV2_1WithTheMaximumNumberOfRefsDescribed() {
    List<EntityRef> refs = Lists.newArrayList(new EntityRef("type", "id"), new EntityRef("type", "id2"));
    SearchResult searchResult = new SearchResult(refs);

    SearchResponseV2_1 searchResponse = SearchResponseV2_1.from(DESCRIPTION, searchResult, 1, 0);

    assertThat(searchResponse, is(likeSearchResponse().withRefs(Lists.newArrayList(new EntityRef("type", "id")))));
  }

  @Test
  public void fromReturnsASearchResponseV2_1WithTheWithAllTheResultsIfTheRowsIsLargerThanTheNumberOfRefs() {
    List<EntityRef> refs = Lists.newArrayList(new EntityRef("type", "id"), new EntityRef("type", "id2"));
    SearchResult searchResult = new SearchResult(refs);


    SearchResponseV2_1 searchResponse = SearchResponseV2_1.from(DESCRIPTION, searchResult, 10, 0);

    assertThat(searchResponse, is(likeSearchResponse().withRefs(refs)));
  }

  @Test
  public void fromSkipsTheNumberOfRefsDefinedInStart() {
    List<EntityRef> refs = Lists.newArrayList(new EntityRef("type", "id"), new EntityRef("type", "id2"));
    SearchResult searchResult = new SearchResult(refs);

    SearchResponseV2_1 searchResponse = SearchResponseV2_1.from(DESCRIPTION, searchResult, 1, 1);

    assertThat(searchResponse, is(likeSearchResponse().withRefs(Lists.newArrayList(new EntityRef("type", "id2")))));
  }

  @Test
  public void fromSetsTheStartWithTheStartParameter() {
    SearchResult searchResult = new SearchResult(Lists.newArrayList(new EntityRef("type", "id")));
    int start = 1;

    SearchResponseV2_1 searchResponse = SearchResponseV2_1.from(DESCRIPTION, searchResult, 1, start);

    assertThat(searchResponse, is(likeSearchResponse().withStart(start)));
  }

  @Test
  public void fromSetsTheRowsWithTheNumberOfRefsInTheResult() {
    SearchResult searchResult = new SearchResult(Lists.newArrayList(new EntityRef("type", "id")));
    int rows = 2;

    SearchResponseV2_1 searchResponse = SearchResponseV2_1.from(DESCRIPTION, searchResult, rows, 0);

    int numberOfRefs = searchResponse.getRefs().size();
    assertThat(searchResponse, is(likeSearchResponse().withRows(numberOfRefs)));
  }

  @Test
  public void fromReturnsASearchResponseWithoutRefsWhenTheSearchResultDoesNotContainARef() {
    SearchResult searchResult = new SearchResult(Lists.newArrayList());

    SearchResponseV2_1 searchResponse = SearchResponseV2_1.from(DESCRIPTION, searchResult, 2, 0);

    assertThat(searchResponse, is(likeSearchResponse().withRefs(Lists.newArrayList()).withRows(0)));
  }

  @Test
  public void fromReturnsASearchResponseWithoutRefsWhenTheStartIsLargerThanTheNumberOfRows() {
    SearchResult searchResult = new SearchResult(Lists.newArrayList(new EntityRef("type", "id")));

    SearchResponseV2_1 searchResponse = SearchResponseV2_1.from(DESCRIPTION, searchResult, 2, 2);

    assertThat(searchResponse, is(likeSearchResponse().withRefs(Lists.newArrayList()).withRows(0)));
  }

}
