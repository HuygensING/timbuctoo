package nl.knaw.huygens.timbuctoo.search.description.fulltext;

import nl.knaw.huygens.timbuctoo.server.rest.search.FullTextSearchParameter;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.junit.Before;
import org.junit.Test;

import static nl.knaw.huygens.timbuctoo.search.VertexMatcher.likeVertex;
import static nl.knaw.huygens.timbuctoo.util.TestGraphBuilder.newGraph;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class FullTextSearchDescriptionTest {

  protected static final String PROPERTY_NAME = "propertyName";
  protected static final String NAME = "name";
  private FullTextSearchDescription instance;

  @Before
  public void setUp() throws Exception {
    instance = FullTextSearchDescription.createLocalSimpleFullTextSearchDescription(NAME, PROPERTY_NAME);
  }

  protected FullTextSearchDescription getInstance() {
    return instance;
  }

  @Test
  public void getNameReturnsTheNameOfTheProperty() {
    String name = getInstance().getName();

    assertThat(name, is(NAME));
  }

  @Test
  public void filterFiltersTheVerticesOnTheValueOfTheProperty() {
    GraphTraversal<Vertex, Vertex> traversal = newGraph()
      .withVertex(vertex -> vertex.withTimId("v1").withProperty(PROPERTY_NAME, "value1"))
      .withVertex(vertex -> vertex.withTimId("v2").withProperty(PROPERTY_NAME, "number2"))
      .withVertex(vertex -> vertex.withTimId("v3").withProperty(PROPERTY_NAME, "value1"))
      .build()
      .traversal()
      .V();
    FullTextSearchParameter searchParameter = new FullTextSearchParameter(NAME, "value1");

    getInstance().filter(traversal, searchParameter);

    assertThat(traversal.toList(), containsInAnyOrder(likeVertex().withTimId("v1"), likeVertex().withTimId("v3")));
  }

  @Test
  public void filterIncludesTheVerticesWhereTheSearchedValueIsPartOfThePropertyValueOfTheVertex() {
    GraphTraversal<Vertex, Vertex> traversal = newGraph()
      .withVertex(vertex -> vertex.withTimId("v1").withProperty(PROPERTY_NAME, "value12344324"))
      .withVertex(vertex -> vertex.withTimId("v2").withProperty(PROPERTY_NAME, "another value"))
      .withVertex(vertex -> vertex.withTimId("v3").withProperty(PROPERTY_NAME, "value1"))
      .build()
      .traversal()
      .V();
    FullTextSearchParameter fullTextSearchParameter = new FullTextSearchParameter(NAME, "value");

    getInstance().filter(traversal, fullTextSearchParameter);

    assertThat(traversal.toList(), containsInAnyOrder(
      likeVertex().withTimId("v1"),
      likeVertex().withTimId("v2"),
      likeVertex().withTimId("v3")));
  }

  @Test
  public void filterDiscardsTheVerticesThatContainThePropertyWithADifferentTypeOfValue() {
    GraphTraversal<Vertex, Vertex> traversal = newGraph()
      .withVertex(vertex -> vertex.withTimId("v1").withProperty(PROPERTY_NAME, "value12344324"))
      .withVertex(vertex -> vertex.withTimId("v2").withProperty(PROPERTY_NAME, 12334))
      .withVertex(vertex -> vertex.withTimId("v3").withProperty(PROPERTY_NAME, "value1"))
      .build()
      .traversal()
      .V();
    FullTextSearchParameter fullTextSearchParameter = new FullTextSearchParameter(NAME, "value");

    getInstance().filter(traversal, fullTextSearchParameter);

    assertThat(traversal.toList(), containsInAnyOrder(
      likeVertex().withTimId("v1"),
      likeVertex().withTimId("v3")));
  }

  @Test
  public void filterFiltersOnEachOfTheTermIndividuallyEachPropertyHasToContainOnlyOne() {
    GraphTraversal<Vertex, Vertex> traversal = newGraph()
      .withVertex(vertex -> vertex.withTimId("v1").withProperty(PROPERTY_NAME, "value 12344324 value2"))
      .withVertex(vertex -> vertex.withTimId("v2").withProperty(PROPERTY_NAME, "value value2"))
      .withVertex(vertex -> vertex.withTimId("v3").withProperty(PROPERTY_NAME, "value1 value2"))
      .build()
      .traversal()
      .V();
    FullTextSearchParameter fullTextSearchParameter = new FullTextSearchParameter(NAME, "value value2");

    getInstance().filter(traversal, fullTextSearchParameter);

    assertThat(traversal.toList(), containsInAnyOrder(
      likeVertex().withTimId("v1"),
      likeVertex().withTimId("v2"),
      likeVertex().withTimId("v3")));
  }

  @Test
  public void filterFiltersCaseIndependent() {
    GraphTraversal<Vertex, Vertex> traversal = newGraph()
      .withVertex(vertex -> vertex.withTimId("v1").withProperty(PROPERTY_NAME, "Value"))
      .withVertex(vertex -> vertex.withTimId("v2").withProperty(PROPERTY_NAME, "VALUE"))
      .withVertex(vertex -> vertex.withTimId("v3").withProperty(PROPERTY_NAME, "vALUE"))
      .build()
      .traversal()
      .V();
    FullTextSearchParameter fullTextSearchParameter = new FullTextSearchParameter(NAME, "value");

    getInstance().filter(traversal, fullTextSearchParameter);

    assertThat(traversal.toList(), containsInAnyOrder(
      likeVertex().withTimId("v1"),
      likeVertex().withTimId("v2"),
      likeVertex().withTimId("v3")));
  }

}
