package nl.knaw.huygens.timbuctoo.server.search.property;

import nl.knaw.huygens.timbuctoo.server.search.PropertyParser;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.junit.Test;

import static nl.knaw.huygens.timbuctoo.server.search.MockVertexBuilder.vertex;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class LocalPropertyDescriptorTest {

  @Test
  public void getReturnsNullIfTheVertexDoesNotContainTheProperty() {
    LocalPropertyDescriptor instance = new LocalPropertyDescriptor("property", mock(PropertyParser.class));

    String value = instance.get(vertex().build());

    assertThat(value, is(nullValue()));
  }

  @Test
  public void getReturnsValueFromParserParseIfTheVertexDoesContainTheProperty() {
    String propertyName = "propName";
    String expectedValue = "a string";
    PropertyParser parser = mock(PropertyParser.class);
    given(parser.parse(anyString())).willReturn(expectedValue);
    Vertex vertex = vertex().withProperty(propertyName, "a string 2").build();
    LocalPropertyDescriptor instance = new LocalPropertyDescriptor(propertyName, parser);

    String value = instance.get(vertex);

    assertThat(value, is(equalTo(expectedValue)));
    verify(parser).parse(anyString());
  }

}
