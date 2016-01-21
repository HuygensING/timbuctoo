package nl.knaw.huygens.timbuctoo.server.search;

import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.junit.Before;
import org.junit.Test;

import static nl.knaw.huygens.timbuctoo.server.search.MockVertexBuilder.vertex;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class SinglePropDescriptorTest {

  public static final String PROP_NAME = "propName";
  private PropGetter propGetter;
  private PropParser dataPropParser;
  private SinglePropDescriptor instance;

  @Before
  public void setUp() throws Exception {
    propGetter = mock(PropGetter.class);
    dataPropParser = mock(PropParser.class);
    instance = new SinglePropDescriptor(propGetter, dataPropParser);
  }

  @Test
  public void getRetrievesTheValueFromTheDataPropGetterAndLetsItParseByTheDataPropParser() {
    String value = "value";
    Vertex vertex = vertex().build();
    given(propGetter.get(vertex)).willReturn(value);

    instance.get(vertex);

    verify(propGetter).get(vertex);
    verify(dataPropParser).parse(value);
  }

  @Test
  public void getReturnsStringValueFromDataPropParserParse() {
    given(dataPropParser.parse(anyString())).willReturn("value");

    String result = instance.get(vertex().build());

    assertThat(result, is(notNullValue()));
  }


}
