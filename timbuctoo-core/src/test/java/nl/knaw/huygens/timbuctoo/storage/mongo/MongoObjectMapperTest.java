package nl.knaw.huygens.timbuctoo.storage.mongo;

import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.Map;

import nl.knaw.huygens.timbuctoo.model.MongoObjectMapperEntity;
import nl.knaw.huygens.timbuctoo.model.MongoObjectMapperNested;

import org.junit.BeforeClass;
import org.junit.Test;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class MongoObjectMapperTest {
  // keys
  private static final String PRIMITIVE_TEST_COLLECTION_KEY = "mongoobjectmapperentity.primitiveTestCollection";
  private static final String PWAA_KEY = "mongoobjectmapperentity.pwaa";
  private static final String PROP_ANNOTATED_KEY = "mongoobjectmapperentity.propAnnotated";
  private static final String TEST_VALUE2_KEY = "mongoobjectmapperentity.testValue2";
  private static final String TEST_VALUE1_KEY = "mongoobjectmapperentity.testValue1";
  private static final String NAME_KEY = "mongoobjectmapperentity.name";
  //default values
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
    instance = new MongoObjectMapper(new MongoFieldMapper());
  }

  @Test
  public void testMapObject() {
    MongoObjectMapperEntity testObject = createMongoObjectMapperEntity(DEFAULT_NAME, DEFAULT_TEST_VALUE1, DEFAULT_TEST_VALUE2, DEFAULT_ANNOTATED_PROPERTY, DEFAULT_PROP_WITH_ANNOTATED_ACCESSORS);

    Map<String, Object> actual = instance.mapObject(TYPE, testObject);

    Map<String, Object> expected = Maps.newHashMap();
    expected.put(NAME_KEY, DEFAULT_NAME);
    expected.put(TEST_VALUE1_KEY, DEFAULT_TEST_VALUE1);
    expected.put(TEST_VALUE2_KEY, DEFAULT_TEST_VALUE2);
    expected.put(PROP_ANNOTATED_KEY, DEFAULT_ANNOTATED_PROPERTY);
    expected.put(PWAA_KEY, DEFAULT_PROP_WITH_ANNOTATED_ACCESSORS);

    assertEquals(expected, actual);

  }

  @Test
  public void testMapObjectWithNullValues() {
    MongoObjectMapperEntity testObject = createMongoObjectMapperEntity(DEFAULT_NAME, DEFAULT_TEST_VALUE1, DEFAULT_TEST_VALUE2, null, null);

    Map<String, Object> actual = instance.mapObject(TYPE, testObject);

    Map<String, Object> expected = Maps.newHashMap();
    expected.put(NAME_KEY, DEFAULT_NAME);
    expected.put(TEST_VALUE1_KEY, DEFAULT_TEST_VALUE1);
    expected.put(TEST_VALUE2_KEY, DEFAULT_TEST_VALUE2);

    assertEquals(expected, actual);
  }

  @Test
  public void testMapObjectWithPrimitiveCollectionFields() {
    MongoObjectMapperEntity testObject = createMongoObjectMapperEntity(DEFAULT_NAME, DEFAULT_TEST_VALUE1, DEFAULT_TEST_VALUE2, DEFAULT_ANNOTATED_PROPERTY, DEFAULT_PROP_WITH_ANNOTATED_ACCESSORS);
    List<String> primitiveList = Lists.newArrayList("String1", "String2", "String3", "String4");
    testObject.setPrimitiveTestCollection(primitiveList);

    Map<String, Object> actual = instance.mapObject(TYPE, testObject);

    Map<String, Object> expected = Maps.newHashMap();
    expected.put(NAME_KEY, DEFAULT_NAME);
    expected.put(TEST_VALUE1_KEY, DEFAULT_TEST_VALUE1);
    expected.put(TEST_VALUE2_KEY, DEFAULT_TEST_VALUE2);
    expected.put(PROP_ANNOTATED_KEY, DEFAULT_ANNOTATED_PROPERTY);
    expected.put(PWAA_KEY, DEFAULT_PROP_WITH_ANNOTATED_ACCESSORS);
    expected.put(PRIMITIVE_TEST_COLLECTION_KEY, primitiveList);

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

    Map<String, Object> actual = instance.mapObject(TYPE, testObject);

    Map<String, Object> expected = Maps.newHashMap();
    expected.put(NAME_KEY, DEFAULT_NAME);
    expected.put(TEST_VALUE1_KEY, DEFAULT_TEST_VALUE1);
    expected.put(TEST_VALUE2_KEY, DEFAULT_TEST_VALUE2);
    expected.put(PROP_ANNOTATED_KEY, DEFAULT_ANNOTATED_PROPERTY);
    expected.put(PWAA_KEY, DEFAULT_PROP_WITH_ANNOTATED_ACCESSORS);

    assertEquals(expected, actual);
  }

  @Test
  public void testMapObjectWithNestedObjects() {
    MongoObjectMapperEntity nestedEntity = createMongoObjectMapperEntity(DEFAULT_NAME, DEFAULT_TEST_VALUE1, DEFAULT_TEST_VALUE2, DEFAULT_ANNOTATED_PROPERTY, DEFAULT_PROP_WITH_ANNOTATED_ACCESSORS);
    MongoObjectMapperNested testObject = new MongoObjectMapperNested(nestedEntity);

    Map<String, Object> actual = instance.mapObject(MongoObjectMapperNested.class, testObject);
    Map<String, Object> expected = Maps.newHashMap();
    expected.put("mongoobjectmappernested.nestedEntity.name", DEFAULT_NAME);
    expected.put("mongoobjectmappernested.nestedEntity.testValue1", DEFAULT_TEST_VALUE1);
    expected.put("mongoobjectmappernested.nestedEntity.testValue2", DEFAULT_TEST_VALUE2);
    expected.put("mongoobjectmappernested.nestedEntity.propAnnotated", DEFAULT_ANNOTATED_PROPERTY);
    expected.put("mongoobjectmappernested.nestedEntity.pwaa", DEFAULT_PROP_WITH_ANNOTATED_ACCESSORS);

    assertEquals(expected, actual);

  }

  @Test(expected = IllegalArgumentException.class)
  public void testMapObjectTypeNull() {
    MongoObjectMapperEntity testObject = createMongoObjectMapperEntity(DEFAULT_NAME, DEFAULT_TEST_VALUE1, DEFAULT_TEST_VALUE2, DEFAULT_ANNOTATED_PROPERTY, DEFAULT_PROP_WITH_ANNOTATED_ACCESSORS);

    instance.mapObject(null, testObject);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testMapObjectObjectNull() {
    instance.mapObject(TYPE, null);
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
