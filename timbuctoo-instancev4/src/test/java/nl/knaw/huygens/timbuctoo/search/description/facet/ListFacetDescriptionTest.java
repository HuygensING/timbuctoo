package nl.knaw.huygens.timbuctoo.search.description.facet;

import com.google.common.collect.Lists;
import nl.knaw.huygens.timbuctoo.search.FacetValue;
import nl.knaw.huygens.timbuctoo.search.VertexMatcher;
import nl.knaw.huygens.timbuctoo.search.description.PropertyParser;
import nl.knaw.huygens.timbuctoo.server.mediatypes.v2.search.ListFacetValue;
import nl.knaw.huygens.timbuctoo.server.mediatypes.v2.search.SearchRequestV2_1;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static nl.knaw.huygens.timbuctoo.util.TestGraphBuilder.newGraph;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.core.Is.is;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class ListFacetDescriptionTest {

  public static final String FACET_NAME = "facetName";
  public static final String PROPERTY = "property";
  private PropertyParser propertyParser;
  private ListFacetDescription instance;

  @Before
  public void setUp() throws Exception {
    propertyParser = mock(PropertyParser.class);
    given(propertyParser.parse(anyString())).willAnswer(invocation -> invocation.getArguments()[0]);
    instance = new ListFacetDescription(FACET_NAME, PROPERTY, propertyParser);
  }

  @Test
  public void getFacetReturnsTheFacetWithItsNameAndTheTypeList() {
    Graph graph = newGraph().build();

    Facet facet = instance.getFacet(graph.traversal().V());

    assertThat(facet, allOf(
      hasProperty("name", equalTo(FACET_NAME)),
      hasProperty("type", equalTo("LIST"))));
  }

  @Test
  public void getFacetReturnsAnEmptyListOfCountsWhenNotVerticesContainTheProperty() {
    Graph graph = newGraph().withVertex(v -> v.withTimId("id")).withVertex(v -> v.withTimId("id1")).build();

    Facet facet = instance.getFacet(graph.traversal().V());

    assertThat(facet.getOptions(), is(empty()));
  }

  @Test
  public void getFacetReturnsTheFacetWithItsCounts() {
    String value = "value";
    String value1 = "value1";
    String value2 = "value2";
    Graph graph = newGraph()
      .withVertex(v -> v.withTimId("id").withProperty(PROPERTY, value))
      .withVertex(v -> v.withTimId("id1").withProperty(PROPERTY, value))
      .withVertex(v -> v.withTimId("id2").withProperty(PROPERTY, value1))
      .withVertex(v -> v.withTimId("id3").withProperty(PROPERTY, value1))
      .withVertex(v -> v.withTimId("id4").withProperty(PROPERTY, value1))
      .withVertex(v -> v.withTimId("id5").withProperty(PROPERTY, value2))
      .build();

    Facet facet = instance.getFacet(graph.traversal().V());

    // L is needed because the counts are longs
    assertThat(facet.getOptions(), containsInAnyOrder(
      new Facet.DefaultOption(value, 2L),
      new Facet.DefaultOption(value1, 3L),
      new Facet.DefaultOption(value2, 1L)));
  }

  @Test
  public void getFacetLetsTheParserParseTheValue() {
    given(propertyParser.parse(anyString())).willAnswer(invocation -> invocation.getArguments()[0]);
    String value = "value";
    String value1 = "value1";
    String value2 = "value2";
    Graph graph = newGraph()
      .withVertex(v -> v.withTimId("id").withProperty(PROPERTY, value))
      .withVertex(v -> v.withTimId("id1").withProperty(PROPERTY, value))
      .withVertex(v -> v.withTimId("id2").withProperty(PROPERTY, value1))
      .withVertex(v -> v.withTimId("id3").withProperty(PROPERTY, value1))
      .withVertex(v -> v.withTimId("id4").withProperty(PROPERTY, value1))
      .withVertex(v -> v.withTimId("id5").withProperty(PROPERTY, value2))
      .build();

    instance.getFacet(graph.traversal().V());

    verify(propertyParser, atLeastOnce()).parse(value);
    verify(propertyParser, atLeastOnce()).parse(value1);
    verify(propertyParser, atLeastOnce()).parse(value2);
  }

  @Test
  public void filterDoesNotAddFilterToTheGraphTraversalWhenTheFacetOfTheDescriptionIsNotPresent() {
    GraphTraversal<Vertex, Vertex> traversal = newGraph()
      .withVertex(v -> v.withProperty(PROPERTY, "value1").withTimId("1"))
      .withVertex(v -> v.withProperty(PROPERTY, "value2").withTimId("2"))
      .build().traversal().V();
    List<FacetValue> facetValues = Lists.newArrayList();

    instance.filter(traversal, facetValues);

    assertThat(traversal.toList(),
      containsInAnyOrder(VertexMatcher.likeVertex().withTimId("1"), VertexMatcher.likeVertex().withTimId("2")));
  }

  @Test
  public void filterAddsAFilterToFilterOutTheNonMatchingVertices() {
    String value1 = "value1";
    List<FacetValue> facetValues = Lists.newArrayList(
      new ListFacetValue(FACET_NAME, Lists.newArrayList(value1)));
    SearchRequestV2_1 searchRequest = new SearchRequestV2_1();
    searchRequest.setFacetValues(facetValues);
    GraphTraversal<Vertex, Vertex> traversal = newGraph()
      .withVertex(v -> v.withProperty(PROPERTY, value1).withTimId("1"))
      .withVertex(v -> v.withProperty(PROPERTY, "value2").withTimId("2"))
      .build().traversal().V();

    instance.filter(traversal, facetValues);

    assertThat(traversal.toList(), contains(VertexMatcher.likeVertex().withTimId("1")));
  }

  @Test
  public void filterAddsNoFilterIfTheFacetValuesIsEmpty() {
    String value1 = "value1";
    List<FacetValue> facetValues = Lists.newArrayList(
      new ListFacetValue(FACET_NAME, Lists.newArrayList()));
    SearchRequestV2_1 searchRequest = new SearchRequestV2_1();
    searchRequest.setFacetValues(facetValues);
    GraphTraversal<Vertex, Vertex> traversal = newGraph()
      .withVertex(v -> v.withProperty(PROPERTY, value1).withTimId("1"))
      .withVertex(v -> v.withProperty(PROPERTY, "value2").withTimId("2"))
      .build().traversal().V();

    instance.filter(traversal, facetValues);

    assertThat(traversal.toList(),
      containsInAnyOrder(VertexMatcher.likeVertex().withTimId("1"), VertexMatcher.likeVertex().withTimId("2")));
  }

  @Test
  public void filterAddsNoFilterIfTheFacetValueIsNotAListFacetValue() {
    String value1 = "value1";
    List<FacetValue> facetValues = Lists.newArrayList((FacetValue) () -> FACET_NAME);
    SearchRequestV2_1 searchRequest = new SearchRequestV2_1();
    searchRequest.setFacetValues(facetValues);
    GraphTraversal<Vertex, Vertex> traversal = newGraph()
      .withVertex(v -> v.withProperty(PROPERTY, value1).withTimId("1"))
      .withVertex(v -> v.withProperty(PROPERTY, "value2").withTimId("2"))
      .build().traversal().V();

    instance.filter(traversal, facetValues);

    assertThat(traversal.toList(),
      containsInAnyOrder(VertexMatcher.likeVertex().withTimId("1"), VertexMatcher.likeVertex().withTimId("2")));
  }

  @Test
  public void filterLetsTheParserParseEachDatabaseValue() {
    String value1 = "value1";
    List<FacetValue> facetValues = Lists.newArrayList(
      new ListFacetValue(FACET_NAME, Lists.newArrayList(value1)));
    GraphTraversal<Vertex, Vertex> traversal = newGraph()
      .withVertex(v -> v.withProperty(PROPERTY, "value1").withTimId("1"))
      .withVertex(v -> v.withProperty(PROPERTY, "value2").withTimId("2"))
      .build().traversal().V();

    instance.filter(traversal, facetValues);
    traversal.toList(); // needed to verify the parser

    verify(propertyParser).parse("value1");
    verify(propertyParser).parse("value2");
  }

}
