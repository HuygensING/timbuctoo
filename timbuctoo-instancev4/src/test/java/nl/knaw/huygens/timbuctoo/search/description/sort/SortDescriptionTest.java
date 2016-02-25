package nl.knaw.huygens.timbuctoo.search.description.sort;

import com.google.common.collect.Lists;
import nl.knaw.huygens.timbuctoo.server.rest.search.SortParameter;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static nl.knaw.huygens.timbuctoo.search.VertexMatcher.likeVertex;
import static nl.knaw.huygens.timbuctoo.server.rest.search.SortParameter.Direction.asc;
import static nl.knaw.huygens.timbuctoo.util.TestGraphBuilder.newGraph;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyZeroInteractions;

public class SortDescriptionTest {

  public static final String SORT_FIELD_1 = "sortField1";
  public static final String SORT_FIELD_2 = "sortField2";
  public static final String PROPERTY_1 = "property1";
  public static final String PROPERTY_2 = "property2";
  public SortFieldDescription sortFieldDescription1;
  public SortFieldDescription sortFieldDescription2;
  private SortDescription instance;

  @Before
  public void setUp() throws Exception {
    sortFieldDescription1 = new SortFieldDescription(SORT_FIELD_1, __.has(PROPERTY_1).values(PROPERTY_1), "");
    sortFieldDescription2 = new SortFieldDescription(SORT_FIELD_2, __.has(PROPERTY_2).values(PROPERTY_2), "");
    instance = new SortDescription(Lists.newArrayList(sortFieldDescription1,
      sortFieldDescription2));
  }

  @Test
  public void sortAddsASortParameterToTheSearchResultsForEachOfTheSortParameters() {

    GraphTraversal<Vertex, Vertex> traversal = newGraph()
      .withVertex(v -> v.withTimId("id1")
                        .withProperty(PROPERTY_1, "value1.2")
                        .withProperty(PROPERTY_2, "value2"))
      .withVertex(v -> v.withTimId("id2")
                        .withProperty(PROPERTY_1, "value1")
                        .withProperty(PROPERTY_2, "value2.1"))
      .withVertex(v -> v.withTimId("id3")
                        .withProperty(PROPERTY_1, "value1")
                        .withProperty(PROPERTY_2, "value2"))
      .build()
      .traversal()
      .V();

    List<SortParameter> sortParameters = Lists.newArrayList(
      new SortParameter(SORT_FIELD_1, asc),
      new SortParameter(SORT_FIELD_2, asc));

    instance.sort(traversal, sortParameters);

    List<Vertex> actual = traversal.toList();
    assertThat(actual, contains(
      likeVertex().withTimId("id3"),
      likeVertex().withTimId("id2"),
      likeVertex().withTimId("id1")));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void sortDoesNothingWhenTheSortParametersAreEmpty() {
    GraphTraversal<Vertex, Vertex> traversal = mock(GraphTraversal.class);
    List<SortParameter> sortParameters = Lists.newArrayList();

    instance.sort(traversal, sortParameters);

    verifyZeroInteractions(traversal);
  }

  @Test
  @SuppressWarnings("unchecked")
  public void sortIgnoresUnknownSortParameters() {
    GraphTraversal<Vertex, Vertex> traversal = mock(GraphTraversal.class);
    List<SortParameter> sortParameters = Lists.newArrayList(new SortParameter("unknownField", asc));

    instance.sort(traversal, sortParameters);

    verifyZeroInteractions(traversal);
  }
}
