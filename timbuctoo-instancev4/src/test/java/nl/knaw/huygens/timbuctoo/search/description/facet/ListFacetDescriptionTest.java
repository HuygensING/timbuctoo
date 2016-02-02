package nl.knaw.huygens.timbuctoo.search.description.facet;

import nl.knaw.huygens.timbuctoo.search.description.PropertyParser;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.junit.Test;

import static nl.knaw.huygens.timbuctoo.util.TestGraphBuilder.newGraph;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.core.Is.is;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;

public class ListFacetDescriptionTest {

  @Test
  public void getFacetReturnsTheFacetWithItsNameAndTheTypeList() {
    PropertyParser parser = mock(PropertyParser.class);
    String facetName = "facetName";
    String property = "property";
    ListFacetDescription instance = new ListFacetDescription(facetName, property, parser);

    Graph graph = newGraph().build();

    Facet facet = instance.getFacet(graph.traversal().V());

    assertThat(facet, allOf(
      hasProperty("name", equalTo(facetName)),
      hasProperty("type", equalTo("LIST"))));

  }

  @Test
  public void getFacetReturnsAnEmptyListOfCountsWhenNotVerticesContainTheProperty() {
    PropertyParser parser = mock(PropertyParser.class);
    ListFacetDescription instance = new ListFacetDescription("facetName", "property", parser);

    Graph graph = newGraph().withVertex(v -> v.withTimId("id")).withVertex(v -> v.withTimId("id1")).build();

    Facet facet = instance.getFacet(graph.traversal().V());

    assertThat(facet.getOptions(), is(empty()));
  }

  @Test
  public void getFacetReturnsTheFacetWithItsCounts() {
    PropertyParser parser = mock(PropertyParser.class);
    given(parser.parse(anyString())).willAnswer(invocation -> invocation.getArguments()[0]);
    String facetName = "facetName";
    String property = "property";
    ListFacetDescription instance = new ListFacetDescription(facetName, property, parser);
    String value = "value";
    String value1 = "value1";
    String value2 = "value2";

    Graph graph = newGraph()
      .withVertex(v -> v.withTimId("id").withProperty(property, value))
      .withVertex(v -> v.withTimId("id1").withProperty(property, value))
      .withVertex(v -> v.withTimId("id2").withProperty(property, value1))
      .withVertex(v -> v.withTimId("id3").withProperty(property, value1))
      .withVertex(v -> v.withTimId("id4").withProperty(property, value1))
      .withVertex(v -> v.withTimId("id5").withProperty(property, value2))
      .build();

    Facet facet = instance.getFacet(graph.traversal().V());

    // L is needed because the counts are longs
    assertThat(facet.getOptions(), containsInAnyOrder(
      new Facet.DefaultOption(value, 2L),
      new Facet.DefaultOption(value1, 3L),
      new Facet.DefaultOption(value2, 1L)));
  }
}
