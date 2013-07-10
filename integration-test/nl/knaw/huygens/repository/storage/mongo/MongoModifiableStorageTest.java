package nl.knaw.huygens.repository.storage.mongo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.List;

import nl.knaw.huygens.repository.config.DocTypeRegistry;
import nl.knaw.huygens.repository.storage.StorageIterator;
import nl.knaw.huygens.repository.storage.mongo.model.TestSystemDocument;
import nl.knaw.huygens.repository.variation.model.TestConcreteDoc;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mongodb.MongoException;

public class MongoModifiableStorageTest extends MongoStorageTestBase {

  private MongoStorage instance;
  private static DocTypeRegistry docTypeRegistry;

  private void setUpDatabase() throws IOException {
    List<TestSystemDocument> docs = Lists.newArrayList(createTestSystemDocument("doc1", "testValue", "testValue2", "doc1", "doc1"),
        createTestSystemDocument("doc2", "testValue", "testValue2", "doc2", "doc2"), createTestSystemDocument("doc3", "testValue", "testValue2", "doc3", "doc3"));
    instance.addItems(TestSystemDocument.class, docs);
  }

  protected TestSystemDocument createTestSystemDocument(String name, String testValue1, String testValue2, String annotatedProperty, String propWithAnnotatedAccessors) {
    TestSystemDocument doc = new TestSystemDocument();
    doc.setName(name);
    doc.setTestValue1(testValue1);
    doc.setTestValue2(testValue2);
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
    instance = new MongoStorage(storageConfiguration, docTypeRegistry);
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

    TestSystemDocument example = new TestSystemDocument();
    example.setName("doc1");

    TestSystemDocument actual = instance.searchItem(TestSystemDocument.class, example);

    assertEquals("doc1", actual.getName());
  }

  @Test
  public void testSearchItemMultipleSearchProperties() throws IOException {
    setUpDatabase();

    TestSystemDocument example = new TestSystemDocument();
    example.setName("doc2");
    example.setTestValue1("testValue");

    TestSystemDocument actual = instance.searchItem(TestSystemDocument.class, example);

    assertEquals("doc2", actual.getName());
    assertEquals("testValue", actual.getTestValue1());
  }

  @Test
  public void testSearchItemMultipleFound() throws IOException {
    setUpDatabase();

    TestSystemDocument example = new TestSystemDocument();
    example.setTestValue1("testValue");

    TestSystemDocument actual = instance.searchItem(TestSystemDocument.class, example);

    assertEquals("doc1", actual.getName());
    assertEquals("testValue", actual.getTestValue1());
  }

  @Test
  public void testSearchItemNothingFound() throws IOException {
    setUpDatabase();

    TestSystemDocument example = new TestSystemDocument();
    example.setName("nonExisting");

    TestSystemDocument actual = instance.searchItem(TestSystemDocument.class, example);

    assertNull(actual);
  }

  @Test
  public void testSearchItemUnknownCollection() throws IOException {
    setUpDatabase();

    TestConcreteDoc example = new TestConcreteDoc();
    example.name = "nonExisting";

    TestConcreteDoc actual = instance.searchItem(TestConcreteDoc.class, example);

    assertNull(actual);
  }

  @Test
  public void testUpdateItem() throws IOException {
    TestSystemDocument doc = new TestSystemDocument();
    String id = "TSD0000000001";
    doc.setId(id);
    doc.setTestValue1("test");

    Class<TestSystemDocument> type = TestSystemDocument.class;
    instance.addItem(type, doc);

    doc.setTestValue1("testValue1");
    instance.updateItem(type, id, doc);

    verifyCollectionSize(1, "testsystemdocument", instance.db);
  }

  @Test(expected = IOException.class)
  public void testUpdateItemNonExistent() throws IOException {
    String id = "TSD0000000001";
    TestSystemDocument expected = new TestSystemDocument();
    expected.setId(id);
    expected.setTestValue1("test");

    Class<TestSystemDocument> type = TestSystemDocument.class;

    instance.updateItem(type, id, expected);
  }

  @Test
  public void testAddItem() throws IOException {
    TestSystemDocument doc = new TestSystemDocument();
    doc.setTestValue1("test");

    Class<TestSystemDocument> type = TestSystemDocument.class;
    instance.addItem(type, doc);

    verifyCollectionSize(1, "testsystemdocument", instance.db);
  }

  @Test
  public void testAddItemWithId() throws IOException {
    TestSystemDocument doc = new TestSystemDocument();
    String id = "TSD0000000001";
    doc.setId(id);
    doc.setTestValue1("test");

    Class<TestSystemDocument> type = TestSystemDocument.class;
    instance.addItem(type, doc);

    verifyCollectionSize(1, "testsystemdocument", instance.db);
  }

  @Test(expected = MongoException.class)
  public void testAddItemTwice() throws IOException {
    TestSystemDocument doc = new TestSystemDocument();
    String id = "TSD0000000001";
    doc.setId(id);
    doc.setTestValue1("test");

    Class<TestSystemDocument> type = TestSystemDocument.class;
    instance.addItem(type, doc);
    instance.addItem(type, doc);
  }

  @Test
  public void testGetItem() throws IOException {
    TestSystemDocument expected = new TestSystemDocument();
    String id = "TSD0000000001";
    expected.setId(id);
    expected.setTestValue1("test");

    Class<TestSystemDocument> type = TestSystemDocument.class;
    instance.addItem(type, expected);

    TestSystemDocument actual = instance.getItem(type, id);

    assertEquals(null, MongoDiff.diffDocuments(expected, actual));
  }

  @Test
  public void testGetItemCreatedWithoutId() throws IOException {
    TestSystemDocument expected = new TestSystemDocument();
    expected.setTestValue1("test");

    Class<TestSystemDocument> type = TestSystemDocument.class;
    instance.addItem(type, expected);

    TestSystemDocument actual = instance.getItem(type, expected.getId());

    assertEquals(null, MongoDiff.diffDocuments(expected, actual));
  }

  @Test
  public void testGetItemUpdatedItem() throws IOException {
    TestSystemDocument expected = new TestSystemDocument();
    String id = "TSD0000000001";
    expected.setId(id);
    expected.setTestValue1("test");

    Class<TestSystemDocument> type = TestSystemDocument.class;
    instance.addItem(type, expected);

    expected.setName("name");

    instance.updateItem(type, id, expected);

    TestSystemDocument actual = instance.getItem(type, id);

    assertEquals(null, MongoDiff.diffDocuments(expected, actual));
  }

  @Test
  public void testGetItemNonExistent() {
    TestSystemDocument doc = instance.getItem(TestSystemDocument.class, "TSD0000000001");

    assertNull(doc);
  }

  @Test
  public void testGetDeletedItem() throws IOException {
    TestSystemDocument expected = new TestSystemDocument();
    String id = "TSD0000000001";
    expected.setId(id);
    expected.setTestValue1("test");

    Class<TestSystemDocument> type = TestSystemDocument.class;
    instance.addItem(type, expected);

    instance.deleteItem(type, id, null);

    expected.setDeleted(true);
    expected.setRev(1);

    TestSystemDocument actual = instance.getItem(type, id);

    assertEquals(null, MongoDiff.diffDocuments(expected, actual));
  }

  @Test
  public void testGetAllByType() throws IOException {
    this.setUpDatabase();

    StorageIterator<TestSystemDocument> iterator = instance.getAllByType(TestSystemDocument.class);

    assertEquals(3, iterator.size());
  }

  @Test
  public void testGetAllByTypeNonFound() {
    StorageIterator<TestSystemDocument> iterator = instance.getAllByType(TestSystemDocument.class);

    assertEquals(0, iterator.size());
  }

  @Test
  public void testEmpty() throws IOException {
    this.setUpDatabase();
    instance.empty();

    StorageIterator<TestSystemDocument> iterator = instance.getAllByType(TestSystemDocument.class);

    assertEquals(0, iterator.size());
  }

}
