package nl.knaw.huygens.timbuctoo.server.mediatypes.v2.search;

import com.google.common.collect.Lists;
import nl.knaw.huygens.timbuctoo.search.EntityRef;
import nl.knaw.huygens.timbuctoo.search.SearchResult;
import nl.knaw.huygens.timbuctoo.search.description.facet.Facet;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;

public class SearchResponseV2_1FactoryTest {

  private SearchResponseV2_1Factory instance;
  private SearchResponseV2_1RefAdder refAdder;
  private NavigationCreator navigationCreator;

  @Before
  public void setup() {
    refAdder = mock(SearchResponseV2_1RefAdder.class);
    navigationCreator = mock(NavigationCreator.class);
    instance = new SearchResponseV2_1Factory(refAdder, navigationCreator);
  }
/*
  @Test
  public void fromReturnsASearchResponseV2_1WithTheFullTextSearchFieldsOfTheSearchResult() {
    List<String> fullTextSearchFields = Lists.newArrayList("field1", "field2");
    ArrayList<Vertex> refs = Lists.newArrayList();
    SearchResult searchResult =
      new SearchResult(refs, fullTextSearchFields, Lists.newArrayList(), Lists.newArrayList());

    SearchResponseV2_1 searchResponse = instance.createResponse(searchResult, 10, 0);

    assertThat(searchResponse, hasProperty("fullTextSearchFields", equalTo(fullTextSearchFields)));
  }

  @Test
  public void fromReturnsASearchResponseV2_1WithTheSortableFieldsOfTheSearchResult() {
    List<EntityRef> refs = Lists.newArrayList();
    List<String> fullTextSearchFields = Lists.newArrayList();
    List<String> sortableFields = Lists.newArrayList("field1", "field2");
    SearchResult searchResult = new SearchResult(refs, fullTextSearchFields, sortableFields, Lists.newArrayList());

    SearchResponseV2_1 searchResponse = instance.createResponse(searchResult, 10, 0);

    assertThat(searchResponse, hasProperty("sortableFields", equalTo(sortableFields)));
  }

  @Test
  public void fromReturnsASearchResponseV2_1WithTheRefsFromTheSearchResult() {
    EntityRef entityRef = new EntityRef("type", "id");
    List<EntityRef> refs = Lists.newArrayList(entityRef);
    SearchResult searchResult =
      new SearchResult(refs, Lists.newArrayList(), Lists.newArrayList(), Lists.newArrayList());

    SearchResponseV2_1 searchResponse = instance.createResponse(searchResult, 10, 0);

    verify(refAdder).addRef(searchResponse, entityRef);
  }

  @Test
  public void fromReturnsASearchResponseV2_1WithTheMaximumNumberOfRefsDescribed() {
    EntityRef entityRef1 = new EntityRef("type", "id");
    List<EntityRef> refs = Lists.newArrayList(entityRef1, new EntityRef("type", "id2"));
    SearchResult searchResult =
      new SearchResult(refs, Lists.newArrayList(), Lists.newArrayList(), Lists.newArrayList());

    SearchResponseV2_1 searchResponse = instance.createResponse(searchResult, 1, 0);

    verify(refAdder).addRef(searchResponse, entityRef1);
    verifyNoMoreInteractions(refAdder);
  }

  @Test
  public void fromReturnsASearchResponseV2_1WithTheWithAllTheResultsIfTheRequestedRowsIsLargerThanTheNumberOfRefs() {
    EntityRef entityRef1 = new EntityRef("type", "id");
    EntityRef entityRef2 = new EntityRef("type", "id2");
    List<EntityRef> refs = Lists.newArrayList(entityRef1, entityRef2);
    SearchResult searchResult =
      new SearchResult(refs, Lists.newArrayList(), Lists.newArrayList(), Lists.newArrayList());

    SearchResponseV2_1 searchResponse = instance.createResponse(searchResult, 10, 0);

    verify(refAdder).addRef(searchResponse, entityRef1);
    verify(refAdder).addRef(searchResponse, entityRef2);
    verifyNoMoreInteractions(refAdder);
  }

  @Test
  public void fromSkipsTheNumberOfRefsDefinedInStart() {
    EntityRef entityRef2 = new EntityRef("type", "id2");
    List<EntityRef> refs = Lists.newArrayList(new EntityRef("type", "id"), entityRef2);
    SearchResult searchResult =
      new SearchResult(refs, Lists.newArrayList(), Lists.newArrayList(), Lists.newArrayList());

    SearchResponseV2_1 searchResponse = instance.createResponse(searchResult, 1, 1);

    verify(refAdder).addRef(searchResponse, entityRef2);
    verifyNoMoreInteractions(refAdder);
  }

  @Test
  public void fromSetsTheStartWithTheStartParameter() {
    SearchResult searchResult = new SearchResult(Lists.newArrayList(new EntityRef("type", "id")), Lists.newArrayList(),
      Lists.newArrayList(), Lists.newArrayList());
    int start = 1;

    SearchResponseV2_1 searchResponse = instance.createResponse(searchResult, 1, start);

    assertThat(searchResponse, hasProperty("start", equalTo(start)));
  }

  @Test
  public void fromReturnsASearchResponseWithoutRefsWhenTheSearchResultDoesNotContainARef() {
    SearchResult searchResult = new SearchResult(Lists.newArrayList(), Lists.newArrayList(), Lists.newArrayList(),
      Lists.newArrayList());

    SearchResponseV2_1 searchResponse = instance.createResponse(searchResult, 2, 0);

    assertThat(searchResponse, hasProperty("rows", equalTo(0)));
    verifyZeroInteractions(refAdder);
  }

  @Test
  public void fromReturnsASearchResponseWithoutRefsWhenTheStartIsLargerThanTheNumberOfRows() {
    SearchResult searchResult = new SearchResult(Lists.newArrayList(new EntityRef("type", "id")), Lists.newArrayList(),
      Lists.newArrayList(), Lists.newArrayList());

    SearchResponseV2_1 searchResponse = instance.createResponse(searchResult, 2, 2);

    assertThat(searchResponse, hasProperty("rows", equalTo(0)));
    verifyZeroInteractions(refAdder);
  }

  @Test
  public void fromReturnsASearchResponseWithTheFacets() {
    SearchResult searchResult = new SearchResult(Lists.newArrayList(), Lists.newArrayList(), Lists.newArrayList(),
      Lists.newArrayList(new Facet("name", Lists.newArrayList(), "LIST")));

    SearchResponseV2_1 response = instance.createResponse(searchResult, 2, 2);

    assertThat(response, hasProperty("facets", Matchers.contains(FacetMatcher.likeFacet().withName("name"))));
  }

  @Test
  public void fromLetsTheNavigationCreatorCreateANextLink() {
    EntityRef entityRef1 = new EntityRef("type", "id");
    List<EntityRef> refs = Lists.newArrayList(entityRef1, new EntityRef("type", "id2"));
    SearchResult searchResult =
      new SearchResult(refs, Lists.newArrayList(), Lists.newArrayList(), Lists.newArrayList());
    UUID resultId = UUID.randomUUID();
    searchResult.setId(resultId);
    int rows = 1;
    int start = 0;

    SearchResponseV2_1 searchResponse = instance.createResponse(searchResult, rows, start);

    verify(navigationCreator).next(searchResponse, rows, start, refs.size(), resultId);
  }

  @Test
  public void fromLetsTheNavigationCreatorCreateAPrevLink() {
    EntityRef entityRef1 = new EntityRef("type", "id");
    List<EntityRef> refs = Lists.newArrayList(entityRef1, new EntityRef("type", "id2"));
    SearchResult searchResult =
      new SearchResult(refs, Lists.newArrayList(), Lists.newArrayList(), Lists.newArrayList());
    UUID resultId = UUID.randomUUID();
    searchResult.setId(resultId);
    int rows = 1;
    int start = 0;

    SearchResponseV2_1 searchResponse = instance.createResponse(searchResult, rows, start);

    verify(navigationCreator).prev(searchResponse, rows, start, refs.size(), resultId);
  }

  @Test
  public void fromReturnsASearchResponseWithNumFoundEqualToTheNumberOfRefs() {
    List<EntityRef> refs = Lists.newArrayList(new EntityRef("type", "id"), new EntityRef("type", "id2"));
    SearchResult searchResult =
      new SearchResult(refs, Lists.newArrayList(), Lists.newArrayList(), Lists.newArrayList());

    SearchResponseV2_1 searchResponse = instance.createResponse(searchResult, 1, 0);

    assertThat(searchResponse, hasProperty("numFound", equalTo(refs.size())));
  }*/

}
