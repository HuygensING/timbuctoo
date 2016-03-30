package nl.knaw.huygens.timbuctoo.search.description.facet;

import nl.knaw.huygens.timbuctoo.search.description.PropertyParser;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.junit.Before;
import org.junit.Test;

import static nl.knaw.huygens.timbuctoo.util.TestGraphBuilder.newGraph;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class DerivedListFacetDescriptionTest {

  public static final String RELATION_NAME = "hasKeyword";
  public static final String FACET_NAME = "facetName";
  public static final String RELATION = "relation";
  public static final String PROPERTY = "property";
  public static final String VALUE1 = "value1";
  public static final String VALUE2 = "value2";
  public static final String RELATION_2 = "relation2";
  private PropertyParser parser;

  @Before
  public void setUp() throws Exception {
    parser = mock(PropertyParser.class);
    given(parser.parse(anyString())).willAnswer(invocation -> invocation.getArguments()[0]);
  }

  @Test
  public void getFacetReturnsTheFacetWithItsNameAndTypeList() {
    Graph graph = newGraph().withVertex(v -> v.withTimId("id")).build();
    DerivedListFacetDescription instance =
            new DerivedListFacetDescription(FACET_NAME, PROPERTY, RELATION_NAME, parser, RELATION);

    Facet facet = instance.getFacet(graph.traversal().V());

    assertThat(facet, allOf(
            hasProperty("name", equalTo(FACET_NAME)),
            hasProperty("type", equalTo("LIST"))));
  }

  @Test
  public void getFacetReturnsTheFacetWithAnEmptyOptionsListWhenTheVerticesListIsEmpty() {
    DerivedListFacetDescription instance =
            new DerivedListFacetDescription(FACET_NAME, PROPERTY, RELATION_NAME, parser, RELATION);
    Graph graph = newGraph().build();

    Facet facet = instance.getFacet(graph.traversal().V());

    assertThat(facet.getOptions(), is(empty()));
  }

  @Test
  public void getFacetReturnsTheFacetWithAnEmptyOptionsListWhenTheVerticesDoNotContainTheRelation() {
    DerivedListFacetDescription instance =
            new DerivedListFacetDescription(FACET_NAME, PROPERTY, RELATION_NAME, parser, RELATION);
    Graph graph = newGraph().withVertex(v -> v.withTimId("id")).withVertex(v -> v.withTimId("id2")).build();

    Facet facet = instance.getFacet(graph.traversal().V());

    assertThat(facet.getOptions(), is(empty()));
  }


  @Test
  public void getFacetAddsTheDifferentValuesToTheOptionsList() {
    DerivedListFacetDescription instance =
            new DerivedListFacetDescription(FACET_NAME, PROPERTY, RELATION_NAME, parser, RELATION);

    Graph graph = newGraph()
            .withVertex("keywrd1", v -> v.withTimId("id1").withProperty(PROPERTY, VALUE1))
            .withVertex("keywrd2", v -> v.withTimId("id2").withProperty(PROPERTY, VALUE2))
            .withVertex("target1", v -> v.withTimId("id3").withOutgoingRelation(RELATION_NAME, "keywrd1"))
            .withVertex("target2", v -> v.withTimId("id4").withOutgoingRelation(RELATION_NAME, "keywrd2"))
            .withVertex("source1", v -> v.withTimId("id5").withOutgoingRelation(RELATION, "target1"))
            .withVertex("source2", v -> v.withTimId("id6").withOutgoingRelation(RELATION, "target2"))
            .build();

    Facet facet = instance.getFacet(graph.traversal().V());

    assertThat(facet.getOptions(),
            containsInAnyOrder(new Facet.DefaultOption(VALUE1, 1), new Facet.DefaultOption(VALUE2, 1)));
  }

  @Test
  public void getFacetLetsTheParserParseEachValueOnce() {
    DerivedListFacetDescription instance =
            new DerivedListFacetDescription(FACET_NAME, PROPERTY, RELATION_NAME, parser, RELATION);

    Graph graph = newGraph().withVertex("v1", v -> v.withTimId("id1").withProperty(PROPERTY, VALUE1))
            .withVertex("keywrd1", v -> v.withTimId("id1").withProperty(PROPERTY, VALUE1))
            .withVertex("keywrd2", v -> v.withTimId("id2").withProperty(PROPERTY, VALUE2))
            .withVertex("keywrd3", v -> v.withTimId("id3").withProperty(PROPERTY, VALUE2))
            .withVertex("target1", v -> v.withTimId("id4").withOutgoingRelation(RELATION_NAME, "keywrd1"))
            .withVertex("target2", v -> v.withTimId("id5")
                    .withOutgoingRelation(RELATION_NAME, "keywrd2").withOutgoingRelation(RELATION_NAME, "keywrd3")
            )
            .withVertex("source1", v -> v.withTimId("id6").withOutgoingRelation(RELATION, "target1"))
            .withVertex("source2", v -> v.withTimId("id7").withOutgoingRelation(RELATION, "target2"))
            .build();

    instance.getFacet(graph.traversal().V());

    verify(parser).parse(VALUE1);
    verify(parser).parse(VALUE2);
  }

  @Test
  public void getFacetGroupsTheCountsOfOneValue() {
    DerivedListFacetDescription instance =
            new DerivedListFacetDescription(FACET_NAME, PROPERTY, RELATION_NAME, parser, RELATION);

    Graph graph = newGraph()
            .withVertex("keywrd1", v -> v.withTimId("id1").withProperty(PROPERTY, VALUE1))
            .withVertex("keywrd2", v -> v.withTimId("id2").withProperty(PROPERTY, VALUE1))
            .withVertex("target1", v -> v.withTimId("id3").withOutgoingRelation(RELATION_NAME, "keywrd1"))
            .withVertex("target2", v -> v.withTimId("id4").withOutgoingRelation(RELATION_NAME, "keywrd2"))
            .withVertex("source1", v -> v.withTimId("id5").withOutgoingRelation(RELATION, "target1"))
            .withVertex("source2", v -> v.withTimId("id6").withOutgoingRelation(RELATION, "target2"))
            .build();

    Facet facet = instance.getFacet(graph.traversal().V());

    assertThat(facet.getOptions(), containsInAnyOrder(new Facet.DefaultOption(VALUE1, 2)));
  }

  @Test
  public void getFacetAddsTheValueOfEachRelationType() {
    DerivedListFacetDescription instance =
            new DerivedListFacetDescription(FACET_NAME, PROPERTY, RELATION_NAME, parser, RELATION, RELATION_2);

    Graph graph = newGraph()
            .withVertex("keywrd1", v -> v.withTimId("id1").withProperty(PROPERTY, VALUE1))
            .withVertex("keywrd2", v -> v.withTimId("id2").withProperty(PROPERTY, VALUE2))
            .withVertex("target1", v -> v.withTimId("id3").withOutgoingRelation(RELATION_NAME, "keywrd1"))
            .withVertex("target2", v -> v.withTimId("id4").withOutgoingRelation(RELATION_NAME, "keywrd2"))
            .withVertex("source1", v -> v.withTimId("id5").withOutgoingRelation(RELATION, "target1"))
            .withVertex("source2", v -> v.withTimId("id6").withOutgoingRelation(RELATION_2, "target2"))
            .build();

    Facet facet = instance.getFacet(graph.traversal().V());

    assertThat(facet.getOptions(),
            containsInAnyOrder(new Facet.DefaultOption(VALUE1, 1), new Facet.DefaultOption(VALUE2, 1)));
  }

  @Test
  public void getFacetAddsAnUniqueSourceTargetVertexCombination() {
    DerivedListFacetDescription instance =
            new DerivedListFacetDescription(FACET_NAME, PROPERTY, RELATION_NAME, parser, RELATION, RELATION_2);

    Graph graph = newGraph()
            .withVertex("keywrd1", v -> v.withTimId("id1").withProperty(PROPERTY, VALUE1))
            .withVertex("keywrd2", v -> v.withTimId("id2").withProperty(PROPERTY, VALUE2))
            .withVertex("target1", v -> v.withTimId("id3").withOutgoingRelation(RELATION_NAME, "keywrd1"))
            .withVertex("target2", v -> v.withTimId("id4").withOutgoingRelation(RELATION_NAME, "keywrd2"))
            .withVertex("source1", v -> v.withTimId("id5").withOutgoingRelation(RELATION, "target1"))
            .withVertex("source2", v -> v.withTimId("id6").withOutgoingRelation(RELATION_2, "target2"))
            .withVertex("source3", v -> v.withTimId("id7").withOutgoingRelation(RELATION_2, "target1"))
            .build();


    Facet facet = instance.getFacet(graph.traversal().V());

    assertThat(facet.getOptions(), containsInAnyOrder(
            new Facet.DefaultOption(VALUE1, 2), // one connection with source1 and one with source2
            new Facet.DefaultOption(VALUE2, 1))); // one connection with source2
  }

  @Test
  public void getFacetOnlyCountsUniqueSources() {
    DerivedListFacetDescription instance =
            new DerivedListFacetDescription(FACET_NAME, PROPERTY, RELATION_NAME, parser, RELATION);


    Graph graph = newGraph()
            .withVertex("keywrd1", v -> v.withTimId("id1").withProperty(PROPERTY, VALUE1))
            .withVertex("keywrd2", v -> v.withTimId("id2").withProperty(PROPERTY, VALUE2))
            .withVertex("target1", v -> v.withTimId("id3").withOutgoingRelation(RELATION_NAME, "keywrd1"))
            .withVertex("target2", v -> v.withTimId("id4").withOutgoingRelation(RELATION_NAME, "keywrd1"))
            .withVertex("source1", v -> v.withTimId("id5")
                    .withOutgoingRelation(RELATION, "target1").withOutgoingRelation(RELATION, "target2"))
            .withVertex("source2", v -> v.withTimId("id6").withOutgoingRelation(RELATION, "target1"))
            .build();

    Facet facet = instance.getFacet(graph.traversal().V());

    assertThat(facet.getOptions(), containsInAnyOrder(
            new Facet.DefaultOption(VALUE1, 2))); // ...yet source2 should only be counted once
  }
}
