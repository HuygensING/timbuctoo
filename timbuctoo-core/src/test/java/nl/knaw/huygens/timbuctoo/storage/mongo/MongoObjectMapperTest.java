package nl.knaw.huygens.timbuctoo.storage.mongo;

import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.Map;

import nl.knaw.huygens.timbuctoo.model.MongoObjectMapperEntity;

import org.junit.BeforeClass;
import org.junit.Test;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class MongoObjectMapperTest {
  private static final String DEFAULT_ID = "testID";
  private static final String DEFAULT_PROP_WITH_ANNOTATED_ACCESSORS = "propWithAnnotatedAccessors";
  private static final String DEFAULT_ANNOTATED_PROPERTY = "annotatedProperty";
  private static final String DEFAULT_TEST_VALUE2 = "testValue2";
  private static final String DEFAULT_TEST_VALUE1 = "testValue1";
  private static final String DEFAULT_NAME = "name";
  private static final Class<MongoObjectMapperEntity> TYPE = MongoObjectMapperEntity.class;
  private static MongoObjectMapper instance;

  @BeforeClass
  public static void setUpClass() {
    instance = new MongoObjectMapper();
  }

  @Test
  public void testMapObject() {
    MongoObjectMapperEntity testObject = createMongoObjectMapperEntity(DEFAULT_NAME, DEFAULT_TEST_VALUE1, DEFAULT_TEST_VALUE2, DEFAULT_ANNOTATED_PROPERTY, DEFAULT_PROP_WITH_ANNOTATED_ACCESSORS);

    Map<String, Object> actual = instance.mapObject(TYPE, testObject, false);

    Map<String, Object> expected = Maps.newHashMap();
    expected.put("name", DEFAULT_NAME);
    expected.put("testValue1", DEFAULT_TEST_VALUE1);
    expected.put("testValue2", DEFAULT_TEST_VALUE2);
    expected.put("propAnnotated", DEFAULT_ANNOTATED_PROPERTY);
    expected.put("pwaa", DEFAULT_PROP_WITH_ANNOTATED_ACCESSORS);

    assertEquals(expected, actual);

  }

  @Test
  public void testMapObjectWithNullValues() {
    MongoObjectMapperEntity testObject = createMongoObjectMapperEntity(DEFAULT_NAME, DEFAULT_TEST_VALUE1, DEFAULT_TEST_VALUE2, null, null);

    Map<String, Object> actual = instance.mapObject(TYPE, testObject, false);

    Map<String, Object> expected = Maps.newHashMap();
    expected.put("name", DEFAULT_NAME);
    expected.put("testValue1", DEFAULT_TEST_VALUE1);
    expected.put("testValue2", DEFAULT_TEST_VALUE2);

    assertEquals(expected, actual);
  }

  @Test
  public void testMapObjectWithFieldsFromSuperClass() {
    MongoObjectMapperEntity testObject = createMongoObjectMapperEntity(DEFAULT_NAME, DEFAULT_TEST_VALUE1, DEFAULT_TEST_VALUE2, DEFAULT_ANNOTATED_PROPERTY, DEFAULT_PROP_WITH_ANNOTATED_ACCESSORS);
    testObject.setId(DEFAULT_ID);

    Map<String, Object> actual = instance.mapObject(TYPE, testObject, true);

    Map<String, Object> expected = Maps.newHashMap();
    expected.put("_id", DEFAULT_ID);
    expected.put("^rev", 0);
    expected.put("_deleted", false);
    expected.put("name", DEFAULT_NAME);
    expected.put("testValue1", DEFAULT_TEST_VALUE1);
    expected.put("testValue2", DEFAULT_TEST_VALUE2);
    expected.put("propAnnotated", DEFAULT_ANNOTATED_PROPERTY);
    expected.put("pwaa", DEFAULT_PROP_WITH_ANNOTATED_ACCESSORS);

    assertEquals(expected, actual);

  }

  @Test
  public void testMapObjectWithPrimitiveCollectionFields() {
    MongoObjectMapperEntity testObject = createMongoObjectMapperEntity(DEFAULT_NAME, DEFAULT_TEST_VALUE1, DEFAULT_TEST_VALUE2, DEFAULT_ANNOTATED_PROPERTY, DEFAULT_PROP_WITH_ANNOTATED_ACCESSORS);
    List<String> primitiveList = Lists.newArrayList("String1", "String2", "String3", "String4");
    testObject.setPrimitiveTestCollection(primitiveList);

    Map<String, Object> actual = instance.mapObject(TYPE, testObject, false);

    Map<String, Object> expected = Maps.newHashMap();
    expected.put("name", DEFAULT_NAME);
    expected.put("testValue1", DEFAULT_TEST_VALUE1);
    expected.put("testValue2", DEFAULT_TEST_VALUE2);
    expected.put("propAnnotated", DEFAULT_ANNOTATED_PROPERTY);
    expected.put("pwaa", DEFAULT_PROP_WITH_ANNOTATED_ACCESSORS);
    expected.put("primitiveTestCollection", primitiveList);

    assertEquals(expected, actual);
  }

  @Test
  public void testMapObjectWithNonPrimitiveCollectionFields() {
    MongoObjectMapperEntity testObject = createMongoObjectMapperEntity(DEFAULT_NAME, DEFAULT_TEST_VALUE1, DEFAULT_TEST_VALUE2, DEFAULT_ANNOTATED_PROPERTY, DEFAULT_PROP_WITH_ANNOTATED_ACCESSORS);
    MongoObjectMapperEntity testObject1 = createMongoObjectMapperEntity(DEFAULT_NAME, DEFAULT_TEST_VALUE1, DEFAULT_TEST_VALUE2, DEFAULT_ANNOTATED_PROPERTY, DEFAULT_PROP_WITH_ANNOTATED_ACCESSORS);
    MongoObjectMapperEntity testObject2 = createMongoObjectMapperEntity(DEFAULT_NAME, DEFAULT_TEST_VALUE1, DEFAULT_TEST_VALUE2, DEFAULT_ANNOTATED_PROPERTY, DEFAULT_PROP_WITH_ANNOTATED_ACCESSORS);
    MongoObjectMapperEntity testObject3 = createMongoObjectMapperEntity(DEFAULT_NAME, DEFAULT_TEST_VALUE1, DEFAULT_TEST_VALUE2, DEFAULT_ANNOTATED_PROPERTY, DEFAULT_PROP_WITH_ANNOTATED_ACCESSORS);
    testObject.setId(DEFAULT_ID);
    testObject.setNonPrimitiveTestCollection(Lists.newArrayList(testObject1, testObject2, testObject3));

    Map<String, Object> actual = instance.mapObject(TYPE, testObject, false);

    Map<String, Object> expected = Maps.newHashMap();
    expected.put("name", DEFAULT_NAME);
    expected.put("testValue1", DEFAULT_TEST_VALUE1);
    expected.put("testValue2", DEFAULT_TEST_VALUE2);
    expected.put("propAnnotated", DEFAULT_ANNOTATED_PROPERTY);
    expected.put("pwaa", DEFAULT_PROP_WITH_ANNOTATED_ACCESSORS);

    assertEquals(expected, actual);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testMapObjectTypeNull() {
    MongoObjectMapperEntity testObject = createMongoObjectMapperEntity(DEFAULT_NAME, DEFAULT_TEST_VALUE1, DEFAULT_TEST_VALUE2, DEFAULT_ANNOTATED_PROPERTY, DEFAULT_PROP_WITH_ANNOTATED_ACCESSORS);

    instance.mapObject(null, testObject, false);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testMapObjectObjectNull() {
    instance.mapObject(TYPE, null, false);
  }

  private MongoObjectMapperEntity createMongoObjectMapperEntity(String name, String testValue1, String testValue2, String annotatedProperty, String propWithAnnotatedAccessors) {
    MongoObjectMapperEntity doc = new MongoObjectMapperEntity();
    doc.setName(name);
    doc.setTestValue1(testValue1);
    doc.setTestValue2(testValue2);
    doc.setAnnotatedProperty(annotatedProperty);
    doc.setPropWithAnnotatedAccessors(propWithAnnotatedAccessors);
    return doc;
  }
}
