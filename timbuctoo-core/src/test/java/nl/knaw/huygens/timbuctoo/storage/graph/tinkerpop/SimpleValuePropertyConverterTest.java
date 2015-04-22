package nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.storage.graph.ConversionException;
import nl.knaw.huygens.timbuctoo.storage.graph.neo4j.conversion.FieldType;

import org.junit.Before;
import org.junit.Test;

import test.model.TestSystemEntityWrapper;

import com.tinkerpop.blueprints.Vertex;

public class SimpleValuePropertyConverterTest implements PropertyConverterTest {
  private static final String FIELD_VALUE = "test";
  private static final FieldType FIELD_TYPE = FieldType.REGULAR;
  private static final Class<TestSystemEntityWrapper> TYPE = TestSystemEntityWrapper.class;
  private static final String FIELD_NAME = "stringValue";
  private static final String PROPERTY_NAME = FIELD_TYPE.propertyName(TYPE, FIELD_NAME);
  private SimpleValuePropertyConverter instance;
  private TestSystemEntityWrapper entity;
  private Vertex vertexMock;

  @Before
  public void setup() throws NoSuchFieldException {
    instance = new SimpleValuePropertyConverter();
    initializeInstance(instance);

    entity = new TestSystemEntityWrapper();
    entity.setStringValue(FIELD_VALUE);
    vertexMock = mock(Vertex.class);
  }

  private void initializeInstance(SimpleValuePropertyConverter instance) throws NoSuchFieldException {
    instance.setContainingType(TYPE);
    instance.setField(TYPE.getDeclaredField(FIELD_NAME));
    instance.setFieldType(FIELD_TYPE);
    instance.setName(FIELD_NAME);
  }

  @Test
  public void setValueOfVertexSetsThePropertyWithTheFieldNameToTheValueOfTheNode() throws Exception {
    // action
    instance.setValueOfVertex(vertexMock, entity);

    // verify
    verify(vertexMock).setProperty(PROPERTY_NAME, FIELD_VALUE);
  }

  @Test
  public void setValueOfVertexDoesNotSetIfTheValueIsNull() throws Exception {
    // setup
    entity.setStringValue(null);

    // action
    instance.setValueOfVertex(vertexMock, entity);

    // verify
    verifyZeroInteractions(vertexMock);
  }

  @Test(expected = ConversionException.class)
  public void setValueOfVertexThrowsAConversionExceptionIfGetFieldValueThrowsAnIllegalAccessException() throws Exception {
    // setup
    SimpleValuePropertyConverter instance = new SimpleValuePropertyConverter() {
      @Override
      protected Object getValue(Entity entity) throws IllegalAccessException, IllegalArgumentException {
        throw new IllegalAccessException();
      }
    };

    initializeInstance(instance);

    // action
    instance.setValueOfVertex(vertexMock, entity);
  }

  @Test(expected = ConversionException.class)
  public void setValueOfVertexThrowsAConversionExceptionIfGetFieldValueThrowsAnIllegalArgumentExceptionIsThrown() throws Exception {
    // setup
    SimpleValuePropertyConverter instance = new SimpleValuePropertyConverter() {
      @Override
      protected Object getValue(Entity entity) throws IllegalAccessException, IllegalArgumentException {
        throw new IllegalArgumentException();
      }
    };

    initializeInstance(instance);

    // action
    instance.setValueOfVertex(vertexMock, entity);
  }
}
