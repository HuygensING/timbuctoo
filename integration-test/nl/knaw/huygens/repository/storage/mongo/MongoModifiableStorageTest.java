package nl.knaw.huygens.repository.storage.mongo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nl.knaw.huygens.repository.config.DocTypeRegistry;
import nl.knaw.huygens.repository.storage.mongo.model.TestSystemDocument;
import nl.knaw.huygens.repository.variation.model.TestConcreteDoc;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.common.collect.Lists;
import com.mongodb.MongoException;

public class MongoModifiableStorageTest extends MongoStorageTestBase {
  private MongoModifiableStorage instance;
  private static DocTypeRegistry docTypeRegistry;

  private void setUpDatabase() throws IOException {
    List<TestSystemDocument> docs = Lists.newArrayList(createTestSystemDocument("doc1", "testValue", "testValue2"), createTestSystemDocument("doc2", "testValue", "testValue2"),
        createTestSystemDocument("doc3", "testValue", "testValue2"));
    instance.addItems(TestSystemDocument.class, docs);
  }

  protected TestSystemDocument createTestSystemDocument(String name, String testValue1, String testValue2) {
    TestSystemDocument doc = new TestSystemDocument();
    doc.setName(name);
    doc.setTestValue1(testValue1);
    doc.setTestValue2(testValue2);
    return doc;
  }

  @BeforeClass
  public static void setUpDocTypeRegistry() {
    docTypeRegistry = new DocTypeRegistry(TestSystemDocument.class.getPackage().getName());
  }

  @Before
  public void setUp() throws UnknownHostException, MongoException {
    instance = new MongoModifiableStorage(storageConfiguration, docTypeRegistry);
  }

  @After
  public void tearDown() {
    instance.db.dropDatabase();
    instance.destroy();
    instance = null;
  }

  @Test
  public void testSearchItemOneSearchProperty() throws IOException {
    setUpDatabase();

    Map<String, String> searchProperties = new HashMap<String, String>();
    searchProperties.put("name", "doc1");

    TestSystemDocument actual = instance.searchItem(TestSystemDocument.class, searchProperties);

    assertEquals("doc1", actual.getName());
  }

  @Test
  public void testSearchItemMultipleSearchProperties() throws IOException {
    setUpDatabase();

    Map<String, String> searchProperties = new HashMap<String, String>();
    searchProperties.put("name", "doc2");
    searchProperties.put("testValue1", "testValue");

    TestSystemDocument actual = instance.searchItem(TestSystemDocument.class, searchProperties);

    assertEquals("doc2", actual.getName());
    assertEquals("testValue", actual.getTestValue1());
  }

  @Test
  public void testSearchItemMultipleFound() throws IOException {
    setUpDatabase();

    Map<String, String> searchProperties = new HashMap<String, String>();
    searchProperties.put("testValue1", "testValue");

    TestSystemDocument actual = instance.searchItem(TestSystemDocument.class, searchProperties);

    assertEquals("doc1", actual.getName());
    assertEquals("testValue", actual.getTestValue1());
  }

  @Test
  public void testSearchItemNothingFound() throws IOException {
    setUpDatabase();

    Map<String, String> searchProperties = new HashMap<String, String>();
    searchProperties.put("name", "nonexisting");

    TestSystemDocument actual = instance.searchItem(TestSystemDocument.class, searchProperties);

    assertNull(actual);
  }

  @Test
  public void testSearchItemUnknownCollection() throws IOException {
    setUpDatabase();

    Map<String, String> searchProperties = new HashMap<String, String>();
    searchProperties.put("name", "nonexisting");

    TestConcreteDoc actual = instance.searchItem(TestConcreteDoc.class, searchProperties);

    assertNull(actual);
  }
}
