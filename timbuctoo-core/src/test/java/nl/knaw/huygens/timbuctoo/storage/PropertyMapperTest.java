package nl.knaw.huygens.timbuctoo.storage;

import static nl.knaw.huygens.timbuctoo.storage.FieldMapper.propertyName;
import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.Map;

import nl.knaw.huygens.timbuctoo.model.util.Datable;
import nl.knaw.huygens.timbuctoo.model.util.PersonName;
import nl.knaw.huygens.timbuctoo.model.util.PersonNameComponent.Type;
import nl.knaw.huygens.timbuctoo.storage.mongo.PersonNameMapper;
import nl.knaw.huygens.timbuctoo.variation.model.MongoObjectMapperEntity;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class PropertyMapperTest {

  // keys
  private static final String PRIMITIVE_TEST_COLLECTION_KEY = propertyName("mongoobjectmapperentity", "primitiveTestCollection");
  private static final String PWAA_KEY = propertyName("mongoobjectmapperentity", "pwaa");
  private static final String PROP_ANNOTATED_KEY = propertyName("mongoobjectmapperentity", "propAnnotated");
  private static final String TEST_VALUE2_KEY = propertyName("mongoobjectmapperentity", "testValue2");
  private static final String TEST_VALUE1_KEY = propertyName("mongoobjectmapperentity", "testValue1");
  private static final String NAME_KEY = propertyName("mongoobjectmapperentity", "name");

  // default values
  private static final String DEFAULT_ID = "testID";
  private static final String DEFAULT_PROP_WITH_ANNOTATED_ACCESSORS = "propWithAnnotatedAccessors";
  private static final String DEFAULT_ANNOTATED_PROPERTY = "annotatedProperty";
  private static final String DEFAULT_TEST_VALUE2 = "testValue2";
  private static final String DEFAULT_TEST_VALUE1 = "testValue1";
  private static final String DEFAULT_NAME = "name";
  private static final Class<MongoObjectMapperEntity> TYPE = MongoObjectMapperEntity.class;

  private PropertyMapper mapper;

  @Before
  public void setUpClass() {
    mapper = new PropertyMapper();
  }

  @Test
  public void testMapObject() {
    MongoObjectMapperEntity testObject = createMongoObjectMapperEntity(DEFAULT_NAME, DEFAULT_TEST_VALUE1, DEFAULT_TEST_VALUE2, DEFAULT_ANNOTATED_PROPERTY, DEFAULT_PROP_WITH_ANNOTATED_ACCESSORS);

    Map<String, Object> actual = mapper.mapObject(TYPE, testObject);

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

    Map<String, Object> actual = mapper.mapObject(TYPE, testObject);

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

    Map<String, Object> actual = mapper.mapObject(TYPE, testObject);

    Map<String, Object> expected = Maps.newHashMap();
    expected.put(NAME_KEY, DEFAULT_NAME);
    expected.put(TEST_VALUE1_KEY, DEFAULT_TEST_VALUE1);
    expected.put(TEST_VALUE2_KEY, DEFAULT_TEST_VALUE2);
    expected.put(PROP_ANNOTATED_KEY, DEFAULT_ANNOTATED_PROPERTY);
    expected.put(PWAA_KEY, DEFAULT_PROP_WITH_ANNOTATED_ACCESSORS);
    expected.put(PRIMITIVE_TEST_COLLECTION_KEY, primitiveList);

    assertEquals(expected, actual);
  }

  @Test(expected = IllegalStateException.class)
  public void testMapObjectWithNonPrimitiveCollectionFields() {
    MongoObjectMapperEntity testObject = createMongoObjectMapperEntity(DEFAULT_NAME, DEFAULT_TEST_VALUE1, DEFAULT_TEST_VALUE2, DEFAULT_ANNOTATED_PROPERTY, DEFAULT_PROP_WITH_ANNOTATED_ACCESSORS);
    testObject.setId(DEFAULT_ID);
    testObject.setNonPrimitiveTestCollection(Lists.newArrayList(testObject));

    mapper.mapObject(TYPE, testObject);
  }

  @Test
  public void testMapObjectWithClass() {
    MongoObjectMapperEntity item = new MongoObjectMapperEntity();
    item.setType(TYPE);

    Map<String, Object> expected = Maps.newHashMap();
    expected.put(propertyName(TYPE, "type"), "nl.knaw.huygens.timbuctoo.variation.model.MongoObjectMapperEntity");

    assertEquals(expected, mapper.mapObject(TYPE, item));
  }

  @Test
  public void testMapObjectWithDatable() {
    MongoObjectMapperEntity item = new MongoObjectMapperEntity();
    item.setDate(new Datable("20031011"));

    Map<String, Object> expected = Maps.newHashMap();
    expected.put(propertyName(TYPE, "date"), "20031011");

    assertEquals(expected, mapper.mapObject(TYPE, item));
  }

  @Test
  public void testMapObjectWithPersonName() {
    MongoObjectMapperEntity item = new MongoObjectMapperEntity();
    PersonName personName = new PersonName();
    personName.addNameComponent(Type.FORENAME, "test");
    personName.addNameComponent(Type.SURNAME, "test");
    item.setPersonName(personName);

    Map<String, Object> expected = Maps.newLinkedHashMap();
    expected.put(propertyName(TYPE, "personName"), PersonNameMapper.createPersonNameMap(personName));

    Map<String, Object> actual = mapper.mapObject(TYPE, item);

    // Use the to string because the maps cannot be compared as map.
    assertEquals(expected.toString(), actual.toString());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testMapObjectObjectNull() {
    mapper.mapObject(TYPE, null);
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

  // --- recursive mapping ---------------------------------------------

  @Test(expected = IllegalArgumentException.class)
  public void testRecursiveMappingWithDependencyError() {
    ClassD item = new ClassD();
    mapper.mapObject(ClassC.class, ClassB.class, item);
  }

  @Test
  public void testRecursiveMapping() {
    ClassD item = new ClassD();

    // See the ClassD instance as ClassC and ClassB
    Map<String, Object> map = mapper.mapObject(ClassB.class, ClassC.class, item);

    Map<String, Object> expected = Maps.newHashMap();
    expected.put(propertyName(ClassB.class, "b"), item.b);
    expected.put(propertyName(ClassC.class, "c"), item.c);

    assertEquals(expected, map);
  }

  static class ClassA {
    public String a = "aa";
  }

  static class ClassB extends ClassA {
    public String b = "bb";
  }

  static class ClassC extends ClassB {
    public String c = "cc";
  }

  static class ClassD extends ClassC {
    public String d = "dd";
  }

}
