package nl.knaw.huygens.timbuctoo.search.description.facet;

import com.google.common.collect.Lists;
import nl.knaw.huygens.timbuctoo.search.description.PropertyParser;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static nl.knaw.huygens.timbuctoo.search.MockVertexBuilder.vertex;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
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
  private DerivedListFacetDescription instance;
  private PropertyParser parser;

  @Before
  public void setUp() throws Exception {
    parser = mock(PropertyParser.class);
    given(parser.parse(anyString())).willAnswer(invocation -> invocation.getArguments()[0]);
    instance = new DerivedListFacetDescription(FACET_NAME, PROPERTY, parser, RELATION);
  }

  @Test
  public void getFacetReturnsTheFacetWithItsName() {
    String facetName = FACET_NAME;
    DerivedListFacetDescription instance =
      new DerivedListFacetDescription(facetName, PROPERTY, mock(PropertyParser.class), RELATION);

    Facet facet = instance.getFacet(Lists.newArrayList(vertex().build()));

    assertThat(facet, is(notNullValue()));
    assertThat(facet.getName(), is(facetName));
  }

  @Test
  public void getFacetReturnsTheFacetWithAnEmptyOptionsListWhenTheVerticesListIsEmpty() {
    Facet facet = instance.getFacet(Lists.newArrayList());

    assertThat(facet.getOptions(), is(empty()));
  }

  @Test
  public void getFacetReturnsTheFacetWithAnEmptyOptionsListWhenTheVerticesDoNotContainTheRelation() {
    List<Vertex> vertices = Lists.newArrayList(vertex().build(), vertex().build());

    Facet facet = instance.getFacet(vertices);

    assertThat(facet.getOptions(), is(empty()));
  }

  @Test
  public void getFacetReturnsTheFacetWithAnEmptyOptionsListWhenTheRelatedVerticesDoNotContainTheProperty() {
    List<Vertex> vertices = Lists.newArrayList(
      vertex().withOutgoingRelation(RELATION, vertex().build()).build(),
      vertex().withOutgoingRelation(RELATION, vertex().build()).build());

    Facet facet = instance.getFacet(vertices);

    assertThat(facet.getOptions(), is(empty()));
  }

  @Test
  public void getFacetAddsTheDifferentValuesToTheOptionsList() {
    List<Vertex> vertices = Lists.newArrayList(
      vertex().withOutgoingRelation(RELATION, vertex().withProperty(PROPERTY, VALUE1).build()).build(),
      vertex().withOutgoingRelation(RELATION, vertex().withProperty(PROPERTY, VALUE2).build()).build());

    Facet facet = instance.getFacet(vertices);

    assertThat(facet.getOptions(), containsInAnyOrder(new Facet.Option(VALUE1, 1), new Facet.Option(VALUE2, 1)));
  }

  @Test
  public void getFacetLetsTheParserParseEachValue() {
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
    List<Vertex> vertices = Lists.newArrayList(
      vertex().withOutgoingRelation(RELATION, vertex().withProperty(PROPERTY, VALUE1).build()).build(),
      vertex().withOutgoingRelation(RELATION, vertex().withProperty(PROPERTY, VALUE1).build()).build());

    Facet facet = instance.getFacet(vertices);

    assertThat(facet.getOptions(), containsInAnyOrder(new Facet.Option(VALUE1, 2)));
  }

  @Test
  public void getFacetAddsTheValueOfEachRelatedVertex() {
    List<Vertex> vertices = Lists.newArrayList(
      vertex().withOutgoingRelation(RELATION, vertex().withProperty(PROPERTY, VALUE1).build())
              .withOutgoingRelation(RELATION, vertex().withProperty(PROPERTY, VALUE2).build()).build());

    Facet facet = instance.getFacet(vertices);

    assertThat(facet.getOptions(), containsInAnyOrder(new Facet.Option(VALUE1, 1), new Facet.Option(VALUE2, 1)));
  }


}
