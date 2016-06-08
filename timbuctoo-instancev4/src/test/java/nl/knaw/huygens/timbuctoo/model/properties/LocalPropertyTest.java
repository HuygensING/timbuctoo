package nl.knaw.huygens.timbuctoo.model.properties;

import javaslang.control.Try;
import nl.knaw.huygens.timbuctoo.experimental.exports.excel.description.ExcelDescription;
import nl.knaw.huygens.timbuctoo.model.properties.converters.Converter;
import nl.knaw.huygens.timbuctoo.util.TestGraphBuilder;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.junit.Test;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class LocalPropertyTest {


  @Test
  public void getExcelDescriptionCreatesATraversalWhichInvokesConverterTinkerpopToExcelMethod() throws IOException {
    final String propertyName = "testProp";
    final String propertyValue = "testValue";
    final String propertyType = "type";
    Converter converter = mock(Converter.class);
    LocalProperty instance = new LocalProperty(propertyName, converter);
    GraphTraversal<?, Try<ExcelDescription>> traversal = instance.getExcelDescription();

    given(converter.getTypeIdentifier()).willReturn(propertyType);
    ExcelDescription shouldBeReached = mock(ExcelDescription.class);
    given(converter.tinkerPopToExcel(propertyValue, propertyType)).willReturn(shouldBeReached);

    Graph graph = TestGraphBuilder.newGraph().withVertex(v -> {
      v.withProperty(propertyName, propertyValue);
    }).build();

    Try<ExcelDescription> result = graph.traversal().V().union(traversal).next();

    verify(converter, atLeastOnce()).getTypeIdentifier();
    verify(converter, atLeastOnce()).tinkerPopToExcel(propertyValue, propertyType);

    assertThat(result.get(), equalTo(shouldBeReached));
  }

}
