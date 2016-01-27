package nl.knaw.huygens.timbuctoo.search.description.facet;

import com.google.common.collect.Lists;
import nl.knaw.huygens.timbuctoo.search.description.PropertyParser;
import nl.knaw.huygens.timbuctoo.search.description.facet.Facet.DefaultOption;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static nl.knaw.huygens.timbuctoo.search.MockVertexBuilder.vertex;
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

    Facet facet = instance.getFacet(Lists.newArrayList());

    assertThat(facet, allOf(
      hasProperty("name", equalTo(facetName)),
      hasProperty("type", equalTo("LIST"))));

  }

  @Test
  public void getFacetReturnsAnEmptyListOfCountsWhenNotVerticesContainTheProperty() {
    PropertyParser parser = mock(PropertyParser.class);
    ListFacetDescription instance = new ListFacetDescription("facetName", "property", parser);

    ArrayList<Vertex> vertices = Lists.newArrayList(
      vertex().build(),
      vertex().build()
    );

    Facet facet = instance.getFacet(vertices);

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
    List<Vertex> vertices = Lists.newArrayList(
      vertex().withProperty(property, value).build(),
      vertex().withProperty(property, value).build(),
      vertex().withProperty(property, value1).build(),
      vertex().withProperty(property, value1).build(),
      vertex().withProperty(property, value1).build(),
      vertex().withProperty(property, value2).build()
    );

    Facet facet = instance.getFacet(vertices);

    // L is needed because the counts are longs
    assertThat(facet.getOptions(), containsInAnyOrder(
      new DefaultOption(value, 2L),
      new Facet.DefaultOption(value1, 3L),
      new Facet.DefaultOption(value2, 1L)));
  }
}
