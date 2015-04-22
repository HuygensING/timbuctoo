package nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop;

import static nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.VertexFields.VERTEX_TYPE;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

import java.util.List;

import nl.knaw.huygens.timbuctoo.config.TypeNames;
import nl.knaw.huygens.timbuctoo.storage.graph.ConversionException;

import org.junit.Before;
import org.junit.Test;

import test.model.TestSystemEntityWrapper;

import com.google.common.collect.Lists;
import com.tinkerpop.blueprints.Vertex;

public class ExtendableVertexConverterTest {
  private PropertyConverter propertyConverter1;
  private PropertyConverter propertyConverter2;
  private List<PropertyConverter> propertyConverters;
  private ExtendableVertexConverter<TestSystemEntityWrapper> instance;
  private Vertex vertexMock;
  private TestSystemEntityWrapper entity;

  @Before
  public void setup() {
    propertyConverter1 = mock(PropertyConverter.class);
    propertyConverter2 = mock(PropertyConverter.class);
    propertyConverters = Lists.newArrayList(propertyConverter1, propertyConverter2);
    instance = new ExtendableVertexConverter<>(propertyConverters);

    vertexMock = mock(Vertex.class);
    entity = new TestSystemEntityWrapper();
  }

  @Test
  public void addValuesToNodeLetsThePropertyConvertersAddTheirValuesToTheNode() throws Exception {
    // action
    instance.addValuesToVertex(vertexMock, entity);

    // verify
    verifyTypeIsSet(vertexMock);
    verify(propertyConverter1).setValueOfVertex(vertexMock, entity);
    verify(propertyConverter2).setValueOfVertex(vertexMock, entity);

  }

  private void verifyTypeIsSet(Vertex vertexMock) {
    verify(vertexMock).setProperty(VERTEX_TYPE, new String[] { TypeNames.getInternalName(TestSystemEntityWrapper.class) });
  }

  @Test(expected = ConversionException.class)
  public void addValuesToNodeFieldMapperThrowsAConversionException() throws Exception {
    // setup
    doThrow(ConversionException.class).when(propertyConverter1).setValueOfVertex(vertexMock, entity);

    try {
      // action
      instance.addValuesToVertex(vertexMock, entity);
    } finally {
      // verify
      verify(propertyConverter1).setValueOfVertex(vertexMock, entity);
      verifyZeroInteractions(propertyConverter2);
    }
  }
}
