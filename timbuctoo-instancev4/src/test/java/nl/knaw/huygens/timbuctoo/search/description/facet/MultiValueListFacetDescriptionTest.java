package nl.knaw.huygens.timbuctoo.search.description.facet;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import nl.knaw.huygens.timbuctoo.search.description.facet.Facet.DefaultOption;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static nl.knaw.huygens.timbuctoo.search.description.facet.DefaultFacetOptionMatcher.likeDefaultFacetOption;
import static nl.knaw.huygens.timbuctoo.util.TestGraphBuilder.newGraph;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;

public class MultiValueListFacetDescriptionTest {

  public static final String FACET_NAME = "facetName";
  public static final String PROPERTY_NAME = "propertyName";
  private ObjectMapper objectMapper;
  private MultiValueListFacetDescription instance;

  @Before
  public void setUp() throws Exception {
    objectMapper = new ObjectMapper();
    instance = new MultiValueListFacetDescription(FACET_NAME, PROPERTY_NAME);
  }

  @Test
  public void getFacetReturnsAFacetWithItsNameAndTypeRange() {
    Graph graph = newGraph()
      .withVertex(v -> v.withTimId("id"))
      .build();

    Facet facet = instance.getFacet(graph.traversal().V());

    assertThat(facet, allOf(
      hasProperty("name", equalTo(FACET_NAME)),
      hasProperty("type", equalTo("LIST"))));
  }

  @Test
  public void getFacetReturnsAnOptionForEachValueTheVertexPropertyContains() {
    Graph graph = newGraph()
      .withVertex(v -> v.withProperty(PROPERTY_NAME, serializedListOf("value1", "value2")))
      .build();

    Facet facet = instance.getFacet(graph.traversal().V());

    assertThat(facet, hasProperty("options", containsInAnyOrder(likeDefaultFacetOption().withName("value1"),
      likeDefaultFacetOption().withName("value2"))));
  }

  @Test
  public void getFacetReturnsAnOptionWithACountOfTheOccurrencesOfAValue() {
    Graph graph = newGraph()
      .withVertex(v -> v.withProperty(PROPERTY_NAME, serializedListOf("value1", "value2")))
      .withVertex(v -> v.withProperty(PROPERTY_NAME, serializedListOf("value2", "value3")))
      .withVertex(v -> v.withProperty(PROPERTY_NAME, serializedListOf("value1", "value3", "value4")))
      .build();

    Facet facet = instance.getFacet(graph.traversal().V());

    assertThat(facet, hasProperty("options", containsInAnyOrder(
      new DefaultOption("value1", 2),
      new DefaultOption("value2", 2),
      new DefaultOption("value3", 2),
      new DefaultOption("value4", 1))));
  }

  private String serializedListOf(String... values) {
    List<String> list = Arrays.asList(values);

    try {
      return objectMapper.writeValueAsString(list);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }
}
