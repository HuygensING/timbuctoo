package nl.knaw.huygens.timbuctoo.server.search.propertygetter;

import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.junit.Before;
import org.junit.Test;

import static nl.knaw.huygens.timbuctoo.server.search.MockVertexBuilder.vertex;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

public class LocalPropertyGetterTest {

  public static final String PROP_NAME = "testProp";
  private LocalPropertyGetter instance;

  @Before
  public void setUp() throws Exception {
    instance = new LocalPropertyGetter(PROP_NAME);
  }

  @Test
  public void getReturnsNullIfTheVertexDoesNotContainTheProperty() {
    LocalPropertyGetter instance = new LocalPropertyGetter(PROP_NAME);

    String value = instance.get(vertex().build());

    assertThat(value, is(nullValue()));
  }

  @Test
  public void getReturnsTheValueOfTheVertexPropertyWithTheGivenPropertyName() {
    String propValue = "propval";
    Vertex vertex = vertex()
      .withProperty(PROP_NAME, propValue)
      .build();

    String result = instance.get(vertex);

    assertThat(result, is(propValue));
  }
}
