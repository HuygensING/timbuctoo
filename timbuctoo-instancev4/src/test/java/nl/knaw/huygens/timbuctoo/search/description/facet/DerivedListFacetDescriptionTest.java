package nl.knaw.huygens.timbuctoo.search.description.facet;

import com.google.common.collect.Lists;
import nl.knaw.huygens.timbuctoo.search.description.PropertyParser;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static nl.knaw.huygens.timbuctoo.search.MockVertexBuilder.vertex;
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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class DerivedListFacetDescriptionTest {

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
    DerivedListFacetDescription instance =
      new DerivedListFacetDescription(FACET_NAME, PROPERTY, parser, RELATION);

    Facet facet = instance.getFacet(Lists.newArrayList(vertex().build()));

    assertThat(facet, allOf(
      hasProperty("name", equalTo(FACET_NAME)),
      hasProperty("type", equalTo("LIST"))));
  }

  @Test
  public void getFacetReturnsTheFacetWithAnEmptyOptionsListWhenTheVerticesListIsEmpty() {
    DerivedListFacetDescription instance =
      new DerivedListFacetDescription(FACET_NAME, PROPERTY, parser, RELATION);

    Facet facet = instance.getFacet(Lists.newArrayList());

    assertThat(facet.getOptions(), is(empty()));
  }

  @Test
  public void getFacetReturnsTheFacetWithAnEmptyOptionsListWhenTheVerticesDoNotContainTheRelation() {
    DerivedListFacetDescription instance =
      new DerivedListFacetDescription(FACET_NAME, PROPERTY, parser, RELATION);
    List<Vertex> vertices = Lists.newArrayList(vertex().build(), vertex().build());

    Facet facet = instance.getFacet(vertices);

    assertThat(facet.getOptions(), is(empty()));
  }

  @Test
  public void getFacetReturnsTheFacetWithAnEmptyOptionsListWhenTheRelatedVerticesDoNotContainTheProperty() {
    DerivedListFacetDescription instance =
      new DerivedListFacetDescription(FACET_NAME, PROPERTY, parser, RELATION);
    List<Vertex> vertices = Lists.newArrayList(
      vertex().withOutgoingRelation(RELATION, vertex().build()).build(),
      vertex().withOutgoingRelation(RELATION, vertex().build()).build());

    Facet facet = instance.getFacet(vertices);

    assertThat(facet.getOptions(), is(empty()));
  }

  @Test
  public void getFacetAddsTheDifferentValuesToTheOptionsList() {
    DerivedListFacetDescription instance =
      new DerivedListFacetDescription(FACET_NAME, PROPERTY, parser, RELATION);
    List<Vertex> vertices = Lists.newArrayList(
      vertex().withOutgoingRelation(RELATION, vertex().withProperty(PROPERTY, VALUE1).build()).build(),
      vertex().withOutgoingRelation(RELATION, vertex().withProperty(PROPERTY, VALUE2).build()).build());

    Facet facet = instance.getFacet(vertices);

    assertThat(facet.getOptions(),
      containsInAnyOrder(new Facet.DefaultOption(VALUE1, 1), new Facet.DefaultOption(VALUE2, 1)));
  }

  @Test
  public void getFacetLetsTheParserParseEachValue() {
    DerivedListFacetDescription instance =
      new DerivedListFacetDescription(FACET_NAME, PROPERTY, parser, RELATION);
    List<Vertex> vertices = Lists.newArrayList(
      vertex().withOutgoingRelation(RELATION, vertex().withProperty(PROPERTY, VALUE1).build()).build(),
      vertex().withOutgoingRelation(RELATION, vertex().withProperty(PROPERTY, VALUE2).build()).build(),
      vertex().withOutgoingRelation(RELATION, vertex().withProperty(PROPERTY, VALUE2).build()).build());

    instance.getFacet(vertices);

    verify(parser).parse(VALUE1);
    verify(parser, times(2)).parse(VALUE2);
  }

  @Test
  public void getFacetGroupsTheCountsOfOneValue() {
    DerivedListFacetDescription instance =
      new DerivedListFacetDescription(FACET_NAME, PROPERTY, parser, RELATION);
    List<Vertex> vertices = Lists.newArrayList(
      vertex().withOutgoingRelation(RELATION, vertex().withProperty(PROPERTY, VALUE1).build()).build(),
      vertex().withOutgoingRelation(RELATION, vertex().withProperty(PROPERTY, VALUE1).build()).build());

    Facet facet = instance.getFacet(vertices);

    assertThat(facet.getOptions(), containsInAnyOrder(new Facet.DefaultOption(VALUE1, 2)));
  }

  @Test
  public void getFacetAddsTheValueOfEachRelatedVertex() {
    DerivedListFacetDescription instance =
      new DerivedListFacetDescription(FACET_NAME, PROPERTY, parser, RELATION);
    List<Vertex> vertices = Lists.newArrayList(
      vertex().withOutgoingRelation(RELATION, vertex().withProperty(PROPERTY, VALUE1).build())
              .withOutgoingRelation(RELATION, vertex().withProperty(PROPERTY, VALUE2).build()).build());

    Facet facet = instance.getFacet(vertices);

    assertThat(facet.getOptions(),
      containsInAnyOrder(new Facet.DefaultOption(VALUE1, 1), new Facet.DefaultOption(VALUE2, 1)));
  }

  @Test
  public void getFacetAddsTheValueOfEachRelationType() {
    DerivedListFacetDescription instance =
      new DerivedListFacetDescription(FACET_NAME, PROPERTY, parser, RELATION, RELATION_2);
    List<Vertex> vertices = Lists.newArrayList(
      vertex().withOutgoingRelation(RELATION, vertex().withProperty(PROPERTY, VALUE1).build())
              .withOutgoingRelation(RELATION_2, vertex().withProperty(PROPERTY, VALUE2).build()).build());

    Facet facet = instance.getFacet(vertices);

    assertThat(facet.getOptions(),
      containsInAnyOrder(new Facet.DefaultOption(VALUE1, 1), new Facet.DefaultOption(VALUE2, 1)));
  }


}
