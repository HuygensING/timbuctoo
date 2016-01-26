package nl.knaw.huygens.timbuctoo.search.description.facet;

import com.google.common.collect.Lists;
import nl.knaw.huygens.timbuctoo.search.description.PropertyParser;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.junit.Test;

import java.util.List;

import static nl.knaw.huygens.timbuctoo.search.MockVertexBuilder.vertex;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;

public class ListFacetDescriptionTest {

  @Test
  public void getFacetReturnsTheFacetWithItsName() {
    PropertyParser parser = mock(PropertyParser.class);
    String facetName = "facetName";
    String property = "property";
    ListFacetDescription instance = new ListFacetDescription(facetName, property, parser);

    Facet facet = instance.getFacet(Lists.newArrayList());

    assertThat(facet, is(notNullValue()));
    assertThat(facet.getName(), is(facetName));
  }

  @Test
  public void getFacetReturnsTheFacetWithItsCounts() {
    PropertyParser parser = mock(PropertyParser.class);
    given(parser.parse(any())).willAnswer(invocation -> invocation.getArguments()[0]);
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
    assertThat(facet.getCounts(), allOf(hasEntry(value, 2L), hasEntry(value1, 3L), hasEntry(value2, 1L)));
  }
}
