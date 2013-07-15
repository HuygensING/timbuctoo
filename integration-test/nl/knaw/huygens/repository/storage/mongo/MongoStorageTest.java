package nl.knaw.huygens.repository.storage.mongo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.UnknownHostException;

import nl.knaw.huygens.repository.config.DocTypeRegistry;
import nl.knaw.huygens.repository.storage.mongo.model.TestSystemDocument;
import nl.knaw.huygens.repository.variation.model.TestConcreteDoc;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.common.collect.Sets;
import com.mongodb.MongoException;

public class MongoStorageTest extends MongoStorageTestBase {

  private static DocTypeRegistry docTypeRegistry;

  private MongoStorage storage;

  private void setUpDatabase() throws IOException {
    storage.addItem(TestSystemDocument.class, newTestSystemDocument("doc1", "testValue", "testValue2", "doc1", "doc1"));
    storage.addItem(TestSystemDocument.class, newTestSystemDocument("doc2", "testValue", "testValue2", "doc2", "doc2"));
    storage.addItem(TestSystemDocument.class, newTestSystemDocument("doc3", "testValue", "testValue2", "doc3", "doc3"));
  }

  private TestSystemDocument newTestSystemDocument(String name, String value1, String value2, String annotatedProperty, String propWithAnnotatedAccessors) {
    TestSystemDocument doc = new TestSystemDocument();
    doc.setName(name);
    doc.setTestValue1(value1);
    doc.setTestValue2(value2);
    doc.setAnnotatedProperty(annotatedProperty);
    doc.setPropWithAnnotatedAccessors(propWithAnnotatedAccessors);
    return doc;
  }

  @BeforeClass
  public static void setUpDocTypeRegistry() {
    docTypeRegistry = new DocTypeRegistry(TestSystemDocument.class.getPackage().getName());
  }

  @Before
  public void setUp() throws UnknownHostException, MongoException {
    when(storageConfiguration.getVersionedTypes()).thenReturn(Sets.newHashSet("testsystemdocument"));
    storage = new MongoStorage(storageConfiguration, docTypeRegistry);
  }

  @After
  public void tearDown() {
    storage.db.dropDatabase();
    storage.close();
    storage = null;
  }

  @Test
  public void testSearchItemOneSearchProperty() throws IOException {
    setUpDatabase();

    TestSystemDocument example = new TestSystemDocument();
    example.setName("doc1");

    TestSystemDocument actual = storage.searchItem(TestSystemDocument.class, example);

    assertEquals("doc1", actual.getName());
  }

  @Test
  public void testSearchItemMultipleSearchProperties() throws IOException {
    setUpDatabase();

    TestSystemDocument example = new TestSystemDocument();
    example.setName("doc2");
    example.setTestValue1("testValue");

    TestSystemDocument actual = storage.searchItem(TestSystemDocument.class, example);

    assertEquals("doc2", actual.getName());
    assertEquals("testValue", actual.getTestValue1());
  }

  @Test
  public void testSearchItemMultipleFound() throws IOException {
    setUpDatabase();

    TestSystemDocument example = new TestSystemDocument();
    example.setTestValue1("testValue");

    TestSystemDocument actual = storage.searchItem(TestSystemDocument.class, example);

    assertEquals("doc1", actual.getName());
    assertEquals("testValue", actual.getTestValue1());
  }

  @Test
  public void testSearchItemNothingFound() throws IOException {
    setUpDatabase();

    TestSystemDocument example = new TestSystemDocument();
    example.setName("nonExisting");

    assertNull(storage.searchItem(TestSystemDocument.class, example));
  }

  @Test
  public void testSearchItemUnknownCollection() throws IOException {
    setUpDatabase();

    TestConcreteDoc example = new TestConcreteDoc();
    example.name = "nonExisting";

    assertNull(storage.searchItem(TestConcreteDoc.class, example));
  }

  @Test
  public void testUpdateItem() throws IOException {
    TestSystemDocument doc = new TestSystemDocument();
    String id = "TSD0000000001";
    doc.setId(id);
    doc.setTestValue1("test");

    Class<TestSystemDocument> type = TestSystemDocument.class;
    storage.addItem(type, doc);

    doc.setTestValue1("testValue1");
    storage.updateItem(type, id, doc);

    verifyCollectionSize(1, "testsystemdocument", storage.db);
  }

  @Test(expected = IOException.class)
  public void testUpdateItemNonExistent() throws IOException {
    String id = "TSD0000000001";
    TestSystemDocument expected = new TestSystemDocument();
    expected.setId(id);
    expected.setTestValue1("test");

    storage.updateItem(TestSystemDocument.class, id, expected);
  }

  @Test
  public void testAddItem() throws IOException {
    TestSystemDocument doc = new TestSystemDocument();
    doc.setTestValue1("test");

    Class<TestSystemDocument> type = TestSystemDocument.class;
    storage.addItem(type, doc);

    verifyCollectionSize(1, "testsystemdocument", storage.db);
  }

  @Test
  public void testAddItemWithId() throws IOException {
    TestSystemDocument doc = new TestSystemDocument();
    String id = "TSD0000000001";
    doc.setId(id);
    doc.setTestValue1("test");

    Class<TestSystemDocument> type = TestSystemDocument.class;
    storage.addItem(type, doc);

    verifyCollectionSize(1, "testsystemdocument", storage.db);
  }

  @Test(expected = MongoException.class)
  public void testAddItemTwice() throws IOException {
    TestSystemDocument doc = new TestSystemDocument();
    String id = "TSD0000000001";
    doc.setId(id);
    doc.setTestValue1("test");

    Class<TestSystemDocument> type = TestSystemDocument.class;
    storage.addItem(type, doc);
    storage.addItem(type, doc);
  }

  @Test
  public void testGetItem() throws IOException {
    TestSystemDocument expected = new TestSystemDocument();
    String id = "TSD0000000001";
    expected.setId(id);
    expected.setTestValue1("test");

    Class<TestSystemDocument> type = TestSystemDocument.class;
    storage.addItem(type, expected);

    TestSystemDocument actual = storage.getItem(type, id);

    assertNull(MongoDiff.diffDocuments(expected, actual));
  }

  @Test
  public void testGetItemCreatedWithoutId() throws IOException {
    TestSystemDocument expected = new TestSystemDocument();
    expected.setTestValue1("test");

    Class<TestSystemDocument> type = TestSystemDocument.class;
    storage.addItem(type, expected);

    TestSystemDocument actual = storage.getItem(type, expected.getId());

    assertNull(MongoDiff.diffDocuments(expected, actual));
  }

  @Test
  public void testGetItemUpdatedItem() throws IOException {
    TestSystemDocument expected = new TestSystemDocument();
    String id = "TSD0000000001";
    expected.setId(id);
    expected.setTestValue1("test");

    Class<TestSystemDocument> type = TestSystemDocument.class;
    storage.addItem(type, expected);

    expected.setName("name");

    storage.updateItem(type, id, expected);

    TestSystemDocument actual = storage.getItem(type, id);

    assertNull(MongoDiff.diffDocuments(expected, actual));
  }

  @Test
  public void testGetItemNonExistent() {
    assertNull(storage.getItem(TestSystemDocument.class, "TSD0000000001"));
  }

  @Test
  public void testGetDeletedItem() throws IOException {
    TestSystemDocument expected = new TestSystemDocument();
    String id = "TSD0000000001";
    expected.setId(id);
    expected.setTestValue1("test");

    Class<TestSystemDocument> type = TestSystemDocument.class;
    storage.addItem(type, expected);

    storage.deleteItem(type, id, null);

    expected.setDeleted(true);
    expected.setRev(1);

    TestSystemDocument actual = storage.getItem(type, id);

    assertNull(MongoDiff.diffDocuments(expected, actual));
  }

  @Test
  public void testGetAllByType() throws IOException {
    setUpDatabase();
    assertEquals(3, storage.getAllByType(TestSystemDocument.class).size());
  }

  @Test
  public void testGetAllByTypeNonFound() throws IOException {
    assertEquals(0, storage.getAllByType(TestSystemDocument.class).size());
  }

  @Test
  public void testEmpty() throws IOException {
    setUpDatabase();
    storage.empty();
    assertEquals(0, storage.getAllByType(TestSystemDocument.class).size());
  }

}
