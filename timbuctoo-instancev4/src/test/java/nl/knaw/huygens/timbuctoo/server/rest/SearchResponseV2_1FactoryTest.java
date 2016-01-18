package nl.knaw.huygens.timbuctoo.server.rest;

import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static nl.knaw.huygens.timbuctoo.server.rest.SearchResponseV2_1Matcher.likeSearchResponse;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;

public class SearchResponseV2_1FactoryTest {

  public static final WwPersonSearchDescription DESCRIPTION = new WwPersonSearchDescription();
  private SearchResponseV2_1Factory instance;
  private SearchResponseV2_1RefAdder refAdder;

  @Before
  public void setup() {
    refAdder = mock(SearchResponseV2_1RefAdder.class);
    instance = new SearchResponseV2_1Factory(refAdder);
  }

  @Test
  public void fromReturnsASearchResponseV2_1WithTheFullTextSearchFieldsOfTheSearchResult() {
    List<String> fullTextSearchFields = Lists.newArrayList("field1", "field2");
    ArrayList<EntityRef> refs = Lists.newArrayList();
    SearchResult searchResult = new SearchResult(refs, fullTextSearchFields, Lists.newArrayList());

    SearchResponseV2_1 searchResponse = instance.createResponse(searchResult, 10, 0);

    assertThat(searchResponse, is(likeSearchResponse().withFullTextSearchFields(fullTextSearchFields)));
  }

  @Test
  public void fromReturnsASearchResponseV2_1WithTheSortableFieldsOfTheSearchResult() {

    List<EntityRef> refs = Lists.newArrayList();
    List<String> fullTextSearchFields = Lists.newArrayList();
    List<String> sortableFields = Lists.newArrayList("field1", "field2");

    SearchResult searchResult = new SearchResult(refs, fullTextSearchFields, sortableFields);

    SearchResponseV2_1 searchResponse = instance.createResponse(searchResult, 10, 0);

    assertThat(searchResponse, is(likeSearchResponse().withSortableFields(sortableFields)));
  }

  @Test
  public void fromReturnsASearchResponseV2_1WithTheRefsFromTheSearchResult() {
    EntityRef entityRef = new EntityRef("type", "id");
    List<EntityRef> refs = Lists.newArrayList(entityRef);
    SearchResult searchResult = new SearchResult(refs, Lists.newArrayList(), Lists.newArrayList());

    SearchResponseV2_1 searchResponse = instance.createResponse(searchResult, 10, 0);

    verify(refAdder).addRef(searchResponse, entityRef);
  }

  @Test
  public void fromReturnsASearchResponseV2_1WithTheMaximumNumberOfRefsDescribed() {
    EntityRef entityRef1 = new EntityRef("type", "id");
    List<EntityRef> refs = Lists.newArrayList(entityRef1, new EntityRef("type", "id2"));
    SearchResult searchResult = new SearchResult(refs, Lists.newArrayList(), Lists.newArrayList());

    SearchResponseV2_1 searchResponse = instance.createResponse(searchResult, 1, 0);

    verify(refAdder).addRef(searchResponse, entityRef1);
    verifyNoMoreInteractions(refAdder);
  }

  @Test
  public void fromReturnsASearchResponseV2_1WithTheWithAllTheResultsIfTheRowsIsLargerThanTheNumberOfRefs() {
    EntityRef entityRef1 = new EntityRef("type", "id");
    EntityRef entityRef2 = new EntityRef("type", "id2");
    List<EntityRef> refs = Lists.newArrayList(entityRef1, entityRef2);
    SearchResult searchResult = new SearchResult(refs, Lists.newArrayList(), Lists.newArrayList());


    SearchResponseV2_1 searchResponse = instance.createResponse(searchResult, 10, 0);

    verify(refAdder).addRef(searchResponse, entityRef1);
    verify(refAdder).addRef(searchResponse, entityRef2);
    verifyNoMoreInteractions(refAdder);
  }

  @Test
  public void fromSkipsTheNumberOfRefsDefinedInStart() {
    EntityRef entityRef2 = new EntityRef("type", "id2");
    List<EntityRef> refs = Lists.newArrayList(new EntityRef("type", "id"), entityRef2);
    SearchResult searchResult = new SearchResult(refs, Lists.newArrayList(), Lists.newArrayList());

    SearchResponseV2_1 searchResponse = instance.createResponse(searchResult, 1, 1);

    verify(refAdder).addRef(searchResponse, entityRef2);
    verifyNoMoreInteractions(refAdder);
  }

  @Test
  public void fromSetsTheStartWithTheStartParameter() {
    SearchResult searchResult = new SearchResult(Lists.newArrayList(new EntityRef("type", "id")), Lists.newArrayList(),
      Lists.newArrayList());
    int start = 1;

    SearchResponseV2_1 searchResponse = instance.createResponse(searchResult, 1, start);

    assertThat(searchResponse, is(likeSearchResponse().withStart(start)));
  }

  @Test
  public void fromSetsTheRowsWithTheNumberOfRefsInTheResult() {
    SearchResult searchResult = new SearchResult(Lists.newArrayList(new EntityRef("type", "id")), Lists.newArrayList(),
      Lists.newArrayList());
    int rows = 2;

    SearchResponseV2_1 searchResponse = instance.createResponse(searchResult, rows, 0);

    int numberOfRefs = searchResponse.getRefs().size();
    assertThat(searchResponse, is(likeSearchResponse().withRows(numberOfRefs)));
  }

  @Test
  public void fromReturnsASearchResponseWithoutRefsWhenTheSearchResultDoesNotContainARef() {
    SearchResult searchResult = new SearchResult(Lists.newArrayList(), Lists.newArrayList(), Lists.newArrayList());

    SearchResponseV2_1 searchResponse = instance.createResponse(searchResult, 2, 0);

    assertThat(searchResponse, is(likeSearchResponse().withRows(0)));
    verifyZeroInteractions(refAdder);
  }

  @Test
  public void fromReturnsASearchResponseWithoutRefsWhenTheStartIsLargerThanTheNumberOfRows() {
    SearchResult searchResult = new SearchResult(Lists.newArrayList(new EntityRef("type", "id")), Lists.newArrayList(),
      Lists.newArrayList());

    SearchResponseV2_1 searchResponse = instance.createResponse(searchResult, 2, 2);

    assertThat(searchResponse, is(likeSearchResponse().withRows(0)));
    verifyZeroInteractions(refAdder);
  }

}
