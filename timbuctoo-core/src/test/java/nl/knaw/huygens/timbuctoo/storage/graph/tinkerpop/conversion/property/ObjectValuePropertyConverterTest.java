package nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.conversion.property;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tinkerpop.blueprints.Vertex;
import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.model.util.Change;
import nl.knaw.huygens.timbuctoo.storage.graph.ConversionException;
import nl.knaw.huygens.timbuctoo.storage.graph.FieldType;
import nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.PropertyConverterTest;
import org.junit.Before;
import org.junit.Test;
import test.model.TestSystemEntityWrapper;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ObjectValuePropertyConverterTest implements PropertyConverterTest {
  private static final Change DEFAULT_VALUE = new Change(87l, "userId", "vreId");
  private static final FieldType FIELD_TYPE = FieldType.REGULAR;
  private static final String FIELD_NAME = "objectValue";
  private static final Class<TestSystemEntityWrapper> TYPE = TestSystemEntityWrapper.class;
  public static final String PROPERTY_NAME = "propertyName";
  private TestSystemEntityWrapper entity;
  private String completePropertyName;
  private ObjectValuePropertyConverter instance;
  private Vertex vertexMock;

  @Before
  public void setUp() throws Exception {
    vertexMock = mock(Vertex.class);
    entity = new TestSystemEntityWrapper();
    completePropertyName = FIELD_TYPE.completePropertyName(TYPE, PROPERTY_NAME);

    instance = new ObjectValuePropertyConverter();
    setupInstance(instance);
  }

  private void setupInstance(ObjectValuePropertyConverter objectValueFieldWrapper) throws Exception {
    objectValueFieldWrapper.setContainingType(TYPE);
    objectValueFieldWrapper.setField(TYPE.getDeclaredField(FIELD_NAME));
    objectValueFieldWrapper.setFieldType(FIELD_TYPE);
    objectValueFieldWrapper.setFieldName(FIELD_NAME);
    objectValueFieldWrapper.setPropertyName(PROPERTY_NAME);
  }

  @Test
  @Override
  public void setPropertyOfElementSetsThePropertyWithTheFieldNameToTheValueOfTheNode() throws Exception {
    // setup
    String serializedValue = serializeValue(DEFAULT_VALUE);

    entity.setObjectValue(DEFAULT_VALUE);

    // action
    instance.setPropertyOfElement(vertexMock, entity);

    // verify
    verify(vertexMock).setProperty(completePropertyName, serializedValue);
  }

  private String serializeValue(Change change) throws JsonProcessingException {
    ObjectMapper objectMapper = new ObjectMapper();
    String serializedValue = objectMapper.writeValueAsString(change);
    return serializedValue;
  }

  @Test
  @Override
  public void setPropertyOfElementRemovesThePropertyIfTheValueIsNull() throws Exception {
    // setup
    entity.setObjectValue(null);

    // action
    instance.setPropertyOfElement(vertexMock, entity);

    // verify
    verify(vertexMock).removeProperty(completePropertyName);
  }

  @Test(expected = ConversionException.class)
  @Override
  public void setPropertyOfElementThrowsAConversionExceptionIfGetFieldValueThrowsAnIllegalAccessException() throws Exception {
    // setup
    ObjectValuePropertyConverter instance = new ObjectValuePropertyConverter() {
      @Override
      protected Object getValue(Entity entity) throws IllegalAccessException, IllegalArgumentException {
        throw new IllegalAccessException();
      }
    };

    setupInstance(instance);

    instance.setPropertyOfElement(vertexMock, entity);
  }

  @Test(expected = ConversionException.class)
  @Override
  public void setPropertyOfElementThrowsAConversionExceptionIfGetFieldValueThrowsAnIllegalArgumentExceptionIsThrown() throws Exception {
    // setup
    ObjectValuePropertyConverter instance = new ObjectValuePropertyConverter() {
      @Override
      protected Object getValue(Entity entity) throws IllegalAccessException, IllegalArgumentException {
        throw new IllegalArgumentException();
      }
    };

    setupInstance(instance);

    // action
    instance.setPropertyOfElement(vertexMock, entity);
  }

  @Test(expected = ConversionException.class)
  public void setPropertyOfElementThrowsAConversionExceptionIfFormatThrowsAnIllegalArgumentException() throws Exception {
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
    instance.setPropertyOfElement(vertexMock, entity);
  }

  @Test
  @Override
  public void addValueToEntitySetTheFieldOfTheEntityWithTheValue() throws Exception {
    // setup
    when(vertexMock.getProperty(completePropertyName)).thenReturn(serializeValue(DEFAULT_VALUE));

    // action
    instance.addValueToEntity(entity, vertexMock);

    // verify
    assertThat(entity.getObjectValue(), is(DEFAULT_VALUE));
  }

  @Test
  @Override
  public void addValueToEntityAddsNullWhenTheValueIsNull() throws Exception {
    // setup
    when(vertexMock.getProperty(completePropertyName)).thenReturn(null);

    // action
    instance.addValueToEntity(entity, vertexMock);

    // verify
    assertThat(entity.getObjectValue(), is(nullValue()));
  }

  @Test(expected = ConversionException.class)
  @Override
  public void addValueToEntityThrowsAConversionExceptionWhenFillFieldThrowsAnIllegalAccessExceptionIsThrown() throws Exception {
    // setup
    ObjectValuePropertyConverter instance = new ObjectValuePropertyConverter() {
      @Override
      protected void fillField(Entity entity, Object value) throws IllegalAccessException, IllegalArgumentException {
        throw new IllegalAccessException();
      }
    };

    setupInstance(instance);

    // action
    instance.addValueToEntity(entity, vertexMock);
  }

  @Test(expected = ConversionException.class)
  @Override
  public void addValueToEntityThrowsAConversionExceptionWhenFillFieldThrowsAnAnIllegalArgumentExceptionIsThrown() throws Exception {
    // setup
    ObjectValuePropertyConverter instance = new ObjectValuePropertyConverter() {
      @Override
      protected void fillField(Entity entity, Object value) throws IllegalAccessException, IllegalArgumentException {
        throw new IllegalArgumentException();
      }
    };

    setupInstance(instance);

    // action
    instance.addValueToEntity(entity, vertexMock);
  }

  @Test
  public void removeFromRemovesThePropertyFromTheElement() {
    // action
    instance.removeFrom(vertexMock);

    // verify
    verify(vertexMock).removeProperty(completePropertyName);
  }
}
