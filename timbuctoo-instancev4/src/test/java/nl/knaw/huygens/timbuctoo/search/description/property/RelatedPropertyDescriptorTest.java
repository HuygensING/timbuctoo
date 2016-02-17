package nl.knaw.huygens.timbuctoo.search.description.property;

import nl.knaw.huygens.timbuctoo.search.description.PropertyParser;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.junit.Test;

import static nl.knaw.huygens.timbuctoo.search.MockVertexBuilder.vertex;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class RelatedPropertyDescriptorTest {

  @Test
  public void getReturnsNullIfTheVertexDoesNotHaveRelationOfRelationName() {
    RelatedPropertyDescriptor instance =
      new RelatedPropertyDescriptor("relationName", "propertyName", mock(PropertyParser.class));
    Vertex vertex = vertex().build();

    String actual = instance.get(vertex);

    assertThat(actual, is(nullValue()));
  }

  @Test
  public void getLetsTheParserProcessTheValueIfOneRelationFound() {
    PropertyParser parser = mock(PropertyParser.class);
    given(parser.parse(anyString())).willReturn("parsedValue");
    String propertyName = "propertyName";
    String relationName = "relationName";
    RelatedPropertyDescriptor instance = new RelatedPropertyDescriptor(relationName, propertyName, parser);
    String derivedPropValue = "propValue";
    Vertex vertex = vertex()
      .withOutgoingRelation(relationName, vertex().withProperty(propertyName, derivedPropValue).build())
      .build();

    String actual = instance.get(vertex);

    assertThat(actual, is(notNullValue()));
    verify(parser).parse(derivedPropValue);
  }

  @Test
  public void getReturnsSemiColonSeparatedParsedValuesOfTheRelationsProperty() {
    PropertyParser parser = mock(PropertyParser.class);
    given((parser.parse(anyString()))).willReturn("parsed");
    String propertyName = "propertyName";
    String relationName = "relationName";
    RelatedPropertyDescriptor instance = new RelatedPropertyDescriptor(relationName, propertyName, parser);
    String derivedPropValue = "val";
    Vertex vertex = vertex()
      .withOutgoingRelation(relationName, vertex().withProperty(propertyName, derivedPropValue).build())
      .withOutgoingRelation(relationName, vertex().withProperty(propertyName, derivedPropValue).build())
      .build();

    String actual = instance.get(vertex);

    assertThat(actual, is(equalTo("parsed;parsed")));
    verify(parser, times(2)).parse(anyString());
  }

  @Test
  public void getOrdersTheValuesAlphabetically() {
    PropertyParser parser = mock(PropertyParser.class);
    given((parser.parse(anyString()))).willAnswer(invocationOnMock -> invocationOnMock.getArguments()[0]);
    String propertyName = "propertyName";
    String relationName = "relationName";
    RelatedPropertyDescriptor instance = new RelatedPropertyDescriptor(relationName, propertyName, parser);
    Vertex vertex = vertex()
      .withOutgoingRelation(relationName, vertex().withProperty(propertyName, "def").build())
      .withOutgoingRelation(relationName, vertex().withProperty(propertyName, "abc").build())
      .withOutgoingRelation(relationName, vertex().withProperty(propertyName, "ghi").build())
      .build();

    String actual = instance.get(vertex);

    assertThat(actual, is("abc;def;ghi"));
  }
}
