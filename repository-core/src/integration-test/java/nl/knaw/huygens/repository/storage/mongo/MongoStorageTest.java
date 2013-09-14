package nl.knaw.huygens.repository.storage.mongo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Date;

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

  private static final Class<TestSystemDocument> TYPE = TestSystemDocument.class;

  private static DocTypeRegistry registry;

  private MongoStorage storage;

  private void setUpDatabase() throws IOException {
    storage.addItem(TYPE, newTestSystemDocument("doc1", "testValue", "testValue2", "doc1", "doc1"));
    storage.addItem(TYPE, newTestSystemDocument("doc2", "testValue", "testValue2", "doc2", "doc2"));
    storage.addItem(TYPE, newTestSystemDocument("doc3", "testValue", "testValue2", "doc3", "doc3"));
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
    registry = new DocTypeRegistry(TYPE.getPackage().getName());
  }

  @Before
  public void setUp() throws UnknownHostException, MongoException {
    when(storageConfiguration.getVersionedTypes()).thenReturn(Sets.newHashSet("testsystemdocument"));
    storage = new MongoStorage(registry, storageConfiguration);
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

    TestSystemDocument actual = storage.searchItem(TYPE, example);
    assertEquals("doc1", actual.getName());
  }

  @Test
  public void testSearchItemMultipleSearchProperties() throws IOException {
    setUpDatabase();

    TestSystemDocument example = new TestSystemDocument();
    example.setName("doc2");
    example.setTestValue1("testValue");

    TestSystemDocument actual = storage.searchItem(TYPE, example);

    assertEquals("doc2", actual.getName());
    assertEquals("testValue", actual.getTestValue1());
  }

  @Test
  public void testSearchItemMultipleFound() throws IOException {
    setUpDatabase();

    TestSystemDocument example = new TestSystemDocument();
    example.setTestValue1("testValue");

    TestSystemDocument actual = storage.searchItem(TYPE, example);

    assertEquals("doc1", actual.getName());
    assertEquals("testValue", actual.getTestValue1());
  }

  @Test
  public void testSearchItemNothingFound() throws IOException {
    setUpDatabase();

    TestSystemDocument example = new TestSystemDocument();
    example.setName("nonExisting");

    assertNull(storage.searchItem(TYPE, example));
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

    storage.addItem(TYPE, doc);

    doc.setTestValue1("testValue1");
    storage.updateItem(TYPE, id, doc);

    verifyCollectionSize(1, "testsystemdocument", storage.db);
  }

  @Test(expected = IOException.class)
  public void testUpdateItemNonExistent() throws IOException {
    String id = "TSD0000000001";
    TestSystemDocument expected = new TestSystemDocument();
    expected.setId(id);
    expected.setTestValue1("test");

    storage.updateItem(TYPE, id, expected);
  }

  @Test
  public void testAddItem() throws IOException {
    TestSystemDocument doc = new TestSystemDocument();
    doc.setTestValue1("test");

    storage.addItem(TYPE, doc);

    verifyCollectionSize(1, "testsystemdocument", storage.db);
  }

  @Test
  public void testAddItemWithId() throws IOException {
    TestSystemDocument doc = new TestSystemDocument();
    String id = "TSD0000000001";
    doc.setId(id);
    doc.setTestValue1("test");

    storage.addItem(TYPE, doc);

    verifyCollectionSize(1, "testsystemdocument", storage.db);
  }

  @Test(expected = MongoException.class)
  public void testAddItemTwice() throws IOException {
    TestSystemDocument doc = new TestSystemDocument();
    String id = "TSD0000000001";
    doc.setId(id);
    doc.setTestValue1("test");

    storage.addItem(TYPE, doc);
    storage.addItem(TYPE, doc);
  }

  @Test
  public void testGetItem() throws IOException {
    TestSystemDocument expected = new TestSystemDocument();
    String id = "TSD0000000001";
    expected.setId(id);
    expected.setTestValue1("test");

    storage.addItem(TYPE, expected);

    assertEqualDocs(expected, storage.getItem(TYPE, id));
  }

  @Test
  public void testGetItemCreatedWithoutId() throws IOException {
    TestSystemDocument expected = new TestSystemDocument();
    expected.setTestValue1("test");

    storage.addItem(TYPE, expected);

    assertEqualDocs(expected, storage.getItem(TYPE, expected.getId()));
  }

  @Test
  public void testGetItemUpdatedItem() throws IOException {
    TestSystemDocument expected = new TestSystemDocument();
    String id = "TSD0000000001";
    expected.setId(id);
    expected.setTestValue1("test");

    storage.addItem(TYPE, expected);

    expected.setName("name");
    storage.updateItem(TYPE, id, expected);

    assertEqualDocs(expected, storage.getItem(TYPE, id));
  }

  @Test
  public void testGetItemNonExistent() {
    assertNull(storage.getItem(TYPE, "TSD0000000001"));
  }

  @Test
  public void testGetDeletedItem() throws IOException {
    TestSystemDocument expected = new TestSystemDocument();
    String id = "TSD0000000001";
    expected.setId(id);
    expected.setTestValue1("test");

    storage.addItem(TYPE, expected);
    storage.deleteItem(TYPE, id, null);

    expected.setDeleted(true);
    expected.setRev(1);

    assertEqualDocs(expected, storage.getItem(TYPE, id));
  }

  @Test
  public void testGetAllByType() throws IOException {
    setUpDatabase();
    assertEquals(3, storage.getAllByType(TYPE).size());
  }

  @Test
  public void testGetAllByTypeNonFound() throws IOException {
    assertEquals(0, storage.getAllByType(TYPE).size());
  }

  @Test
  public void testEmpty() throws IOException {
    setUpDatabase();
    storage.empty();
    assertEquals(0, storage.getAllByType(TYPE).size());
  }

  @Test
  public void testRemoveAll() throws IOException {
    createDocumentsWithDate();
    verifyCollectionSize(3, "testsystemdocument", storage.db);
    assertEquals(3, storage.removeAll(TestSystemDocument.class));
    verifyCollectionSize(0, "testsystemdocument", storage.db);
  }

  @Test
  public void testRemoveByDate() throws IOException {
    Date now = createDocumentsWithDate();
    verifyCollectionSize(3, "testsystemdocument", storage.db);
    assertEquals(0, storage.removeByDate(TestSystemDocument.class, "date", offsetDate(now, -4000)));
    verifyCollectionSize(3, "testsystemdocument", storage.db);
    assertEquals(1, storage.removeByDate(TestSystemDocument.class, "date", offsetDate(now, -3000)));
    verifyCollectionSize(2, "testsystemdocument", storage.db);
    assertEquals(2, storage.removeByDate(TestSystemDocument.class, "date", offsetDate(now, -1000)));
    verifyCollectionSize(0, "testsystemdocument", storage.db);
  }

  private Date createDocumentsWithDate() throws IOException {
    Date now = new Date();
    TestSystemDocument doc1 = newTestSystemDocument("doc1", "value", "value2", "doc1", "doc1");
    doc1.setDate(offsetDate(now, -3100));
    storage.addItem(TYPE, doc1);
    TestSystemDocument doc2 = newTestSystemDocument("doc2", "value", "value2", "doc2", "doc2");
    doc2.setDate(offsetDate(now, -2100));
    storage.addItem(TYPE, doc2);
    TestSystemDocument doc3 = newTestSystemDocument("doc3", "value", "value2", "doc1", "doc3");
    doc3.setDate(offsetDate(now, -1100));
    storage.addItem(TYPE, doc3);
    return now;
  }

  private Date offsetDate(Date date, long millis) {
    return new Date(date.getTime() + millis);
  }

}
