package nl.knaw.huygens.timbuctoo.tools.conversion;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import nl.knaw.huygens.timbuctoo.model.Person;
import nl.knaw.huygens.timbuctoo.storage.graph.ConversionException;
import nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.VertexConverter;
import nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.conversion.ElementConverterFactory;

import org.junit.Before;
import org.junit.Test;

import com.tinkerpop.blueprints.Vertex;

public class VariationConverterTest {
  private ElementConverterFactory converterFactory;
  private VariationConverter instance;

  @Before
  public void setup() {
    converterFactory = mock(ElementConverterFactory.class);
    instance = new VariationConverter(converterFactory);
  }

  @SuppressWarnings("unchecked")
  @Test
  public void addDataToVertexUsesAElementConverterToAddTheData() throws ConversionException {
    VertexConverter<Person> converter = mock(VertexConverter.class);
    when(converterFactory.forType(Person.class)).thenReturn(converter);

    Person variant = new Person();
    Vertex vertex = mock(Vertex.class);

    // action
    instance.addDataToVertex(vertex, variant);

    // verify
    verify(converter).addValuesToElement(vertex, variant);
  }

}
