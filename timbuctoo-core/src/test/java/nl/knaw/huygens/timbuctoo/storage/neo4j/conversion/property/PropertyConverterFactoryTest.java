package nl.knaw.huygens.timbuctoo.storage.neo4j.conversion.property;

import static nl.knaw.huygens.timbuctoo.storage.neo4j.conversion.FieldType.REGULAR;
import static nl.knaw.huygens.timbuctoo.storage.neo4j.conversion.FieldType.VIRTUAL;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;

import nl.knaw.huygens.timbuctoo.model.Entity;
<<<<<<< Updated upstream:timbuctoo-core/src/test/java/nl/knaw/huygens/timbuctoo/storage/neo4j/conversion/property/PropertyConverterFactoryTest.java
import nl.knaw.huygens.timbuctoo.storage.neo4j.conversion.FieldType;
import nl.knaw.huygens.timbuctoo.storage.neo4j.conversion.PropertyConverter;
import nl.knaw.huygens.timbuctoo.storage.neo4j.conversion.property.NoOpPropertyConverter;
import nl.knaw.huygens.timbuctoo.storage.neo4j.conversion.property.ObjectValuePropertyConverter;
import nl.knaw.huygens.timbuctoo.storage.neo4j.conversion.property.PropertyConverterFactory;
import nl.knaw.huygens.timbuctoo.storage.neo4j.conversion.property.SimpleCollectionPropertyConverter;
import nl.knaw.huygens.timbuctoo.storage.neo4j.conversion.property.SimpleValuePropertyConverter;
=======
import nl.knaw.huygens.timbuctoo.storage.graph.neo4j.conversion.FieldType;
import nl.knaw.huygens.timbuctoo.storage.graph.neo4j.conversion.PropertyConverter;
>>>>>>> Stashed changes:timbuctoo-core/src/test/java/nl/knaw/huygens/timbuctoo/storage/graph/neo4j/conversion/property/PropertyConverterFactoryTest.java

import org.junit.Before;
import org.junit.Test;

import test.model.TestSystemEntityWrapper;

public class PropertyConverterFactoryTest {
  private static final String FIELD_NAME = "fieldName";
  private static final Class<TestSystemEntityWrapper> TYPE = TestSystemEntityWrapper.class;
  private static final Class<ObjectValuePropertyConverter> OBJECT_CONVERTER_TYPE = ObjectValuePropertyConverter.class;
  private static final Class<SimpleValuePropertyConverter> SIMPLE_VALUE_CONVERTER_TYPE = SimpleValuePropertyConverter.class;
  private static final Class<NoOpPropertyConverter> NO_OP_CONVERTER_TYPE = NoOpPropertyConverter.class;
  @SuppressWarnings("rawtypes")
  private static final Class<SimpleCollectionPropertyConverter> SIMPLE_COLLECTION_FIELD_CONVERTER_TYPE = SimpleCollectionPropertyConverter.class;
  private PropertyConverterFactory instance;
  private PropertyBusinessRules propertyBusinessRulesMock;

  @Before
  public void setUp() {
    propertyBusinessRulesMock = mock(PropertyBusinessRules.class);

    instance = new PropertyConverterFactory(propertyBusinessRulesMock) {
      @Override
      protected PropertyConverter createSimpleValuePropertyConverter() {
        return mock(SIMPLE_VALUE_CONVERTER_TYPE);
      }

      @Override
      protected PropertyConverter createObjectValuePropertyConverter() {
        return mock(OBJECT_CONVERTER_TYPE);
      }

      @Override
      protected PropertyConverter createNoOpPropertyConverter() {
        return mock(NO_OP_CONVERTER_TYPE);
      }

      @Override
      protected <T> PropertyConverter createSimpleCollectionPropertyConverter(Class<T> componentType) {
        return mock(SIMPLE_COLLECTION_FIELD_CONVERTER_TYPE);
      }

    };
  }

  @Test
  public void createForCreatesASimpleValuePropertyConverterIfTheFieldContainsAString() throws Exception {
    Field stringField = getField(TYPE, "stringValue");

    testCreateFor(stringField, REGULAR, SIMPLE_VALUE_CONVERTER_TYPE);
  }

  @Test
  public void createForCreatesASimpleValuePropertyConverterIfTheFieldContainsAPrimitive() throws Exception {
    Field intField = getField(TYPE, "primitiveValue");

    testCreateFor(intField, REGULAR, SIMPLE_VALUE_CONVERTER_TYPE);
  }

  @Test
  public void wrapCreatesASimpleValuePropertyConverterIfTheFieldContainsAPrimitiveWrapper() throws Exception {
    Field longWrapperField = getField(TYPE, "primitiveWrapperValue");

    testCreateFor(longWrapperField, REGULAR, SIMPLE_VALUE_CONVERTER_TYPE);
  }

  @Test
  public void createForCreatesASimpleCollectionPropertyConverterIfTheFieldContainsAPrimitiveCollection() throws Exception {
    Field primitiveCollectionField = getField(TYPE, "primitiveCollection");

    testCreateFor(primitiveCollectionField, REGULAR, SIMPLE_COLLECTION_FIELD_CONVERTER_TYPE);
  }

  @Test
  public void wrapCreatesASimpleCollectionPropertyConverterIfTheFieldContainsAStringCollection() throws Exception {
    Field stringCollectionField = getField(TYPE, "stringCollection");

    testCreateFor(stringCollectionField, REGULAR, SIMPLE_COLLECTION_FIELD_CONVERTER_TYPE);
  }

  @Test
  public void createForCreatesAnObjectPropertyConverterIfTheFieldContainsAnObjectValue() throws Exception {
    Field objectField = getField(TYPE, "objectValue");

    testCreateFor(objectField, REGULAR, OBJECT_CONVERTER_TYPE);
  }

  @Test
  public void createForCreatesAnObjectPropertyConverterIfTheFieldContainsAnObjectCollection() throws Exception {
    Field objectField = getField(TYPE, "objectCollection");

    testCreateFor(objectField, REGULAR, OBJECT_CONVERTER_TYPE);
  }

  @Test
  public void createForCreatesAnObjectPropertyConverterIfTheFieldContainsAMap() throws Exception {
    Field objectField = getField(TYPE, "map");

    testCreateFor(objectField, REGULAR, OBJECT_CONVERTER_TYPE);
  }

  @Test
  public void createForCreatesANoOpPropertyConverterIfTheFieldTypeIsVirtual() throws Exception {
    Field field = getField(TYPE, "dbIgnoreAnnotatedProperty");

    testCreateFor(field, VIRTUAL, NO_OP_CONVERTER_TYPE);
  }

  private void testCreateFor(Field field, FieldType fieldType, Class<? extends PropertyConverter> expectedConverterType) {
    when(propertyBusinessRulesMock.getFieldType(TYPE, field)).thenReturn(fieldType);
    when(propertyBusinessRulesMock.getFieldName(TYPE, field)).thenReturn(FIELD_NAME);

    // action
    PropertyConverter propertyConverter = instance.createFor(TYPE, field);

    // verify
    assertThat(propertyConverter, is(instanceOf(expectedConverterType)));

    verify(propertyConverter).setField(field);
    verify(propertyConverter).setContainingType(TYPE);
    verify(propertyConverter).setFieldType(fieldType);
    verify(propertyConverter).setName(FIELD_NAME);
  }

  private Field getField(Class<? extends Entity> type, String fieldName) throws NoSuchFieldException {
    return type.getDeclaredField(fieldName);
  }
}
