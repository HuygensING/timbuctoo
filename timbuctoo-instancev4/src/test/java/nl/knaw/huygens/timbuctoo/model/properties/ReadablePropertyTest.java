package nl.knaw.huygens.timbuctoo.model.properties;

import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.junit.Before;
import org.junit.Test;

import static nl.knaw.huygens.timbuctoo.model.properties.ReadableProperty.CLIENT_PROPERTY_NAME;
import static nl.knaw.huygens.timbuctoo.model.properties.ReadableProperty.DATABASE_LABEL;
import static nl.knaw.huygens.timbuctoo.model.properties.ReadableProperty.PROPERTY_TYPE_NAME;
import static nl.knaw.huygens.timbuctoo.util.TestGraphBuilder.newGraph;
import static nl.knaw.huygens.timbuctoo.util.VertexMatcher.likeVertex;
import static org.hamcrest.MatcherAssert.assertThat;

public class ReadablePropertyTest {
  private Graph graph;

  @Before
  public void setUp() {
    graph = newGraph().build();
  }

  @Test
  public void saveCreatesANewVertexWithCorrectLabelAndProperties() {
    ReadableProperty readableProperty = new ReadableProperty(null) {
      @Override
      public String getTypeId() {
        return null;
      }

      @Override
      public String getUniqueTypeId() {
        return "type-id";
      }
    };

    Vertex result = readableProperty.save(graph, "client-prop-name");

    assertThat(result, likeVertex()
      .withProperty(CLIENT_PROPERTY_NAME, "client-prop-name")
      .withProperty(PROPERTY_TYPE_NAME, "type-id")
      .withLabel(DATABASE_LABEL)
    );
  }
}
