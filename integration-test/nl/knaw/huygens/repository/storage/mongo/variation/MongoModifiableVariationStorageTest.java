package nl.knaw.huygens.repository.storage.mongo.variation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import nl.knaw.huygens.repository.config.DocTypeRegistry;
import nl.knaw.huygens.repository.model.Document;
import nl.knaw.huygens.repository.storage.StorageIterator;
import nl.knaw.huygens.repository.storage.generic.StorageConfiguration;
import nl.knaw.huygens.repository.storage.mongo.MongoChanges;
import nl.knaw.huygens.repository.storage.mongo.MongoDiff;
import nl.knaw.huygens.repository.variation.VariationException;
import nl.knaw.huygens.repository.variation.model.GeneralTestDoc;
import nl.knaw.huygens.repository.variation.model.TestConcreteDoc;
import nl.knaw.huygens.repository.variation.model.TestDocWithIDPrefix;
import nl.knaw.huygens.repository.variation.model.projecta.ProjectAGeneralTestDoc;
import nl.knaw.huygens.repository.variation.model.projectb.ProjectBGeneralTestDoc;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.google.common.collect.Lists;
import com.mongodb.MongoException;

public class MongoModifiableVariationStorageTest {
  private static final String DEFAULT_ID = "TCD000000001";
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

    docTypeRegistry = new DocTypeRegistry("nl.knaw.huygens.repository.variation.model nl.knaw.huygens.repository.variation.model.projecta nl.knaw.huygens.repository.variation.model.projectb");

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

    verifyCollectionSize(1, "testconcretedoc");
    verifyCollectionSize(1, "testconcretedoc-versions");
  }

  @Test
  public void testAddItemWithId() throws IOException {
    TestConcreteDoc input = createTestDoc(DEFAULT_ID, "test");

    Class<TestConcreteDoc> type = TestConcreteDoc.class;
    instance.addItem(type, input);

    verifyCollectionSize(1, "testconcretedoc");
    verifyCollectionSize(1, "testconcretedoc-versions");
  }

  @Test(expected = MongoException.class)
  public void testAddItemTwice() throws IOException {
    TestConcreteDoc input = createTestDoc(DEFAULT_ID, "test");

    Class<TestConcreteDoc> type = TestConcreteDoc.class;
    instance.addItem(type, input);
    instance.addItem(type, input);
  }

  @Test
  public void addItemSubType() throws IOException {
    GeneralTestDoc input = new GeneralTestDoc();
    input.setId(DEFAULT_ID);
    input.name = "subType";
    input.generalTestDocValue = "test";

    instance.addItem(GeneralTestDoc.class, input);

    verifyCollectionSize(1, "testconcretedoc");
    verifyCollectionSize(1, "testconcretedoc-versions");
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

    verifyCollectionSize(1, "testconcretedoc");
    verifyCollectionSize(1, "testconcretedoc-versions");
  }

  @Test
  public void testUpdateItemWithSubType() throws IOException {
    TestConcreteDoc input = createTestDoc(DEFAULT_ID, "test");

    Class<TestConcreteDoc> type = TestConcreteDoc.class;
    instance.addItem(type, input);

    ProjectAGeneralTestDoc subClassInput = new ProjectAGeneralTestDoc();
    subClassInput.name = "updated";
    subClassInput.setId(DEFAULT_ID);

    instance.updateItem(type, DEFAULT_ID, subClassInput);

    verifyCollectionSize(1, "testconcretedoc");
    verifyCollectionSize(1, "testconcretedoc-versions");
  }

  @Test(expected = IOException.class)
  public void testUpdateItemNonExistent() throws IOException {
    TestConcreteDoc expected = createTestDoc(DEFAULT_ID, "test");

    Class<TestConcreteDoc> type = TestConcreteDoc.class;

    expected.name = "updated";
    instance.updateItem(type, DEFAULT_ID, expected);
  }

  @Test
  public void testDeletetem() throws IOException {
    TestConcreteDoc input = createTestDoc(DEFAULT_ID, "test");

    Class<TestConcreteDoc> type = TestConcreteDoc.class;
    instance.addItem(type, input);

    instance.deleteItem(type, DEFAULT_ID, null);

    verifyCollectionSize(1, "testconcretedoc");
    verifyCollectionSize(1, "testconcretedoc-versions");

  }

  @Test(expected = IOException.class)
  public void testDeleteItemNonExistent() throws IOException {
    Class<TestConcreteDoc> type = TestConcreteDoc.class;

    instance.deleteItem(type, DEFAULT_ID, null);
  }

  //MongoVariationStorageImpl tests

  @Test
  public void testGetItem() throws IOException {
    TestConcreteDoc expected = createTestDoc(DEFAULT_ID, "getItem");

    Class<TestConcreteDoc> type = TestConcreteDoc.class;
    instance.addItem(type, expected);

    TestConcreteDoc actual = instance.getItem(type, DEFAULT_ID);

    assertEquals(null, MongoDiff.diffDocuments(expected, actual));
  }

  @Test
  public void testGetItemUpdatedItem() throws IOException {
    TestConcreteDoc input = createTestDoc(DEFAULT_ID, "getItem");

    Class<TestConcreteDoc> type = TestConcreteDoc.class;
    instance.addItem(type, input);

    input.name = "getItem2";
    instance.updateItem(type, DEFAULT_ID, input);

    TestConcreteDoc expected = createTestDoc(DEFAULT_ID, "getItem2");
    expected.setRev(1);

    TestConcreteDoc actual = instance.getItem(type, DEFAULT_ID);

    assertEquals(null, MongoDiff.diffDocuments(expected, actual));
  }

  @Test
  public void testGetItemDeletedItem() throws IOException {
    TestConcreteDoc input = createTestDoc(DEFAULT_ID, "getItem");

    Class<TestConcreteDoc> type = TestConcreteDoc.class;
    instance.addItem(type, input);

    instance.deleteItem(type, DEFAULT_ID, null);

    TestConcreteDoc expected = createTestDoc(DEFAULT_ID, "getItem");
    expected.setRev(1);
    expected.setDeleted(true);

    TestConcreteDoc actual = instance.getItem(type, DEFAULT_ID);

    assertEquals(null, MongoDiff.diffDocuments(expected, actual));
  }

  @Test
  public void testGetItemNonExistent() throws VariationException, IOException {
    TestConcreteDoc item = instance.getItem(TestConcreteDoc.class, "TCD000000001");
    assertNull(item);
  }

  @Test
  public void testGetItemSubType() throws IOException {
    GeneralTestDoc input = new GeneralTestDoc();
    input.setId(DEFAULT_ID);
    input.name = "subType";
    input.generalTestDocValue = "test";

    GeneralTestDoc expected = new GeneralTestDoc();
    expected.setId(DEFAULT_ID);
    expected.name = "subType";
    expected.generalTestDocValue = "test";
    expected.setCurrentVariation("model");
    expected.setVariations(Lists.newArrayList("generaltestdoc", "testconcretedoc"));

    Class<GeneralTestDoc> type = GeneralTestDoc.class;
    instance.addItem(type, input);

    GeneralTestDoc actual = instance.getItem(type, DEFAULT_ID);

    assertEquals(null, MongoDiff.diffDocuments(expected, actual));
  }

  @Test
  public void testGetAllVariations() throws IOException {
    GeneralTestDoc input = new GeneralTestDoc();
    input.setId(DEFAULT_ID);
    input.name = "subType";
    input.generalTestDocValue = "test";

    instance.addItem(GeneralTestDoc.class, input);
    List<TestConcreteDoc> variations = instance.getAllVariations(TestConcreteDoc.class, DEFAULT_ID);

    assertEquals(2, variations.size());
  }

  @Test
  public void testGetVariation() throws IOException {
    ProjectAGeneralTestDoc projectAInput = createProjectAGeneralTestDoc(DEFAULT_ID, "subTypeA", "testA", "aTestA");

    instance.addItem(ProjectAGeneralTestDoc.class, projectAInput);

    ProjectBGeneralTestDoc projectBInput = createProjectBGeneralTestDoc(DEFAULT_ID, "subTypeB", "testB", "bTestB");

    instance.updateItem(ProjectBGeneralTestDoc.class, DEFAULT_ID, projectBInput);

    TestConcreteDoc expected = createTestDoc(DEFAULT_ID, "subTypeB");
    expected.setVariations(Lists.newArrayList("projecta-projectageneraltestdoc", "generaltestdoc", "testconcretedoc", "projectb-projectbgeneraltestdoc"));
    expected.setCurrentVariation("projectb");
    expected.setRev(1);

    TestConcreteDoc actual = instance.getVariation(TestConcreteDoc.class, DEFAULT_ID, "projectb");

    assertEquals(null, MongoDiff.diffDocuments(expected, actual));

  }

  @Test
  public void testGetVariationNonExisting() throws IOException {
    ProjectAGeneralTestDoc projectAInput = createProjectAGeneralTestDoc(DEFAULT_ID, "subTypeA", "testA", "aTestA");;

    TestConcreteDoc expected = createTestDoc(DEFAULT_ID, "subTypeA");
    expected.setVariations(Lists.newArrayList("projecta-projectageneraltestdoc", "generaltestdoc", "testconcretedoc"));
    expected.setCurrentVariation("projecta");

    instance.addItem(ProjectAGeneralTestDoc.class, projectAInput);

    TestConcreteDoc actual = instance.getVariation(TestConcreteDoc.class, DEFAULT_ID, "projectb");

    assertEquals(null, MongoDiff.diffDocuments(expected, actual));
  }

  @Test
  public void testGetVariationOfNonExistingItem() throws IOException {
    TestConcreteDoc actual = instance.getVariation(TestConcreteDoc.class, DEFAULT_ID, "projectb");

    assertNull(actual);
  }

  @Test
  public void testGetAllByType() throws IOException {
    List<TestConcreteDoc> items = createTestDocList("test1", "test2", "test3");

    Class<TestConcreteDoc> type = TestConcreteDoc.class;
    instance.addItems(type, items);

    StorageIterator<TestConcreteDoc> iterator = instance.getAllByType(type);

    assertEquals(3, iterator.size());
  }

  @Test
  public void testGetAllByTypeSubType() throws IOException {
    ProjectAGeneralTestDoc projectAInput = createProjectAGeneralTestDoc(null, "subTypeA", "testA", "aTestA");
    instance.addItem(ProjectAGeneralTestDoc.class, projectAInput);

    ProjectBGeneralTestDoc projectBInput = createProjectBGeneralTestDoc(null, "subTypeB", "testB", "bTestB");
    instance.addItem(ProjectBGeneralTestDoc.class, projectBInput);

    StorageIterator<ProjectBGeneralTestDoc> iterator = instance.getAllByType(ProjectBGeneralTestDoc.class);

    assertEquals(1, iterator.size());
  }

  @Test
  public void testGetAllByTypeNoneFound() throws IOException {
    ProjectAGeneralTestDoc projectAInput = createProjectAGeneralTestDoc(null, "subTypeA", "testA", "aTestA");
    instance.addItem(ProjectAGeneralTestDoc.class, projectAInput);

    StorageIterator<ProjectBGeneralTestDoc> iterator = instance.getAllByType(ProjectBGeneralTestDoc.class);

    assertEquals(0, iterator.size());
  }

  @Test
  public void testGetAllRevisionsSingleRevision() throws IOException {
    ProjectAGeneralTestDoc projectAInput = createProjectAGeneralTestDoc(DEFAULT_ID, "subTypeA", "testA", "aTestA");
    instance.addItem(ProjectAGeneralTestDoc.class, projectAInput);

    MongoChanges<TestConcreteDoc> changes = instance.getAllRevisions(TestConcreteDoc.class, DEFAULT_ID);

    assertEquals(1, changes.versions.size());
  }

  @Test
  public void testGetAllRevisionsMultipleRevisions() throws IOException {
    ProjectAGeneralTestDoc projectAInput = createProjectAGeneralTestDoc(DEFAULT_ID, "subTypeA", "testA", "aTestA");
    instance.addItem(ProjectAGeneralTestDoc.class, projectAInput);

    ProjectBGeneralTestDoc projectBInput = createProjectBGeneralTestDoc(DEFAULT_ID, "subTypeB", "testB", "bTestB");
    instance.updateItem(ProjectBGeneralTestDoc.class, DEFAULT_ID, projectBInput);

    MongoChanges<TestConcreteDoc> changes = instance.getAllRevisions(TestConcreteDoc.class, DEFAULT_ID);

    assertEquals(2, changes.versions.size());
  }

  @Test
  public void testGetAllRevisionsOfSubType() throws IOException {
    ProjectAGeneralTestDoc projectAInput = createProjectAGeneralTestDoc(DEFAULT_ID, "subTypeA", "testA", "aTestA");
    instance.addItem(ProjectAGeneralTestDoc.class, projectAInput);

    ProjectBGeneralTestDoc projectBInput = createProjectBGeneralTestDoc(DEFAULT_ID, "subTypeB", "testB", "bTestB");
    instance.updateItem(ProjectBGeneralTestDoc.class, DEFAULT_ID, projectBInput);

    MongoChanges<ProjectAGeneralTestDoc> changes = instance.getAllRevisions(ProjectAGeneralTestDoc.class, DEFAULT_ID);

    assertEquals(2, changes.versions.size()); //FIXME: There should be 2 revisions.
  }

  @Test
  public void testGetAllRevisionsOfNoneExisting() throws IOException {
    MongoChanges<ProjectAGeneralTestDoc> changes = instance.getAllRevisions(ProjectAGeneralTestDoc.class, DEFAULT_ID);
    assertNull(changes);
  }

  @Test
  public void testGetByMultipleIds() throws IOException {
    List<TestConcreteDoc> items = createTestDocListWithIds("TCD", "test1", "test2", "test3");

    Class<TestConcreteDoc> type = TestConcreteDoc.class;
    instance.addItems(type, items);

    List<String> ids = Lists.newArrayList("TCD1", "TCD2", "TCD3");

    StorageIterator<TestConcreteDoc> iterator = instance.getByMultipleIds(type, ids);

    assertEquals(3, iterator.size());
  }

  @Test
  public void testGetByMultipleIdsNotAllFound() throws IOException {
    List<TestConcreteDoc> items = createTestDocListWithIds("TCD", "test1", "test2", "test3");

    Class<TestConcreteDoc> type = TestConcreteDoc.class;
    instance.addItems(type, items);

    List<String> ids = Lists.newArrayList("TCD1", "TCD2", "TCD4");

    StorageIterator<TestConcreteDoc> iterator = instance.getByMultipleIds(type, ids);

    assertEquals(2, iterator.size());
  }

  @Test
  public void testGetByMultipleIdsNonFound() {
    List<String> ids = Lists.newArrayList("TCD1", "TCD2", "TCD3");

    StorageIterator<TestConcreteDoc> iterator = instance.getByMultipleIds(TestConcreteDoc.class, ids);

    assertEquals(0, iterator.size());
  }

  @Test(expected = IndexOutOfBoundsException.class)
  //FIXME: should not throw an exception
  public void testGetLastChanged() throws IOException {
    List<TestConcreteDoc> items = createTestDocListWithIds("TCD", "test1", "test2", "test3");
    instance.addItems(TestConcreteDoc.class, items);

    List<Document> lastChangedDocuments = instance.getLastChanged(2);

    assertEquals(2, lastChangedDocuments.size());
  }

  @Test(expected = IndexOutOfBoundsException.class)
  public void testGetLastChangedMoreThanFound() throws IOException {
    instance.getLastChanged(2);
  }

  @Test
  @Ignore(value = "Code not used at this moment.")
  public void testFetchAll() {
    fail("Yet to be implemented");
  }

  @Test
  @Ignore(value = "Code not used at this moment.")
  public void testGetIdsForQuery() {
    fail("Yet to be implemented");
  }

  @Test
  @Ignore("Related indexes are not used at the moment")
  public void testEnsureIndex() throws IOException {
    fail("Yet to be implemented");
  }

  @Test
  @Ignore("Related indexes are not used at the moment")
  public void testEnsureIndexNothingToIndex() {
    fail("Yet to be implemented");
  }

  @Test
  @Ignore("Related indexes are not used at the moment")
  public void testEnsureIndexEmptyAccessorList() {
    fail("Yet to be implemented");
  }

  @Test
  @Ignore("Related indexes are not used at the moment")
  public void testEnsureIndexAccessorListIsNull() {
    fail("Yet to be implemented");
  }

  @Test
  @Ignore("Related indexes are not used at the moment")
  public void testEnsureIndexTypeIsNull() {
    fail("Yet to be implemented");
  }

  @Test
  @Ignore("Related indexes are not used at the moment")
  public void testEnsureIndexDoesNotExist() {
    fail("Yet to be implemented");
  }

  @Test
  public void testGetRevision() throws IOException {
    ProjectAGeneralTestDoc doc = createProjectAGeneralTestDoc(DEFAULT_ID, "test", "testDocValue", "projectATestDocValue");
    doc.setVariations(Lists.newArrayList("projecta-projectageneraltestdoc", "generaltestdoc", "testconcretedoc"));
    Class<ProjectAGeneralTestDoc> type = ProjectAGeneralTestDoc.class;
    instance.addItem(type, doc);

    ProjectAGeneralTestDoc revision = instance.getRevision(type, DEFAULT_ID, 0);

    assertEquals(null, MongoDiff.diffDocuments(doc, revision));
  }

  @Test
  public void testGetRevisionSuperType() throws IOException {
    ProjectAGeneralTestDoc doc = createProjectAGeneralTestDoc(DEFAULT_ID, "test", "testDocValue", "projectATestDocValue");
    instance.addItem(ProjectAGeneralTestDoc.class, doc);
    TestConcreteDoc revision = instance.getRevision(TestConcreteDoc.class, DEFAULT_ID, 0);

    TestConcreteDoc expected = createTestDoc(DEFAULT_ID, "test");
    expected.setCurrentVariation("projecta");
    expected.setVariations(Lists.newArrayList("projecta-projectageneraltestdoc", "generaltestdoc", "testconcretedoc"));

    assertEquals(null, MongoDiff.diffDocuments(expected, revision));
  }

  @Test
  public void testGetRevisionNonExistent() throws IOException {
    TestConcreteDoc doc = createTestDoc(DEFAULT_ID, "test");
    Class<TestConcreteDoc> type = TestConcreteDoc.class;
    instance.addItem(type, doc);
    TestConcreteDoc revision = instance.getRevision(type, DEFAULT_ID, 1);
    assertNull(revision);
  }

  @Test
  public void testGetRevisionOfNonExistingItem() throws IOException {
    TestConcreteDoc revision = instance.getRevision(TestConcreteDoc.class, DEFAULT_ID, 1);
    assertNull(revision);
  }

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

  private ProjectAGeneralTestDoc createProjectAGeneralTestDoc(String id, String name, String generalTestDocValue, String projectAGeneralTestDocValue) {
    ProjectAGeneralTestDoc projectATestDoc = new ProjectAGeneralTestDoc();
    projectATestDoc.setId(id);
    projectATestDoc.name = name;
    projectATestDoc.generalTestDocValue = generalTestDocValue;
    projectATestDoc.projectAGeneralTestDocValue = projectAGeneralTestDocValue;
    return projectATestDoc;
  }

  private ProjectBGeneralTestDoc createProjectBGeneralTestDoc(String id, String name, String generalTestDocValue, String projectBGeneralTestDocValue) {
    ProjectBGeneralTestDoc projectBTestDoc = new ProjectBGeneralTestDoc();
    projectBTestDoc.setId(id);
    projectBTestDoc.name = name;
    projectBTestDoc.generalTestDocValue = generalTestDocValue;
    projectBTestDoc.projectBGeneralTestDocValue = projectBGeneralTestDocValue;

    return projectBTestDoc;
  }

}
