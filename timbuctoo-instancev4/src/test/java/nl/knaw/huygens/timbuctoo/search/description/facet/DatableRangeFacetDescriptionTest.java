package nl.knaw.huygens.timbuctoo.search.description.facet;

import org.apache.tinkerpop.gremlin.structure.Graph;
import org.junit.Test;

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

  @Test
  public void getFacetReturnsAFacetWithItsNameAndTypeRange() {
    DatableRangeFacetDescription instance = new DatableRangeFacetDescription(FACET_NAME, PROPERTY_NAME);

    Graph graph = newGraph().withVertex(v -> v.withTimId("id")).build();

    Facet facet = instance.getFacet(graph.traversal().V());

    assertThat(facet, allOf(
      hasProperty("name", equalTo(FACET_NAME)),
      hasProperty("type", equalTo("RANGE"))));
  }

  @Test
  public void getFacetReturnsFacetWithOneOptionWithDefaultValuesWhenTheVerticesDoNotContainTheProperty() {
    DatableRangeFacetDescription instance = new DatableRangeFacetDescription(FACET_NAME, PROPERTY_NAME);
    Graph graph = newGraph().withVertex(v -> v.withTimId("id")).build();

    Facet facet = instance.getFacet(graph.traversal().V());

    assertThat(facet.getOptions(), containsInAnyOrder(new Facet.RangeOption(0, 0)));
  }

  @Test
  public void getFacetReturnsRangeOptionWithDefaultValuesWhenTheStoredDatabaseIsNotValid() {
    DatableRangeFacetDescription instance = new DatableRangeFacetDescription(FACET_NAME, PROPERTY_NAME);
    Graph graph = newGraph().withVertex(
      v -> v.withTimId("id").withProperty(PROPERTY_NAME, "invalidDatable")
    ).build();

    Facet facet = instance.getFacet(graph.traversal().V());

    assertThat(facet.getOptions(), containsInAnyOrder(new Facet.RangeOption(0, 0)));
  }

  @Test
  public void getFacetReturnsTheUpperAndLowerLimitInYearMonthDayFormat() {
    DatableRangeFacetDescription instance = new DatableRangeFacetDescription(FACET_NAME, PROPERTY_NAME);
    Graph graph = newGraph().withVertex(v -> v.withTimId("id").withProperty(PROPERTY_NAME, "2015-01"))
                            .build();

    Facet facet = instance.getFacet(graph.traversal().V());

    assertThat(facet.getOptions(), contains(new Facet.RangeOption(20150101, 20150131)));
  }

  @Test
  public void getFacetReturnsLowestLowerLimitAndTheHighestUpperLimit() {
    DatableRangeFacetDescription instance = new DatableRangeFacetDescription(FACET_NAME, PROPERTY_NAME);

    Graph graph = newGraph().withVertex(v -> v.withTimId("id1").withProperty(PROPERTY_NAME, "2015-01"))
                            .withVertex(v -> v.withTimId("id2").withProperty(PROPERTY_NAME, "0015-01"))
                            .withVertex(v -> v.withTimId("id3").withProperty(PROPERTY_NAME, "0190-01"))
                            .build();

    Facet facet = instance.getFacet(graph.traversal().V());

    assertThat(facet.getOptions(), contains(new Facet.RangeOption(150101, 20150131)));
  }

}
