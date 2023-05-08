package nl.knaw.huygens.timbuctoo.search.description.facet;

import com.google.common.collect.Lists;
import nl.knaw.huygens.timbuctoo.search.FacetValue;
import nl.knaw.huygens.timbuctoo.search.description.PropertyParser;
import nl.knaw.huygens.timbuctoo.server.mediatypes.v2.search.ListFacetValue;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static nl.knaw.huygens.timbuctoo.util.VertexMatcher.likeVertex;
import static nl.knaw.huygens.timbuctoo.util.TestGraphBuilder.newGraph;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.mockito.BDDMockito.given;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class RelatedListFacetDescriptionTest {

  public static final String FACET_NAME = "facetName";
  public static final String RELATION = "relation";
  public static final String PROPERTY = "property";
  public static final String VALUE1 = "value1";
  public static final String VALUE2 = "value2";
  public static final String RELATION_2 = "relation2";
  private PropertyParser parser;

  @BeforeEach
  public void setUp() throws Exception {
    parser = mock(PropertyParser.class);
    given(parser.parse(anyString())).willAnswer(invocation -> invocation.getArguments()[0]);
  }


  @Test
  public void filterDoesNotAddFilterToTheGraphTraversalWhenTheFacetOfTheDescriptionIsNotPresent() {
    RelatedListFacetDescription instance =
      new RelatedListFacetDescription(FACET_NAME, PROPERTY, parser, RELATION);
    GraphTraversal<Vertex, Vertex> traversal = newGraph()
      .withVertex("v1", v -> v.withTimId("id1").withProperty(PROPERTY, VALUE1))
      .withVertex("v2", v -> v.withTimId("id2").withProperty(PROPERTY, VALUE2))
      .withVertex("v3", v -> v.withTimId("id3").withOutgoingRelation(RELATION, "v1"))
      .withVertex("v4", v -> v.withTimId("id4").withOutgoingRelation(RELATION, "v2"))
      .build().traversal().V();
    ArrayList<FacetValue> facets = Lists.newArrayList();

    instance.filter(traversal, facets);

    assertThat(traversal.toList(),
      containsInAnyOrder(likeVertex().withTimId("id1"), likeVertex().withTimId("id2"), likeVertex().withTimId("id3"),
        likeVertex().withTimId("id4")));
  }

  @Test
  public void filterAddsAFilterToTheGraphTraversal() {
    RelatedListFacetDescription instance =
      new RelatedListFacetDescription(FACET_NAME, PROPERTY, parser, RELATION);
    List<FacetValue> facets = Lists.newArrayList(new ListFacetValue(FACET_NAME, Lists.newArrayList(VALUE1)));
    GraphTraversal<Vertex, Vertex> traversal = newGraph()
      .withVertex("v1", v -> v.withTimId("id1").withProperty(PROPERTY, VALUE1))
      .withVertex("v2", v -> v.withTimId("id2").withProperty(PROPERTY, VALUE2))
      .withVertex("v3", v -> v.withTimId("id3").withOutgoingRelation(RELATION, "v1"))
      .withVertex("v4", v -> v.withTimId("id4").withOutgoingRelation(RELATION, "v2"))
      .build().traversal().V();

    instance.filter(traversal, facets);

    List<Vertex> vertices = traversal.toList();
    assertThat(vertices, contains(likeVertex().withTimId("id3")));
  }

  @Test
  public void filterAddsNoFilterWhenTheFacetValueIsNotAListFacetValue() {
    RelatedListFacetDescription instance =
      new RelatedListFacetDescription(FACET_NAME, PROPERTY, parser, RELATION);
    List<FacetValue> facets = Lists.newArrayList((FacetValue) () -> FACET_NAME);
    GraphTraversal<Vertex, Vertex> traversal = newGraph()
      .withVertex("v1", v -> v.withTimId("id1").withProperty(PROPERTY, VALUE1))
      .withVertex("v2", v -> v.withTimId("id2").withProperty(PROPERTY, VALUE2))
      .withVertex("v3", v -> v.withTimId("id3").withOutgoingRelation(RELATION, "v1"))
      .withVertex("v4", v -> v.withTimId("id4").withOutgoingRelation(RELATION, "v2"))
      .build().traversal().V();

    instance.filter(traversal, facets);

    assertThat(traversal.toList(),
      containsInAnyOrder(likeVertex().withTimId("id1"), likeVertex().withTimId("id2"), likeVertex().withTimId("id3"),
        likeVertex().withTimId("id4")));
  }

  @Test
  public void filterAddsNoFilterWhenTheFacetValueHasNoValues() {
    RelatedListFacetDescription instance =
      new RelatedListFacetDescription(FACET_NAME, PROPERTY, parser, RELATION);
    List<FacetValue> facets = Lists.newArrayList(new ListFacetValue(FACET_NAME, Lists.newArrayList()));
    GraphTraversal<Vertex, Vertex> traversal = newGraph()
      .withVertex("v1", v -> v.withTimId("id1").withProperty(PROPERTY, VALUE1))
      .withVertex("v2", v -> v.withTimId("id2").withProperty(PROPERTY, VALUE2))
      .withVertex("v3", v -> v.withTimId("id3").withOutgoingRelation(RELATION, "v1"))
      .withVertex("v4", v -> v.withTimId("id4").withOutgoingRelation(RELATION, "v2"))
      .build().traversal().V();

    instance.filter(traversal, facets);

    assertThat(traversal.toList(),
      containsInAnyOrder(likeVertex().withTimId("id1"), likeVertex().withTimId("id2"), likeVertex().withTimId("id3"),
        likeVertex().withTimId("id4")));
  }

  @Test
  public void filterLetsTheParserParseEachDatabaseValue() {
    RelatedListFacetDescription instance =
      new RelatedListFacetDescription(FACET_NAME, PROPERTY, parser, RELATION);
    List<FacetValue> facets = Lists.newArrayList(new ListFacetValue(FACET_NAME, Lists.newArrayList(VALUE1)));
    GraphTraversal<Vertex, Vertex> traversal = newGraph()
      .withVertex("v1", v -> v.withTimId("id1").withProperty(PROPERTY, VALUE1))
      .withVertex("v2", v -> v.withTimId("id2").withProperty(PROPERTY, VALUE2))
      .withVertex("v3", v -> v.withTimId("id3").withOutgoingRelation(RELATION, "v1"))
      .withVertex("v4", v -> v.withTimId("id4").withOutgoingRelation(RELATION, "v2"))
      .build().traversal().V();

    instance.filter(traversal, facets);
    traversal.toList(); // needed to verify the parser

    verify(parser).parse(VALUE1);
    verify(parser).parse(VALUE2);
  }

}
