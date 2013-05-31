package nl.knaw.huygens.repository.storage.mongo;

import static org.junit.Assert.assertEquals;

import java.util.Map;

import nl.knaw.huygens.repository.storage.mongo.model.MongoObjectMapperDocument;

import org.junit.BeforeClass;
import org.junit.Test;

public class MongoObjectMapperTest {
  private static MongoObjectMapper instance;

  @BeforeClass
  public static void setUpClass() {
    instance = new MongoObjectMapper();
  }

  @Test
  public void testMapObject() {
    MongoObjectMapperDocument testObject = createMongoObjectMapperDocument("name", "testValue1", "testValue2", "annotatedProperty", "propWithAnnotatedAccessors");

    Map<String, String> mappedObject = instance.mapObject(MongoObjectMapperDocument.class, testObject);

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

    Map<String, String> mappedObject = instance.mapObject(MongoObjectMapperDocument.class, testObject);

    assertEquals(3, mappedObject.size());
    assertEquals("name", mappedObject.get("name"));
    assertEquals("testValue1", mappedObject.get("testValue1"));
    assertEquals("testValue2", mappedObject.get("testValue2"));
  }

  @Test
  public void testMapObjectWithFieldsFromSuperClass() {
    MongoObjectMapperDocument testObject = createMongoObjectMapperDocument("name", "testValue1", "testValue2", "annotatedProperty", "propWithAnnotatedAccessors");
    testObject.setId("testID");

    Map<String, String> mappedObject = instance.mapObject(MongoObjectMapperDocument.class, testObject);

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
    instance.mapObject(MongoObjectMapperDocument.class, null);
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
