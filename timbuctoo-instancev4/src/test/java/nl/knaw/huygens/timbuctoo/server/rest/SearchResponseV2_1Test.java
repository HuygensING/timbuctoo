package nl.knaw.huygens.timbuctoo.server.rest;

import com.google.common.collect.Lists;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static nl.knaw.huygens.timbuctoo.server.rest.SearchResponseV2_1Matcher.likeSearchResponse;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class SearchResponseV2_1Test {

  public static final WwPersonSearchDescription DESCRIPTION = new WwPersonSearchDescription();

  @Test
  public void fromReturnsASearchResponseV2_1WithTheFullTextSearchFieldsOfTheSearchResult() {
    List<String> fullTextSearchFields = Lists.newArrayList("field1", "field2");
    ArrayList<EntityRef> refs = Lists.newArrayList();
    SearchResult searchResult = new SearchResult(refs, fullTextSearchFields, Lists.newArrayList());

    SearchResponseV2_1 searchResponse = SearchResponseV2_1.from(searchResult, 10, 0);

    assertThat(searchResponse, is(likeSearchResponse().withFullTextSearchFields(fullTextSearchFields)));
  }

  @Test
  public void fromReturnsASearchResponseV2_1WithTheSortableFieldsOfTheSearchResult() {

    List<EntityRef> refs = Lists.newArrayList();
    List<String> fullTextSearchFields = Lists.newArrayList();
    List<String> sortableFields = Lists.newArrayList("field1", "field2");

    SearchResult searchResult = new SearchResult(refs, fullTextSearchFields, sortableFields);

    SearchResponseV2_1 searchResponse = SearchResponseV2_1.from(searchResult, 10, 0);

    assertThat(searchResponse, is(likeSearchResponse().withSortableFields(sortableFields)));
  }

  @Test
  public void fromReturnsASearchResponseV2_1WithTheRefsFromTheSearchResult() {
    List<EntityRef> refs = Lists.newArrayList(new EntityRef("type", "id"));
    SearchResult searchResult = new SearchResult(refs, Lists.newArrayList(), Lists.newArrayList());

    SearchResponseV2_1 searchResponse = SearchResponseV2_1.from(searchResult, 10, 0);

    assertThat(searchResponse, is(likeSearchResponse().withRefs(refs)));
  }

  @Test
  public void fromReturnsASearchResponseV2_1WithTheMaximumNumberOfRefsDescribed() {
    List<EntityRef> refs = Lists.newArrayList(new EntityRef("type", "id"), new EntityRef("type", "id2"));
    SearchResult searchResult = new SearchResult(refs, Lists.newArrayList(), Lists.newArrayList());

    SearchResponseV2_1 searchResponse = SearchResponseV2_1.from(searchResult, 1, 0);

    assertThat(searchResponse, is(likeSearchResponse().withRefs(Lists.newArrayList(new EntityRef("type", "id")))));
  }

  @Test
  public void fromReturnsASearchResponseV2_1WithTheWithAllTheResultsIfTheRowsIsLargerThanTheNumberOfRefs() {
    List<EntityRef> refs = Lists.newArrayList(new EntityRef("type", "id"), new EntityRef("type", "id2"));
    SearchResult searchResult = new SearchResult(refs, Lists.newArrayList(), Lists.newArrayList());


    SearchResponseV2_1 searchResponse = SearchResponseV2_1.from(searchResult, 10, 0);

    assertThat(searchResponse, is(likeSearchResponse().withRefs(refs)));
  }

  @Test
  public void fromSkipsTheNumberOfRefsDefinedInStart() {
    List<EntityRef> refs = Lists.newArrayList(new EntityRef("type", "id"), new EntityRef("type", "id2"));
    SearchResult searchResult = new SearchResult(refs, Lists.newArrayList(), Lists.newArrayList());

    SearchResponseV2_1 searchResponse = SearchResponseV2_1.from(searchResult, 1, 1);

    assertThat(searchResponse, is(likeSearchResponse().withRefs(Lists.newArrayList(new EntityRef("type", "id2")))));
  }

  @Test
  public void fromSetsTheStartWithTheStartParameter() {
    SearchResult searchResult = new SearchResult(Lists.newArrayList(new EntityRef("type", "id")), Lists.newArrayList(),
      Lists.newArrayList());
    int start = 1;

    SearchResponseV2_1 searchResponse = SearchResponseV2_1.from(searchResult, 1, start);

    assertThat(searchResponse, is(likeSearchResponse().withStart(start)));
  }

  @Test
  public void fromSetsTheRowsWithTheNumberOfRefsInTheResult() {
    SearchResult searchResult = new SearchResult(Lists.newArrayList(new EntityRef("type", "id")), Lists.newArrayList(),
      Lists.newArrayList());
    int rows = 2;

    SearchResponseV2_1 searchResponse = SearchResponseV2_1.from(searchResult, rows, 0);

    int numberOfRefs = searchResponse.getRefs().size();
    assertThat(searchResponse, is(likeSearchResponse().withRows(numberOfRefs)));
  }

  @Test
  public void fromReturnsASearchResponseWithoutRefsWhenTheSearchResultDoesNotContainARef() {
    SearchResult searchResult = new SearchResult(Lists.newArrayList(), Lists.newArrayList(), Lists.newArrayList());

    SearchResponseV2_1 searchResponse = SearchResponseV2_1.from(searchResult, 2, 0);

    assertThat(searchResponse, is(likeSearchResponse().withRefs(Lists.newArrayList()).withRows(0)));
  }

  @Test
  public void fromReturnsASearchResponseWithoutRefsWhenTheStartIsLargerThanTheNumberOfRows() {
    SearchResult searchResult = new SearchResult(Lists.newArrayList(new EntityRef("type", "id")), Lists.newArrayList(),
      Lists.newArrayList());

    SearchResponseV2_1 searchResponse = SearchResponseV2_1.from(searchResult, 2, 2);

    assertThat(searchResponse, is(likeSearchResponse().withRefs(Lists.newArrayList()).withRows(0)));
  }

}
