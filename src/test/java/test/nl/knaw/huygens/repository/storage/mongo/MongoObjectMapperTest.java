package nl.knaw.huygens.repository.storage.mongo;

import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.Map;

import nl.knaw.huygens.repository.storage.mongo.model.MongoObjectMapperDocument;

import org.junit.BeforeClass;
import org.junit.Test;

import com.google.common.collect.Lists;

public class MongoObjectMapperTest {
  private static final Class<MongoObjectMapperDocument> TYPE = MongoObjectMapperDocument.class;
  private static MongoObjectMapper instance;

  @BeforeClass
  public static void setUpClass() {
    instance = new MongoObjectMapper();
  }

  @Test
  public void testMapObject() {
    MongoObjectMapperDocument testObject = createMongoObjectMapperDocument("name", "testValue1", "testValue2", "annotatedProperty", "propWithAnnotatedAccessors");

    Map<String, Object> mappedObject = instance.mapObject(TYPE, testObject);

    assertEquals(5, mappedObject.size());
    assertEquals("name", mappedObject.get("name"));
    assertEquals("testValue1", mappedObject.get("testValue1"));
    assertEquals("testValue2", mappedObject.get("testValue2"));
    assertEquals("annotatedProperty", mappedObject.get("propAnnotated"));
    assertEquals("propWithAnnotatedAccessors", mappedObject.get("pwaa"));

  }

  @Test
  public void testMapObjectWithNullValues() {
    MongoObjectMapperDocument testObject = createMongoObjectMapperDocument("name", "testValue1", "testValue2", null, null);

    Map<String, Object> mappedObject = instance.mapObject(TYPE, testObject);

    assertEquals(3, mappedObject.size());
    assertEquals("name", mappedObject.get("name"));
    assertEquals("testValue1", mappedObject.get("testValue1"));
    assertEquals("testValue2", mappedObject.get("testValue2"));
  }

  @Test
  public void testMapObjectWithFieldsFromSuperClass() {
    MongoObjectMapperDocument testObject = createMongoObjectMapperDocument("name", "testValue1", "testValue2", "annotatedProperty", "propWithAnnotatedAccessors");
    testObject.setId("testID");

    Map<String, Object> mappedObject = instance.mapObject(TYPE, testObject);

    assertEquals(5, mappedObject.size());
    assertEquals("name", mappedObject.get("name"));
    assertEquals("testValue1", mappedObject.get("testValue1"));
    assertEquals("testValue2", mappedObject.get("testValue2"));
    assertEquals("annotatedProperty", mappedObject.get("propAnnotated"));
    assertEquals("propWithAnnotatedAccessors", mappedObject.get("pwaa"));

  }

  @Test
  public void testMapObjectWithPrimitiveCollectionFields() {
    MongoObjectMapperDocument testObject = createMongoObjectMapperDocument("name", "testValue1", "testValue2", "annotatedProperty", "propWithAnnotatedAccessors");
    testObject.setId("testID");
    List<String> primitiveList = Lists.newArrayList("String1", "String2", "String3", "String4");
    testObject.setPrimitiveTestCollection(primitiveList);

    Map<String, Object> mappedObject = instance.mapObject(TYPE, testObject);

    assertEquals(6, mappedObject.size());
    assertEquals("name", mappedObject.get("name"));
    assertEquals("testValue1", mappedObject.get("testValue1"));
    assertEquals("testValue2", mappedObject.get("testValue2"));
    assertEquals("annotatedProperty", mappedObject.get("propAnnotated"));
    assertEquals("propWithAnnotatedAccessors", mappedObject.get("pwaa"));
    assertEquals(primitiveList, mappedObject.get("primitiveTestCollection"));
  }

  @Test
  public void testMapObjectWithNonPrimitiveCollectionFields() {
    MongoObjectMapperDocument testObject = createMongoObjectMapperDocument("name", "testValue1", "testValue2", "annotatedProperty", "propWithAnnotatedAccessors");
    MongoObjectMapperDocument testObject1 = createMongoObjectMapperDocument("name", "testValue1", "testValue2", "annotatedProperty", "propWithAnnotatedAccessors");
    MongoObjectMapperDocument testObject2 = createMongoObjectMapperDocument("name", "testValue1", "testValue2", "annotatedProperty", "propWithAnnotatedAccessors");
    MongoObjectMapperDocument testObject3 = createMongoObjectMapperDocument("name", "testValue1", "testValue2", "annotatedProperty", "propWithAnnotatedAccessors");
    testObject.setId("testID");
    testObject.setNonPrimitiveTestCollection(Lists.newArrayList(testObject1, testObject2, testObject3));

    Map<String, Object> mappedObject = instance.mapObject(TYPE, testObject);

    assertEquals(5, mappedObject.size());
    assertEquals("name", mappedObject.get("name"));
    assertEquals("testValue1", mappedObject.get("testValue1"));
    assertEquals("testValue2", mappedObject.get("testValue2"));
    assertEquals("annotatedProperty", mappedObject.get("propAnnotated"));
    assertEquals("propWithAnnotatedAccessors", mappedObject.get("pwaa"));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testMapObjectTypeNull() {
    MongoObjectMapperDocument testObject = createMongoObjectMapperDocument("name", "testValue1", "testValue2", "annotatedProperty", "propWithAnnotatedAccessors");

    instance.mapObject(null, testObject);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testMapObjectObjectNull() {
    instance.mapObject(TYPE, null);
  }

  private MongoObjectMapperDocument createMongoObjectMapperDocument(String name, String testValue1, String testValue2, String annotatedProperty, String propWithAnnotatedAccessors) {
    MongoObjectMapperDocument doc = new MongoObjectMapperDocument();
    doc.setName(name);
    doc.setTestValue1(testValue1);
    doc.setTestValue2(testValue2);
    doc.setAnnotatedProperty(annotatedProperty);
    doc.setPropWithAnnotatedAccessors(propWithAnnotatedAccessors);
    return doc;
  }
}
