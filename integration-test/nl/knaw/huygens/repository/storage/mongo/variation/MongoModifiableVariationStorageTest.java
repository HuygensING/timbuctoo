package nl.knaw.huygens.repository.storage.mongo.variation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import nl.knaw.huygens.repository.config.DocTypeRegistry;
import nl.knaw.huygens.repository.storage.generic.StorageConfiguration;
import nl.knaw.huygens.repository.storage.mongo.MongoDiff;
import nl.knaw.huygens.repository.variation.VariationException;
import nl.knaw.huygens.repository.variation.model.TestConcreteDoc;
import nl.knaw.huygens.repository.variation.model.TestDocWithIDPrefix;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.mongodb.MongoException;

public class MongoModifiableVariationStorageTest {
  private static final String DB_NAME = "test";
  private MongoModifiableVariationStorage instance;
  private DocTypeRegistry docTypeRegistry;

  @Before
  public void setUp() throws UnknownHostException, MongoException {
    StorageConfiguration storageConfiguration = mock(StorageConfiguration.class);
    when(storageConfiguration.getDbName()).thenReturn(DB_NAME);
    when(storageConfiguration.getHost()).thenReturn("127.0.0.1");
    when(storageConfiguration.getPort()).thenReturn(27017);
    when(storageConfiguration.getUser()).thenReturn("test");
    when(storageConfiguration.getPassword()).thenReturn("test");

    docTypeRegistry = mock(DocTypeRegistry.class);
    when(docTypeRegistry.getCollectionId(TestConcreteDoc.class)).thenReturn("testconcretedoc");
    when(docTypeRegistry.getCollectionId(TestDocWithIDPrefix.class)).thenReturn("testdocwithidprefix");

    instance = new MongoModifiableVariationStorage(storageConfiguration, docTypeRegistry);
  }

  @After
  public void tearDown() {
    instance.db.dropDatabase();
    instance.destroy();
    instance = null;
  }

  @Test
  public void testAddItem() throws IOException {
    TestConcreteDoc input = createTestDoc("test");

    Class<TestConcreteDoc> type = TestConcreteDoc.class;
    instance.addItem(type, input);

    verifyCollectionSize(1, "testconcretedoc");
    verifyCollectionSize(1, "testconcretedoc-versions");
  }

  @Test
  public void testAddItemWithIDPrefix() throws IOException {
    TestDocWithIDPrefix input = new TestDocWithIDPrefix();
    input.name = "test";
    input.generalTestDocValue = "TestDocWithIDPrefix";

    instance.addItem(TestDocWithIDPrefix.class, input);

    verifyCollectionSize(1, "testdocwithidprefix-versions");
    verifyCollectionSize(1, "testdocwithidprefix-versions");
  }

  @Test
  public void testAddItemWithId() throws IOException {
    String id = "TCD000000001";

    TestConcreteDoc input = createTestDoc(id, "test");

    Class<TestConcreteDoc> type = TestConcreteDoc.class;
    instance.addItem(type, input);

    verifyCollectionSize(1, "testconcretedoc");
    verifyCollectionSize(1, "testconcretedoc-versions");
  }

  @Test(expected = MongoException.class)
  public void testAddItemTwice() throws IOException {
    String id = "TCD000000001";

    TestConcreteDoc input = createTestDoc(id, "test");

    Class<TestConcreteDoc> type = TestConcreteDoc.class;
    instance.addItem(type, input);
    instance.addItem(type, input);
  }

  @Test
  public void testAddItems() throws IOException {
    List<TestConcreteDoc> items = createTestDocList("test1", "test2", "test3");

    Class<TestConcreteDoc> type = TestConcreteDoc.class;
    instance.addItems(type, items);

    verifyCollectionSize(3, "testconcretedoc");
    verifyCollectionSize(3, "testconcretedoc-versions");
  }

  @Test
  public void testAddItemsWithId() throws IOException {
    List<TestConcreteDoc> items = createTestDocListWithIds("TCD", "test1", "test2", "test3");

    Class<TestConcreteDoc> type = TestConcreteDoc.class;
    instance.addItems(type, items);

    verifyCollectionSize(3, "testconcretedoc");
    verifyCollectionSize(3, "testconcretedoc-versions");
  }

  @Test
  public void testUpdateItem() throws IOException {
    TestConcreteDoc input = createTestDoc("test");

    Class<TestConcreteDoc> type = TestConcreteDoc.class;
    instance.addItem(type, input);

    input.name = "updated";
    instance.updateItem(type, input.getId(), input);

    verifyCollectionSize(1, "testconcretedoc-versions");
    verifyCollectionSize(1, "testconcretedoc-versions");
  }

  @Test(expected = IOException.class)
  public void testUpdateItemNonExistent() throws IOException {
    String id = "TCD000000001";
    TestConcreteDoc expected = createTestDoc(id, "test");

    Class<TestConcreteDoc> type = TestConcreteDoc.class;

    expected.name = "updated";
    instance.updateItem(type, id, expected);
  }

  @Test
  public void testDeletetem() throws IOException {
    String id = "TCD000000001";
    TestConcreteDoc input = createTestDoc(id, "test");

    Class<TestConcreteDoc> type = TestConcreteDoc.class;
    instance.addItem(type, input);

    instance.deleteItem(type, id, null);

    verifyCollectionSize(1, "testconcretedoc");
    verifyCollectionSize(1, "testconcretedoc-versions");

  }

  @Test(expected = IOException.class)
  public void testDeleteItemNonExistent() throws IOException {
    String id = "TCD000000001";

    Class<TestConcreteDoc> type = TestConcreteDoc.class;

    instance.deleteItem(type, id, null);
  }

  //MongoVariationStorageImpl tests
  @Test
  public void testGetItem() throws IOException {
    String id = "TCD0000000001";
    TestConcreteDoc expected = createTestDoc(id, "getItem");

    Class<TestConcreteDoc> type = TestConcreteDoc.class;
    instance.addItem(type, expected);

    TestConcreteDoc actual = instance.getItem(type, id);

    assertEquals(null, MongoDiff.diffDocuments(expected, actual));
  }

  @Test
  public void testGetItemUpdatedItem() throws IOException {
    String id = "TCD0000000001";
    TestConcreteDoc input = createTestDoc(id, "getItem");

    Class<TestConcreteDoc> type = TestConcreteDoc.class;
    instance.addItem(type, input);

    input.name = "getItem2";
    instance.updateItem(type, id, input);

    TestConcreteDoc expected = createTestDoc(id, "getItem2");
    expected.setRev(1);

    TestConcreteDoc actual = instance.getItem(type, id);

    assertEquals(null, MongoDiff.diffDocuments(expected, actual));
  }

  @Test
  public void testGetItemDeletedItem() throws IOException {
    String id = "TCD0000000001";
    TestConcreteDoc input = createTestDoc(id, "getItem");

    Class<TestConcreteDoc> type = TestConcreteDoc.class;
    instance.addItem(type, input);

    instance.deleteItem(type, id, null);

    TestConcreteDoc expected = createTestDoc(id, "getItem");
    expected.setRev(1);
    expected.setDeleted(true);

    TestConcreteDoc actual = instance.getItem(type, id);

    assertEquals(null, MongoDiff.diffDocuments(expected, actual));
  }

  @Test
  public void testGetItemNonExistent() throws VariationException, IOException {
    TestConcreteDoc item = instance.getItem(TestConcreteDoc.class, "TCD000000001");
    assertNull(item);
  }

  //  @Test
  //  public void testGetVersion() {
  //    fail("Yet to be implemented");
  //  }
  //
  //  @Test
  //  public void testGetVersionNonExistent() {
  //    fail("Yet to be implemented");
  //  }
  //
  //  @Test
  //  public void testGetVersionOfNonExistingItem() {
  //    fail("Yet to be implemented");
  //  }

  //Helper methods
  private void verifyCollectionSize(long expectedSize, String collectionName) {
    assertEquals(expectedSize, instance.db.getCollection(collectionName).getCount());
  }

  private List<TestConcreteDoc> createTestDocListWithIds(String idBase, String... names) {
    List<TestConcreteDoc> docs = new ArrayList<TestConcreteDoc>();
    int counter = 1;
    for (String name : names) {
      docs.add(createTestDoc((idBase + counter), name));
      counter++;
    }

    return docs;
  }

  private List<TestConcreteDoc> createTestDocList(String... names) {
    List<TestConcreteDoc> docs = new ArrayList<TestConcreteDoc>();
    for (String name : names) {
      docs.add(createTestDoc(name));
    }

    return docs;
  }

  private TestConcreteDoc createTestDoc(String name) {
    return createTestDoc(null, name);
  }

  private TestConcreteDoc createTestDoc(String id, String name) {
    TestConcreteDoc expected = new TestConcreteDoc();
    expected.name = name;
    expected.setId(id);
    expected.setCurrentVariation("model");
    expected.getVariations().add("testconcretedoc");
    return expected;
  }
}
