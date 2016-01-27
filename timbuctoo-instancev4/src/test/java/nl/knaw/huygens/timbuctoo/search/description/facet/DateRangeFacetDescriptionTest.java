package nl.knaw.huygens.timbuctoo.search.description.facet;

import com.google.common.collect.Lists;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.junit.Test;

import java.util.List;

import static nl.knaw.huygens.timbuctoo.search.MockVertexBuilder.vertex;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;

public class DateRangeFacetDescriptionTest {

  public static final String FACET_NAME = "facetName";
  public static final String PROPERTY_NAME = "propertyName";

  @Test
  public void getFacetReturnsAFacetWithItsNameAndTypeRange() {
    DateRangeFacetDescription instance = new DateRangeFacetDescription(FACET_NAME, PROPERTY_NAME);
    List<Vertex> vertices = Lists.newArrayList(vertex().build());

    Facet facet = instance.getFacet(vertices);

    assertThat(facet, allOf(
      hasProperty("name", equalTo(FACET_NAME)),
      hasProperty("type", equalTo("RANGE"))));
  }

  @Test
  public void getFacetReturnsFacetWithOneOptionWithDefaultValuesWhenTheVerticesDoNotContainTheProperty() {
    DateRangeFacetDescription instance = new DateRangeFacetDescription(FACET_NAME, PROPERTY_NAME);
    List<Vertex> vertices = Lists.newArrayList(vertex().build());

    Facet facet = instance.getFacet(vertices);

    assertThat(facet.getOptions(), containsInAnyOrder(new Facet.RangeOption(0, 0)));
  }

  @Test
  public void getFacetReturnsRangeOptionWithDefaultValuesWhenTheStoredDatabaseIsNotValid() {
    DateRangeFacetDescription instance = new DateRangeFacetDescription(FACET_NAME, PROPERTY_NAME);
    List<Vertex> vertices = Lists.newArrayList(vertex().withProperty(PROPERTY_NAME, "invalidDatable").build());

    Facet facet = instance.getFacet(vertices);

    assertThat(facet.getOptions(), containsInAnyOrder(new Facet.RangeOption(0, 0)));
  }

  @Test
  public void getFacetReturnsTheUpperAndLowerLimitInYearMonthDayFormat() {
    DateRangeFacetDescription instance = new DateRangeFacetDescription(FACET_NAME, PROPERTY_NAME);
    List<Vertex> vertices = Lists.newArrayList(vertex().withProperty(PROPERTY_NAME, "2015-01").build());

    Facet facet = instance.getFacet(vertices);

    assertThat(facet.getOptions(), contains(new Facet.RangeOption(20150101, 20150131)));
  }

  @Test
  public void getFacetReturnsLowestLowerLimitAndTheHighestUpperLimit() {
    DateRangeFacetDescription instance = new DateRangeFacetDescription(FACET_NAME, PROPERTY_NAME);
    List<Vertex> vertices = Lists.newArrayList(
      vertex().withProperty(PROPERTY_NAME, "2015-01").build(),
      vertex().withProperty(PROPERTY_NAME, "0015-01").build(),
      vertex().withProperty(PROPERTY_NAME, "0190-01").build());

    Facet facet = instance.getFacet(vertices);

    assertThat(facet.getOptions(), contains(new Facet.RangeOption(150101, 20150131)));
  }

}
