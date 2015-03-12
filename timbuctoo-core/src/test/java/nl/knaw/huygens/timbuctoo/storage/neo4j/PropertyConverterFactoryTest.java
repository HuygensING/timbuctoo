package nl.knaw.huygens.timbuctoo.storage.neo4j;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;

import nl.knaw.huygens.timbuctoo.model.Entity;

import org.junit.Before;
import org.junit.Test;

import test.model.TestSystemEntityWrapper;

public class PropertyConverterFactoryTest {
  private static final String FIELD_NAME = "fieldName";
  private static final FieldType FIELD_TYPE = FieldType.REGULAR;
  private static final Class<TestSystemEntityWrapper> TYPE = TestSystemEntityWrapper.class;
  private static final Class<ObjectValuePropertyConverter> OBJECT_WRAPPER_TYPE = ObjectValuePropertyConverter.class;
  private static final Class<SimpleValuePropertyConverter> SIMPLE_VALUE_WRAPPER_TYPE = SimpleValuePropertyConverter.class;
  private static final Class<NoOpPropertyConverter> NO_OP_WRAPPER_TYPE = NoOpPropertyConverter.class;
  @SuppressWarnings("rawtypes")
  private static final Class<SimpleCollectionPropertyConverter> SIMPLE_COLLECTION_FIELD_WRAPPER_TYPE = SimpleCollectionPropertyConverter.class;
  private static final TestSystemEntityWrapper TEST_SYSTEM_ENTITY = new TestSystemEntityWrapper();
  private PropertyConverterFactory instance;
  private PropertyBusinessRules propertyBusinessRulesMock;

  @Before
  public void setUp() {
    propertyBusinessRulesMock = mock(PropertyBusinessRules.class);

    instance = new PropertyConverterFactory(propertyBusinessRulesMock) {
      @Override
      protected PropertyConverter createSimpleValuePropertyConverter() {
        return mock(SIMPLE_VALUE_WRAPPER_TYPE);
      }

      @Override
      protected PropertyConverter createObjectValuePropertyConverter() {
        return mock(OBJECT_WRAPPER_TYPE);
      }

      @Override
      protected PropertyConverter createNoOpPropertyConverter() {
        return mock(NO_OP_WRAPPER_TYPE);
      }

      @Override
      protected <T> PropertyConverter createSimpleCollectionPropertyConverter(Class<T> componentType) {
        return mock(SIMPLE_COLLECTION_FIELD_WRAPPER_TYPE);
      }

    };
  }

  @Test
  public void createForCreatesASimpleValueFieldWrapperIfTheFieldContainsAString() throws Exception {
    Field stringField = getField(TYPE, "stringValue");

    testCreateFor(TEST_SYSTEM_ENTITY, stringField, SIMPLE_VALUE_WRAPPER_TYPE);
  }

  @Test
  public void createForCreatesASimpleValueFieldWrapperIfTheFieldContainsAPrimitive() throws Exception {
    Field intField = getField(TYPE, "primitiveValue");

    testCreateFor(TEST_SYSTEM_ENTITY, intField, SIMPLE_VALUE_WRAPPER_TYPE);
  }

  @Test
  public void wrapCreatesASimpleValueFieldWrapperIfTheFieldContainsAPrimitiveWrapper() throws Exception {
    Field longWrapperField = getField(TYPE, "primitiveWrapperValue");

    testCreateFor(TEST_SYSTEM_ENTITY, longWrapperField, SIMPLE_VALUE_WRAPPER_TYPE);
  }

  @Test
  public void createForCreatesASimpleCollectionFieldWrapperIfTheFieldContainsAPrimitiveCollection() throws Exception {
    Field primitiveCollectionField = getField(TYPE, "primitiveCollection");

    testCreateFor(TEST_SYSTEM_ENTITY, primitiveCollectionField, SIMPLE_COLLECTION_FIELD_WRAPPER_TYPE);
  }

  @Test
  public void wrapCreatesASimpleCollectionFieldWrapperIfTheFieldContainsAStringCollection() throws Exception {
    Field stringCollectionField = getField(TYPE, "stringCollection");

    testCreateFor(TEST_SYSTEM_ENTITY, stringCollectionField, SIMPLE_COLLECTION_FIELD_WRAPPER_TYPE);
  }

  @Test
  public void createForCreatesAnObjectFieldWrapperIfTheFieldContainsAnObjectValue() throws Exception {
    Field objectField = getField(TYPE, "objectValue");

    testCreateFor(TEST_SYSTEM_ENTITY, objectField, OBJECT_WRAPPER_TYPE);
  }

  @Test
  public void createForCreatesAnObjectFieldWrapperIfTheFieldContainsAnObjectCollection() throws Exception {
    Field objectField = getField(TYPE, "objectCollection");

    testCreateFor(TEST_SYSTEM_ENTITY, objectField, OBJECT_WRAPPER_TYPE);
  }

  @Test
  public void createForCreatesAnObjectFieldWrapperIfTheFieldContainsAMap() throws Exception {
    Field objectField = getField(TYPE, "map");

    testCreateFor(TEST_SYSTEM_ENTITY, objectField, OBJECT_WRAPPER_TYPE);
  }

  @Test
  public void createForCreatesANoOpFieldWrapperIfTheFieldIsStatic() throws Exception {
    Field field = getField(TYPE, "staticField");
    testCreateFor(TEST_SYSTEM_ENTITY, field, NO_OP_WRAPPER_TYPE);
  }

  private void testCreateFor(TestSystemEntityWrapper testSystemEntity, Field field, Class<? extends PropertyConverter> wrapperType) {
    when(propertyBusinessRulesMock.getFieldType(TYPE, field)).thenReturn(FIELD_TYPE);
    when(propertyBusinessRulesMock.getFieldName(TYPE, field)).thenReturn(FIELD_NAME);

    // action
    PropertyConverter propertyConverter = instance.createFor(TYPE, field);

    // verify
    assertThat(propertyConverter, is(instanceOf(wrapperType)));

    verify(propertyConverter).setField(field);
    verify(propertyConverter).setContainingType(TYPE);
    verify(propertyConverter).setFieldType(FIELD_TYPE);
    verify(propertyConverter).setName(FIELD_NAME);
  }

  private Field getField(Class<? extends Entity> type, String fieldName) throws NoSuchFieldException {
    return type.getDeclaredField(fieldName);
  }
}
