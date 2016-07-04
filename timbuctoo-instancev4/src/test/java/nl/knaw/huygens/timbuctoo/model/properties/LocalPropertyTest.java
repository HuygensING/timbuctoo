package nl.knaw.huygens.timbuctoo.model.properties;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import javaslang.control.Try;
import nl.knaw.huygens.timbuctoo.experimental.exports.excel.description.ExcelDescription;
import nl.knaw.huygens.timbuctoo.model.properties.converters.Converter;
import nl.knaw.huygens.timbuctoo.model.properties.converters.StringArrayToEncodedArrayOfLimitedValues;
import nl.knaw.huygens.timbuctoo.util.TestGraphBuilder;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static nl.knaw.huygens.timbuctoo.model.properties.LocalProperty.DATABASE_PROPERTY_NAME;
import static nl.knaw.huygens.timbuctoo.model.properties.LocalProperty.OPTIONS_PROPERTY_NAME;
import static nl.knaw.huygens.timbuctoo.model.properties.ReadableProperty.CLIENT_PROPERTY_NAME;
import static nl.knaw.huygens.timbuctoo.model.properties.ReadableProperty.DATABASE_LABEL;
import static nl.knaw.huygens.timbuctoo.model.properties.ReadableProperty.PROPERTY_TYPE_NAME;
import static nl.knaw.huygens.timbuctoo.util.TestGraphBuilder.newGraph;
import static nl.knaw.huygens.timbuctoo.util.VertexMatcher.likeVertex;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class LocalPropertyTest {
  private Graph graph;

  @Before
  public void setUp() {
    graph = newGraph().build();
  }


  @Test
  public void getExcelDescriptionCreatesATraversalWhichInvokesConverterTinkerpopToExcelMethod() throws IOException {
    final String propertyName = "testProp";
    final String propertyValue = "testValue";
    final String propertyType = "type";
    Converter converter = mock(Converter.class);
    LocalProperty instance = new LocalProperty(propertyName, converter);
    GraphTraversal<?, Try<ExcelDescription>> traversal = instance.getExcelDescription();

    given(converter.getGuiTypeId()).willReturn(propertyType);
    ExcelDescription shouldBeReached = mock(ExcelDescription.class);
    given(converter.tinkerPopToExcel(propertyValue, propertyType)).willReturn(shouldBeReached);

    Graph graph = TestGraphBuilder.newGraph().withVertex(v -> {
      v.withProperty(propertyName, propertyValue);
    }).build();

    Try<ExcelDescription> result = graph.traversal().V().union(traversal).next();

    verify(converter, atLeastOnce()).getGuiTypeId();
    verify(converter, atLeastOnce()).tinkerPopToExcel(propertyValue, propertyType);

    assertThat(result.get(), equalTo(shouldBeReached));
  }

  @Test
  public void saveCreatesANewVertexWithCorrectLabelAndProperties() {
    final String propertyName = "testProp";
    final String propertyType = "type";
    final String clientPropName = "clientPropName";

    Converter converter = mock(Converter.class);
    given(converter.getUniqueTypeIdentifier()).willReturn(propertyType);

    LocalProperty property = new LocalProperty(propertyName, converter);

    Vertex result = property.save(graph, clientPropName);

    assertThat(result, likeVertex()
      .withLabel(DATABASE_LABEL)
      .withProperty(DATABASE_PROPERTY_NAME, propertyName)
      .withProperty(CLIENT_PROPERTY_NAME, clientPropName)
      .withProperty(PROPERTY_TYPE_NAME, propertyType)
    );
  }

  @Test
  public void saveSetsOptionsPropertyWhenTheConverterHasOptions() throws JsonProcessingException {
    final String propertyName = "testProp";
    final String clientPropName = "clientPropName";
    final String[] options = {
      "val1",
      "val2"
    };

    Converter converter = new StringArrayToEncodedArrayOfLimitedValues(options);

    LocalProperty property = new LocalProperty(propertyName, converter);

    Vertex result = property.save(graph, clientPropName);

    assertThat(result, likeVertex()
      .withProperty(OPTIONS_PROPERTY_NAME, new ObjectMapper().writeValueAsString(options))
      .withProperty(PROPERTY_TYPE_NAME, converter.getUniqueTypeIdentifier())
    );
  }
}
