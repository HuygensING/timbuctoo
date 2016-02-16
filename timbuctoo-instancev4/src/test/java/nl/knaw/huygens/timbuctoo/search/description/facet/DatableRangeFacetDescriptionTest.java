package nl.knaw.huygens.timbuctoo.search.description.facet;

import com.google.common.collect.Lists;
import nl.knaw.huygens.timbuctoo.search.FacetValue;
import nl.knaw.huygens.timbuctoo.server.rest.search.DateRangeFacetValue;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static nl.knaw.huygens.timbuctoo.search.VertexMatcher.likeVertex;
import static nl.knaw.huygens.timbuctoo.util.TestGraphBuilder.newGraph;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;

public class DatableRangeFacetDescriptionTest {

  public static final String FACET_NAME = "facetName";
  public static final String PROPERTY_NAME = "propertyName";
  private DatableRangeFacetDescription instance;

  @Before
  public void setUp() throws Exception {
    instance = new DatableRangeFacetDescription(FACET_NAME, PROPERTY_NAME);
  }

  @Test
  public void getFacetReturnsAFacetWithItsNameAndTypeRange() {
    Graph graph = newGraph()
      .withVertex(v -> v.withTimId("id"))
      .build();

    Facet facet = instance.getFacet(graph.traversal().V());

    assertThat(facet, allOf(
      hasProperty("name", equalTo(FACET_NAME)),
      hasProperty("type", equalTo("RANGE"))));
  }

  @Test
  public void getFacetReturnsFacetWithOneOptionWithDefaultValuesWhenTheVerticesDoNotContainTheProperty() {
    Graph graph = newGraph()
      .withVertex(v -> v.withTimId("id"))
      .build();

    Facet facet = instance.getFacet(graph.traversal().V());

    assertThat(facet.getOptions(), containsInAnyOrder(new Facet.RangeOption(0, 0)));
  }

  @Test
  public void getFacetReturnsRangeOptionWithDefaultValuesWhenTheStoredDatabaseIsNotValid() {
    Graph graph = newGraph()
      .withVertex(v -> v.withTimId("id").withProperty(PROPERTY_NAME, "invalidDatable"))
      .build();

    Facet facet = instance.getFacet(graph.traversal().V());

    assertThat(facet.getOptions(), containsInAnyOrder(new Facet.RangeOption(0, 0)));
  }

  @Test
  public void getFacetReturnsTheUpperAndLowerLimitInYearMonthDayFormat() {
    Graph graph = newGraph().withVertex(v -> v.withProperty(PROPERTY_NAME, asSerializedDatable(
      "2015-01")))
                            .build();

    Facet facet = instance.getFacet(graph.traversal().V());

    assertThat(facet.getOptions(), contains(new Facet.RangeOption(20150101, 20150131)));
  }

  @Test
  public void getFacetReturnsLowestLowerLimitAndTheHighestUpperLimit() {
    Graph graph = newGraph()
      .withVertex(v -> v.withProperty(PROPERTY_NAME, asSerializedDatable("2015-01")))
      .withVertex(v -> v.withProperty(PROPERTY_NAME, asSerializedDatable("0015-01")))
      .withVertex(v -> v.withProperty(PROPERTY_NAME, asSerializedDatable("0190-01")))
      .build();

    Facet facet = instance.getFacet(graph.traversal().V());

    assertThat(facet.getOptions(), contains(new Facet.RangeOption(150101, 20150131)));
  }

  @Test
  public void filterAddsNoFilterWhenTheFacetIsNotPresent() {
    GraphTraversal<Vertex, Vertex> traversal = newGraph()
      .withVertex(v -> v.withTimId("id1").withProperty(PROPERTY_NAME, asSerializedDatable("2015-01")))
      .withVertex(v -> v.withTimId("id2").withProperty(PROPERTY_NAME, asSerializedDatable("0015-01")))
      .withVertex(v -> v.withTimId("id3").withProperty(PROPERTY_NAME, asSerializedDatable("0190-01")))
      .build().traversal().V();
    List<FacetValue> facetValues = Lists.newArrayList();

    instance.filter(traversal, facetValues);

    assertThat(traversal.toList(), containsInAnyOrder(
      likeVertex().withTimId("id1"),
      likeVertex().withTimId("id2"),
      likeVertex().withTimId("id3")));
  }

  @Test
  public void filterAddFiltersTheVertices() {
    GraphTraversal<Vertex, Vertex> traversal = newGraph()
      .withVertex(v -> v.withTimId("id1").withProperty(PROPERTY_NAME, asSerializedDatable("2015-01")))
      .withVertex(v -> v.withTimId("id2").withProperty(PROPERTY_NAME, asSerializedDatable("0015-01")))
      .withVertex(v -> v.withTimId("id3").withProperty(PROPERTY_NAME, asSerializedDatable("0190-01")))
      .build().traversal().V();
    List<FacetValue> facetValues = Lists.newArrayList(new DateRangeFacetValue(FACET_NAME, 101101L, 10001231L));

    instance.filter(traversal, facetValues);

    assertThat(traversal.toList(), containsInAnyOrder(
      likeVertex().withTimId("id2"),
      likeVertex().withTimId("id3")));
  }

  @Test
  public void filterIncludesTheVerticesWhereOnlyTheStartFallsInTheRange() {
    GraphTraversal<Vertex, Vertex> traversal = newGraph()
      .withVertex(v -> v.withTimId("id1").withProperty(PROPERTY_NAME, asSerializedDatable("2015-01")))
      .withVertex(v -> v.withTimId("id2").withProperty(PROPERTY_NAME, asSerializedDatable("0015-01")))
      .withVertex(v -> v.withTimId("id3").withProperty(PROPERTY_NAME, asSerializedDatable("0190-01")))
      .build().traversal().V();
    List<FacetValue> facetValues = Lists.newArrayList(new DateRangeFacetValue(FACET_NAME, 20141231L, 20150102L));

    instance.filter(traversal, facetValues);

    assertThat(traversal.toList(), contains(likeVertex().withTimId("id1")));
  }

  @Test
  public void filterIncludesTheVerticesWhereOnlyTheEndFallsInTheRange() {
    GraphTraversal<Vertex, Vertex> traversal = newGraph()
      .withVertex(v -> v.withTimId("id1").withProperty(PROPERTY_NAME, asSerializedDatable("2015-01")))
      .withVertex(v -> v.withTimId("id2").withProperty(PROPERTY_NAME, asSerializedDatable("0015-01")))
      .withVertex(v -> v.withTimId("id3").withProperty(PROPERTY_NAME, asSerializedDatable("0190-01")))
      .build().traversal().V();
    List<FacetValue> facetValues = Lists.newArrayList(new DateRangeFacetValue(FACET_NAME, 20150114L, 20150224L));

    instance.filter(traversal, facetValues);

    assertThat(traversal.toList(), contains(likeVertex().withTimId("id1")));
  }

  @Test
  public void filterAddNoFilterWhenTheFacetValueIsNotADateRangeFacetValue() {
    GraphTraversal<Vertex, Vertex> traversal = newGraph()
      .withVertex(v -> v.withTimId("id1").withProperty(PROPERTY_NAME, asSerializedDatable("2015-01")))
      .withVertex(v -> v.withTimId("id2").withProperty(PROPERTY_NAME, asSerializedDatable("0015-01")))
      .withVertex(v -> v.withTimId("id3").withProperty(PROPERTY_NAME, asSerializedDatable("0190-01")))
      .build().traversal().V();
    List<FacetValue> facetValues = Lists.newArrayList((FacetValue) () -> FACET_NAME);

    instance.filter(traversal, facetValues);

    assertThat(traversal.toList(), containsInAnyOrder(
      likeVertex().withTimId("id1"),
      likeVertex().withTimId("id2"),
      likeVertex().withTimId("id3")));
  }

  private String asSerializedDatable(String datableString) {
    return String.format("\"%s\"", datableString);
  }

}
