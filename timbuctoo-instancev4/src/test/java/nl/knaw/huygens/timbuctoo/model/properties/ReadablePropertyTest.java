package nl.knaw.huygens.timbuctoo.model.properties;

import com.fasterxml.jackson.databind.ObjectMapper;
import nl.knaw.huygens.timbuctoo.model.properties.converters.HasOptions;
import nl.knaw.huygens.timbuctoo.model.properties.converters.StringToUnencodedStringOfLimitedValuesConverter;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import static nl.knaw.huygens.timbuctoo.model.properties.ReadableProperty.CLIENT_PROPERTY_NAME;
import static nl.knaw.huygens.timbuctoo.model.properties.ReadableProperty.DATABASE_LABEL;
import static nl.knaw.huygens.timbuctoo.model.properties.ReadableProperty.PROPERTY_TYPE_NAME;
import static nl.knaw.huygens.timbuctoo.util.TestGraphBuilder.newGraph;
import static nl.knaw.huygens.timbuctoo.util.VertexMatcher.likeVertex;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;

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

  @Test
  public void loadLoadsAPropertyConfiguration()
    throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException,
    IOException {

    final String[] options = {"a", "b"};
    final Vertex vertex = graph.addVertex(DATABASE_LABEL);
    final String typeIdentifier = new StringToUnencodedStringOfLimitedValuesConverter(new String[]{})
      .getUniqueTypeIdentifier();
    vertex.property(CLIENT_PROPERTY_NAME, "clientName");
    vertex.property(PROPERTY_TYPE_NAME, typeIdentifier);
    vertex.property(LocalProperty.OPTIONS_PROPERTY_NAME, new ObjectMapper().writeValueAsString(options));
    vertex.property(LocalProperty.DATABASE_PROPERTY_NAME, "dbName");

    ReadableProperty result = ReadableProperty.load(vertex);

    assertThat(result, instanceOf(LocalProperty.class));
    assertThat(result.getUniqueTypeId(), equalTo(typeIdentifier));
    assertThat(((LocalProperty) result).getOptions().get(), contains("a", "b"));
  }
}
