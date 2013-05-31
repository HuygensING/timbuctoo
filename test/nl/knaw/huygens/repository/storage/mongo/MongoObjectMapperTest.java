package nl.knaw.huygens.repository.storage.mongo;

import static org.junit.Assert.assertEquals;

import java.util.Map;

import nl.knaw.huygens.repository.storage.mongo.model.TestSystemDocument;

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
    TestSystemDocument testObject = createTestSystemDocument("name", "testValue1", "testValue2", "annotatedProperty", "propWithAnnotatedAccessors");

    Map<String, String> mappedObject = instance.mapObject(TestSystemDocument.class, testObject);

    assertEquals(5, mappedObject.size());
    assertEquals("name", mappedObject.get("name"));
    assertEquals("testValue1", mappedObject.get("testValue1"));
    assertEquals("testValue2", mappedObject.get("testValue2"));
    assertEquals("annotatedProperty", mappedObject.get("propAnnotated"));
    assertEquals("propWithAnnotatedAccessors", mappedObject.get("pwaa"));

  }

  @Test
  public void testMapObjectWithNullValues() {
    TestSystemDocument testObject = createTestSystemDocument("name", "testValue1", "testValue2", null, null);

    Map<String, String> mappedObject = instance.mapObject(TestSystemDocument.class, testObject);

    assertEquals(3, mappedObject.size());
    assertEquals("name", mappedObject.get("name"));
    assertEquals("testValue1", mappedObject.get("testValue1"));
    assertEquals("testValue2", mappedObject.get("testValue2"));
  }

  @Test
  public void testMapObjectWithFieldsFromSuperClass() {
    TestSystemDocument testObject = createTestSystemDocument("name", "testValue1", "testValue2", "annotatedProperty", "propWithAnnotatedAccessors");
    testObject.setId("testID");

    Map<String, String> mappedObject = instance.mapObject(TestSystemDocument.class, testObject);

    assertEquals(5, mappedObject.size());
    assertEquals("name", mappedObject.get("name"));
    assertEquals("testValue1", mappedObject.get("testValue1"));
    assertEquals("testValue2", mappedObject.get("testValue2"));
    assertEquals("annotatedProperty", mappedObject.get("propAnnotated"));
    assertEquals("propWithAnnotatedAccessors", mappedObject.get("pwaa"));

  }

  @Test(expected = IllegalArgumentException.class)
  public void testMapObjectTypeNull() {
    TestSystemDocument testObject = createTestSystemDocument("name", "testValue1", "testValue2", "annotatedProperty", "propWithAnnotatedAccessors");

    instance.mapObject(null, testObject);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testMapObjectObjectNull() {
    instance.mapObject(TestSystemDocument.class, null);
  }

  private TestSystemDocument createTestSystemDocument(String name, String testValue1, String testValue2, String annotatedProperty, String propWithAnnotatedAccessors) {
    TestSystemDocument doc = new TestSystemDocument();
    doc.setName(name);
    doc.setTestValue1(testValue1);
    doc.setTestValue2(testValue2);
    doc.setAnnotatedProperty(annotatedProperty);
    doc.setPropWithAnnotatedAccessors(propWithAnnotatedAccessors);
    return doc;
  }
}
