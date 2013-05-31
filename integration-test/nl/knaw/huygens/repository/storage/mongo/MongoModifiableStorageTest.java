package nl.knaw.huygens.repository.storage.mongo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.List;

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
}
