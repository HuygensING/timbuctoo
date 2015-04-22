package nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.model.util.Change;
import nl.knaw.huygens.timbuctoo.storage.graph.ConversionException;
import nl.knaw.huygens.timbuctoo.storage.graph.neo4j.conversion.FieldType;

import org.junit.Before;
import org.junit.Test;

import test.model.TestSystemEntityWrapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tinkerpop.blueprints.Vertex;

public class ObjectValuePropertyConverterTest implements PropertyConverterTest {
  private static final Change DEFAULT_VALUE = new Change(87l, "userId", "vreId");
  private static final FieldType FIELD_TYPE = FieldType.REGULAR;
  private static final String FIELD_NAME = "objectValue";
  private static final Class<TestSystemEntityWrapper> TYPE = TestSystemEntityWrapper.class;
  private TestSystemEntityWrapper entity;
  private String propertyName;
  private ObjectValuePropertyConverter instance;
  private Vertex vertexMock;

  @Before
  public void setUp() throws Exception {
    vertexMock = mock(Vertex.class);
    entity = new TestSystemEntityWrapper();
    propertyName = FIELD_TYPE.propertyName(TYPE, FIELD_NAME);

    instance = new ObjectValuePropertyConverter();
    setupInstance(instance);
  }

  private void setupInstance(ObjectValuePropertyConverter objectValueFieldWrapper) throws Exception {
    objectValueFieldWrapper.setContainingType(TYPE);
    objectValueFieldWrapper.setField(TYPE.getDeclaredField(FIELD_NAME));
    objectValueFieldWrapper.setFieldType(FIELD_TYPE);
    objectValueFieldWrapper.setName(FIELD_NAME);
  }

  @Test
  @Override
  public void setValueOfVertexSetsThePropertyWithTheFieldNameToTheValueOfTheNode() throws Exception {
    // setup
    String serializedValue = serializeValue(DEFAULT_VALUE);

    entity.setObjectValue(DEFAULT_VALUE);

    // action
    instance.setValueOfVertex(vertexMock, entity);

    // verify
    verify(vertexMock).setProperty(propertyName, serializedValue);
  }

  private String serializeValue(Change change) throws JsonProcessingException {
    ObjectMapper objectMapper = new ObjectMapper();
    String serializedValue = objectMapper.writeValueAsString(change);
    return serializedValue;
  }

  @Test
  @Override
  public void setValueOfVertexDoesNotSetIfTheValueIsNull() throws Exception {
    // setup
    entity.setObjectValue(null);

    // action
    instance.setValueOfVertex(vertexMock, entity);

    // verify
    verifyZeroInteractions(vertexMock);
  }

  @Test(expected = ConversionException.class)
  @Override
  public void setValueOfVertexThrowsAConversionExceptionIfGetFieldValueThrowsAnIllegalAccessException() throws Exception {
    // setup
    ObjectValuePropertyConverter instance = new ObjectValuePropertyConverter() {
      @Override
      protected Object getValue(Entity entity) throws IllegalAccessException, IllegalArgumentException {
        throw new IllegalAccessException();
      }
    };

    setupInstance(instance);

    instance.setValueOfVertex(vertexMock, entity);
  }

  @Test(expected = ConversionException.class)
  @Override
  public void setValueOfVertexThrowsAConversionExceptionIfGetFieldValueThrowsAnIllegalArgumentExceptionIsThrown() throws Exception {
    // setup
    ObjectValuePropertyConverter instance = new ObjectValuePropertyConverter() {
      @Override
      protected Object getValue(Entity entity) throws IllegalAccessException, IllegalArgumentException {
        throw new IllegalArgumentException();
      }
    };

    setupInstance(instance);

    // action
    instance.setValueOfVertex(vertexMock, entity);
  }

  @Test(expected = ConversionException.class)
  public void setValueOfVertexThrowsAConversionExceptionIfFormatThrowsAnIllegalArgumentException() throws Exception {
    // setup
    entity.setObjectValue(DEFAULT_VALUE);

    ObjectValuePropertyConverter instance = new ObjectValuePropertyConverter() {
      @Override
      protected Object format(Object value) throws IllegalArgumentException {
        throw new IllegalArgumentException();
      }
    };

    setupInstance(instance);

    // action
    instance.setValueOfVertex(vertexMock, entity);
  }
}
