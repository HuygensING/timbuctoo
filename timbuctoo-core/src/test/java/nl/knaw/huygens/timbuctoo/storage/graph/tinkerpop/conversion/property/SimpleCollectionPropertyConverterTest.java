package nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.conversion.property;

import com.google.common.collect.Lists;
import com.tinkerpop.blueprints.Vertex;
import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.storage.graph.ConversionException;
import nl.knaw.huygens.timbuctoo.storage.graph.FieldType;
import nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.PropertyConverterTest;
import org.junit.Before;
import org.junit.Test;
import test.model.TestSystemEntityWrapper;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SimpleCollectionPropertyConverterTest implements PropertyConverterTest {

  private static final String STRINGIFIED_COLLECTION = "[1,2,3,4]";
  private static final Class<Integer> COMPONENT_TYPE = Integer.class;
  private static final int VALUE_4 = 4;
  private static final int VALUE_3 = 3;
  private static final int VALUE_2 = 2;
  private static final int VALUE_1 = 1;
  private static final FieldType FIELD_TYPE = FieldType.REGULAR;
  private static final Class<TestSystemEntityWrapper> CONTAINING_TYPE = TestSystemEntityWrapper.class;
  private static final String FIELD_NAME = "primitiveCollection";
  public static final String PROPERTY_NAME = "completePropertyName";
  private String completePropertyName;
  private SimpleCollectionPropertyConverter<Integer> instance;
  private Vertex vertexMock;
  private TestSystemEntityWrapper entity;

  @Before
  public void setUp() throws NoSuchFieldException {
    instance = new SimpleCollectionPropertyConverter<>(COMPONENT_TYPE);
    setupInstance(instance);

    completePropertyName = FIELD_TYPE.completePropertyName(CONTAINING_TYPE, PROPERTY_NAME);

    vertexMock = mock(Vertex.class);
    entity = new TestSystemEntityWrapper();
  }

  private void setupInstance(SimpleCollectionPropertyConverter<Integer> simpleCollectionFieldWrapper) throws NoSuchFieldException {
    simpleCollectionFieldWrapper.setField(CONTAINING_TYPE.getDeclaredField(FIELD_NAME));
    simpleCollectionFieldWrapper.setFieldType(FIELD_TYPE);
    simpleCollectionFieldWrapper.setContainingType(CONTAINING_TYPE);
    simpleCollectionFieldWrapper.setFieldName(FIELD_NAME);
    simpleCollectionFieldWrapper.setPropertyName(PROPERTY_NAME);
  }

  @Test
  @Override
  public void setPropertyOfElementSetsThePropertyWithTheFieldNameToTheValueOfTheNode() throws Exception {
    // setup
    List<Integer> value = Lists.newArrayList(VALUE_1, VALUE_2, VALUE_3, VALUE_4);
    entity.setPrimitiveCollection(value);

    // action
    instance.setPropertyOfElement(vertexMock, entity);

    // verify
    verify(vertexMock).setProperty(completePropertyName, STRINGIFIED_COLLECTION);
  }

  @Test
  @Override
  public void setPropertyOfElementRemovesThePropertyIfTheValueIsNull() throws Exception {
    // setup
    entity.setPrimitiveCollection(null);

    // action
    instance.setPropertyOfElement(vertexMock, entity);

    // verify
    verify(vertexMock).removeProperty(completePropertyName);
  }

  @Test
  public void setValueOfVertexDoesNotSetIfTheValueIsEmpty() throws Exception {
    // setup
    entity.setPrimitiveCollection(Lists.<Integer> newArrayList());

    // action
    instance.setPropertyOfElement(vertexMock, entity);

    // verify
    verify(vertexMock).removeProperty(completePropertyName);
  }

  @Test(expected = ConversionException.class)
  @Override
  public void setPropertyOfElementThrowsAConversionExceptionIfGetFieldValueThrowsAnIllegalAccessException() throws Exception {
    // setup
    SimpleCollectionPropertyConverter<Integer> instance = new SimpleCollectionPropertyConverter<Integer>(COMPONENT_TYPE) {
      @Override
      protected Object getValue(Entity entity) throws IllegalAccessException, IllegalArgumentException {
        throw new IllegalAccessException();
      }
    };

    setupInstance(instance);

    // action
    instance.setPropertyOfElement(vertexMock, entity);

  }

  @Test(expected = ConversionException.class)
  @Override
  public void setPropertyOfElementThrowsAConversionExceptionIfGetFieldValueThrowsAnIllegalArgumentExceptionIsThrown() throws Exception {
    // setup
    SimpleCollectionPropertyConverter<Integer> instance = new SimpleCollectionPropertyConverter<Integer>(COMPONENT_TYPE) {
      @Override
      protected Object getValue(Entity entity) throws IllegalAccessException, IllegalArgumentException {
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
    when(vertexMock.getProperty(completePropertyName)).thenReturn(STRINGIFIED_COLLECTION);

    // action
    instance.addValueToEntity(entity, vertexMock);

    // verify
    assertThat(entity.getPrimitiveCollection(), contains(VALUE_1, VALUE_2, VALUE_3, VALUE_4));
  }

  @Test
  @Override
  public void addValueToEntityAddsNullWhenTheValueIsNull() throws Exception {
    // setup
    when(vertexMock.getProperty(completePropertyName)).thenReturn(null);

    // action
    instance.addValueToEntity(entity, vertexMock);

    // verify
    assertThat(entity.getPrimitiveCollection(), is(nullValue()));

  }

  @Test(expected = ConversionException.class)
  @Override
  public void addValueToEntityThrowsAConversionExceptionWhenFillFieldThrowsAnIllegalAccessExceptionIsThrown() throws Exception {
    // setup
    SimpleCollectionPropertyConverter<Integer> instance = new SimpleCollectionPropertyConverter<Integer>(COMPONENT_TYPE) {
      @Override
      protected void fillField(Entity entity, Object value) throws IllegalAccessException, IllegalArgumentException {
        throw new IllegalArgumentException();
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
    SimpleCollectionPropertyConverter<Integer> instance = new SimpleCollectionPropertyConverter<Integer>(COMPONENT_TYPE) {
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
