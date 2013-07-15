package nl.knaw.huygens.repository.storage.mongo.variation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import nl.knaw.huygens.repository.VariationHelper;
import nl.knaw.huygens.repository.config.DocTypeRegistry;
import nl.knaw.huygens.repository.model.Reference;
import nl.knaw.huygens.repository.storage.mongo.MongoChanges;
import nl.knaw.huygens.repository.storage.mongo.MongoDiff;
import nl.knaw.huygens.repository.storage.mongo.MongoStorageTestBase;
import nl.knaw.huygens.repository.variation.VariationException;
import nl.knaw.huygens.repository.variation.model.GeneralTestDoc;
import nl.knaw.huygens.repository.variation.model.TestConcreteDoc;
import nl.knaw.huygens.repository.variation.model.TestDocWithIDPrefix;
import nl.knaw.huygens.repository.variation.model.projecta.ProjectAGeneralTestDoc;
import nl.knaw.huygens.repository.variation.model.projectb.ProjectBGeneralTestDoc;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.google.common.collect.Lists;
import com.mongodb.MongoException;

public class MongoVariationStorageTest extends MongoStorageTestBase {

  private static final String DEFAULT_ID = "TCD000000001";

  private static DocTypeRegistry docTypeRegistry;

  private MongoVariationStorage storage;

  @BeforeClass
  public static void setUpDocTypeRegistry() {
    docTypeRegistry = new DocTypeRegistry("nl.knaw.huygens.repository.variation.model nl.knaw.huygens.repository.variation.model.projecta nl.knaw.huygens.repository.variation.model.projectb");
  }

  @Before
  public void setUp() throws UnknownHostException, MongoException {
    storage = new MongoVariationStorage(storageConfiguration, docTypeRegistry);
  }

  @After
  public void tearDown() {
    storage.getDB().dropDatabase();
    storage.close();
    storage = null;
  }

  @Test
  public void testAddItem() throws IOException {
    TestConcreteDoc input = createTestDoc("test");

    Class<TestConcreteDoc> type = TestConcreteDoc.class;
    storage.addItem(type, input);

    verifyCollectionSize(1, "testconcretedoc", storage.getDB());
    verifyCollectionSize(1, "testconcretedoc-versions", storage.getDB());
  }

  @Test
  public void testAddItemWithIDPrefix() throws IOException {
    TestDocWithIDPrefix input = new TestDocWithIDPrefix();
    input.name = "test";
    input.generalTestDocValue = "TestDocWithIDPrefix";

    storage.addItem(TestDocWithIDPrefix.class, input);

    verifyCollectionSize(1, "testconcretedoc", storage.getDB());
    verifyCollectionSize(1, "testconcretedoc-versions", storage.getDB());
  }

  @Test
  public void testAddItemWithId() throws IOException {
    TestConcreteDoc input = createTestDoc(DEFAULT_ID, "test");

    Class<TestConcreteDoc> type = TestConcreteDoc.class;
    storage.addItem(type, input);

    verifyCollectionSize(1, "testconcretedoc", storage.getDB());
    verifyCollectionSize(1, "testconcretedoc-versions", storage.getDB());
  }

  @Test(expected = MongoException.class)
  public void testAddItemTwice() throws IOException {
    TestConcreteDoc input = createTestDoc(DEFAULT_ID, "test");

    Class<TestConcreteDoc> type = TestConcreteDoc.class;
    storage.addItem(type, input);
    storage.addItem(type, input);
  }

  @Test
  public void addItemSubType() throws IOException {
    GeneralTestDoc input = new GeneralTestDoc();
    input.setId(DEFAULT_ID);
    input.name = "subType";
    input.generalTestDocValue = "test";

    storage.addItem(GeneralTestDoc.class, input);

    verifyCollectionSize(1, "testconcretedoc", storage.getDB());
    verifyCollectionSize(1, "testconcretedoc-versions", storage.getDB());
  }

  @Test
  public void testUpdateItem() throws IOException {
    TestConcreteDoc input = createTestDoc("test");

    Class<TestConcreteDoc> type = TestConcreteDoc.class;
    storage.addItem(type, input);

    input.name = "updated";
    storage.updateItem(type, input.getId(), input);

    verifyCollectionSize(1, "testconcretedoc", storage.getDB());
    verifyCollectionSize(1, "testconcretedoc-versions", storage.getDB());
  }

  @Test
  public void testUpdateItemWithSubType() throws IOException {
    TestConcreteDoc input = createTestDoc(DEFAULT_ID, "test");

    Class<TestConcreteDoc> type = TestConcreteDoc.class;
    storage.addItem(type, input);

    ProjectAGeneralTestDoc subClassInput = new ProjectAGeneralTestDoc();
    subClassInput.name = "updated";
    subClassInput.setId(DEFAULT_ID);

    storage.updateItem(type, DEFAULT_ID, subClassInput);

    verifyCollectionSize(1, "testconcretedoc", storage.getDB());
    verifyCollectionSize(1, "testconcretedoc-versions", storage.getDB());
  }

  @Test(expected = IOException.class)
  public void testUpdateItemNonExistent() throws IOException {
    TestConcreteDoc expected = createTestDoc(DEFAULT_ID, "test");

    Class<TestConcreteDoc> type = TestConcreteDoc.class;

    expected.name = "updated";
    storage.updateItem(type, DEFAULT_ID, expected);
  }

  @Test
  public void testDeletetem() throws IOException {
    TestConcreteDoc input = createTestDoc(DEFAULT_ID, "test");

    Class<TestConcreteDoc> type = TestConcreteDoc.class;
    storage.addItem(type, input);

    storage.deleteItem(type, DEFAULT_ID, null);

    verifyCollectionSize(1, "testconcretedoc", storage.getDB());
    verifyCollectionSize(1, "testconcretedoc-versions", storage.getDB());

  }

  @Test(expected = IOException.class)
  public void testDeleteItemNonExistent() throws IOException {
    Class<TestConcreteDoc> type = TestConcreteDoc.class;

    storage.deleteItem(type, DEFAULT_ID, null);
  }

  //MongoVariationStorageImpl tests

  @Test
  public void testGetItem() throws IOException {
    TestConcreteDoc expected = createTestDoc(DEFAULT_ID, "getItem");
    expected.getVariations().add(new Reference(TestConcreteDoc.class, DEFAULT_ID, null));

    Class<TestConcreteDoc> type = TestConcreteDoc.class;
    storage.addItem(type, expected);

    TestConcreteDoc actual = storage.getItem(type, DEFAULT_ID);

    assertEquals(null, MongoDiff.diffDocuments(expected, actual));
  }

  @Test
  public void testGetItemUpdatedItem() throws IOException {
    TestConcreteDoc input = createTestDoc(DEFAULT_ID, "getItem");

    Class<TestConcreteDoc> type = TestConcreteDoc.class;
    storage.addItem(type, input);

    input.name = "getItem2";
    storage.updateItem(type, DEFAULT_ID, input);

    TestConcreteDoc expected = createTestDoc(DEFAULT_ID, "getItem2");
    expected.setRev(1);
    expected.getVariations().add(new Reference(TestConcreteDoc.class, DEFAULT_ID, null));

    TestConcreteDoc actual = storage.getItem(type, DEFAULT_ID);

    assertEquals(null, MongoDiff.diffDocuments(expected, actual));
  }

  @Test
  public void testGetItemDeletedItem() throws IOException {
    TestConcreteDoc input = createTestDoc(DEFAULT_ID, "getItem");

    Class<TestConcreteDoc> type = TestConcreteDoc.class;
    storage.addItem(type, input);

    storage.deleteItem(type, DEFAULT_ID, null);

    TestConcreteDoc expected = createTestDoc(DEFAULT_ID, "getItem");
    expected.setRev(1);
    expected.setDeleted(true);
    expected.getVariations().add(new Reference(TestConcreteDoc.class, DEFAULT_ID, null));

    TestConcreteDoc actual = storage.getItem(type, DEFAULT_ID);

    assertEquals(null, MongoDiff.diffDocuments(expected, actual));
  }

  @Test
  public void testGetItemNonExistent() throws VariationException, IOException {
    TestConcreteDoc item = storage.getItem(TestConcreteDoc.class, "TCD000000001");
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
    expected.getVariations().add(new Reference(GeneralTestDoc.class, DEFAULT_ID, null));
    expected.getVariations().add(new Reference(TestConcreteDoc.class, DEFAULT_ID, null));

    Class<GeneralTestDoc> type = GeneralTestDoc.class;
    storage.addItem(type, input);

    GeneralTestDoc actual = storage.getItem(type, DEFAULT_ID);

    assertEquals(null, MongoDiff.diffDocuments(expected, actual));
  }

  @Test
  public void testGetAllVariations() throws IOException {
    GeneralTestDoc input = new GeneralTestDoc();
    input.setId(DEFAULT_ID);
    input.name = "subType";
    input.generalTestDocValue = "test";

    storage.addItem(GeneralTestDoc.class, input);
    List<TestConcreteDoc> variations = storage.getAllVariations(TestConcreteDoc.class, DEFAULT_ID);

    assertEquals(2, variations.size());
  }

  @Test
  public void testGetVariation() throws IOException {
    ProjectAGeneralTestDoc projectAInput = createProjectAGeneralTestDoc(DEFAULT_ID, "subTypeA", "testA", "aTestA");

    storage.addItem(ProjectAGeneralTestDoc.class, projectAInput);

    ProjectBGeneralTestDoc projectBInput = createProjectBGeneralTestDoc(DEFAULT_ID, "subTypeB", "testB", "bTestB");

    storage.updateItem(ProjectBGeneralTestDoc.class, DEFAULT_ID, projectBInput);

    TestConcreteDoc expected = createTestDoc(DEFAULT_ID, "subTypeB");
    expected.getVariations().add(new Reference(ProjectAGeneralTestDoc.class, DEFAULT_ID, null));
    expected.getVariations().add(new Reference(ProjectBGeneralTestDoc.class, DEFAULT_ID, null));
    expected.getVariations().addAll(VariationHelper.createVariationsForType(TestConcreteDoc.class, DEFAULT_ID, "projecta", "projectb", null));
    expected.getVariations().addAll(VariationHelper.createVariationsForType(GeneralTestDoc.class, DEFAULT_ID, "projecta", "projectb", null));
    expected.setCurrentVariation("projectb");
    expected.setRev(1);

    TestConcreteDoc actual = storage.getVariation(TestConcreteDoc.class, DEFAULT_ID, "projectb");

    assertEquals(null, MongoDiff.diffDocuments(expected, actual));

  }

  @Test
  public void testGetVariationNonExisting() throws IOException {
    ProjectAGeneralTestDoc projectAInput = createProjectAGeneralTestDoc(DEFAULT_ID, "subTypeA", "testA", "aTestA");;

    TestConcreteDoc expected = createTestDoc(DEFAULT_ID, "subTypeA");
    expected.getVariations().add(new Reference(ProjectAGeneralTestDoc.class, DEFAULT_ID, null));
    expected.getVariations().addAll(VariationHelper.createVariationsForType(GeneralTestDoc.class, DEFAULT_ID, "projecta", null));
    expected.getVariations().addAll(VariationHelper.createVariationsForType(TestConcreteDoc.class, DEFAULT_ID, "projecta", null));
    expected.setCurrentVariation("projecta");

    storage.addItem(ProjectAGeneralTestDoc.class, projectAInput);

    TestConcreteDoc actual = storage.getVariation(TestConcreteDoc.class, DEFAULT_ID, "projectb");

    assertEquals(null, MongoDiff.diffDocuments(expected, actual));
  }

  @Test
  public void testGetVariationOfNonExistingItem() throws IOException {
    assertNull(storage.getVariation(TestConcreteDoc.class, DEFAULT_ID, "projectb"));
  }

  @Test
  public void testGetAllByType() throws IOException {
    Class<TestConcreteDoc> type = TestConcreteDoc.class;
    for (TestConcreteDoc item : createTestDocList("test1", "test2", "test3")) {
      storage.addItem(type, item);
    }

    assertEquals(3, storage.getAllByType(type).size());
  }

  @Test
  public void testGetAllByTypeSubType() throws IOException {
    ProjectAGeneralTestDoc projectAInput = createProjectAGeneralTestDoc(null, "subTypeA", "testA", "aTestA");
    storage.addItem(ProjectAGeneralTestDoc.class, projectAInput);

    ProjectBGeneralTestDoc projectBInput = createProjectBGeneralTestDoc(null, "subTypeB", "testB", "bTestB");
    storage.addItem(ProjectBGeneralTestDoc.class, projectBInput);

    assertEquals(1, storage.getAllByType(ProjectBGeneralTestDoc.class).size());
  }

  @Test
  public void testGetAllByTypeNoneFound() throws IOException {
    ProjectAGeneralTestDoc projectAInput = createProjectAGeneralTestDoc(null, "subTypeA", "testA", "aTestA");
    storage.addItem(ProjectAGeneralTestDoc.class, projectAInput);

    assertEquals(0, storage.getAllByType(ProjectBGeneralTestDoc.class).size());
  }

  @Test
  public void testGetAllRevisionsSingleRevision() throws IOException {
    ProjectAGeneralTestDoc projectAInput = createProjectAGeneralTestDoc(DEFAULT_ID, "subTypeA", "testA", "aTestA");
    storage.addItem(ProjectAGeneralTestDoc.class, projectAInput);

    MongoChanges<TestConcreteDoc> changes = storage.getAllRevisions(TestConcreteDoc.class, DEFAULT_ID);

    assertEquals(1, changes.versions.size());
  }

  @Test
  public void testGetAllRevisionsMultipleRevisions() throws IOException {
    ProjectAGeneralTestDoc projectAInput = createProjectAGeneralTestDoc(DEFAULT_ID, "subTypeA", "testA", "aTestA");
    storage.addItem(ProjectAGeneralTestDoc.class, projectAInput);

    ProjectBGeneralTestDoc projectBInput = createProjectBGeneralTestDoc(DEFAULT_ID, "subTypeB", "testB", "bTestB");
    storage.updateItem(ProjectBGeneralTestDoc.class, DEFAULT_ID, projectBInput);

    MongoChanges<TestConcreteDoc> changes = storage.getAllRevisions(TestConcreteDoc.class, DEFAULT_ID);

    assertEquals(2, changes.versions.size());
  }

  @Test
  public void testGetAllRevisionsOfSubType() throws IOException {
    ProjectAGeneralTestDoc projectAInput = createProjectAGeneralTestDoc(DEFAULT_ID, "subTypeA", "testA", "aTestA");
    storage.addItem(ProjectAGeneralTestDoc.class, projectAInput);

    ProjectBGeneralTestDoc projectBInput = createProjectBGeneralTestDoc(DEFAULT_ID, "subTypeB", "testB", "bTestB");
    storage.updateItem(ProjectBGeneralTestDoc.class, DEFAULT_ID, projectBInput);

    MongoChanges<ProjectAGeneralTestDoc> changes = storage.getAllRevisions(ProjectAGeneralTestDoc.class, DEFAULT_ID);

    assertEquals(2, changes.versions.size());
  }

  @Test
  public void testGetAllRevisionsOfNoneExisting() throws IOException {
    MongoChanges<ProjectAGeneralTestDoc> changes = storage.getAllRevisions(ProjectAGeneralTestDoc.class, DEFAULT_ID);
    assertNull(changes);
  }

  @Test
  public void testGetByMultipleIds() throws IOException {
    Class<TestConcreteDoc> type = TestConcreteDoc.class;
    for (TestConcreteDoc item : createTestDocListWithIds("TCD", "test1", "test2", "test3")) {
      storage.addItem(type, item);
    }

    List<String> ids = Lists.newArrayList("TCD1", "TCD2", "TCD3");
    assertEquals(3, storage.getByMultipleIds(type, ids).size());
  }

  @Test
  public void testGetByMultipleIdsNotAllFound() throws IOException {
    Class<TestConcreteDoc> type = TestConcreteDoc.class;
    for (TestConcreteDoc item : createTestDocListWithIds("TCD", "test1", "test2", "test3")) {
      storage.addItem(type, item);
    }

    List<String> ids = Lists.newArrayList("TCD1", "TCD2", "TCD4");
    assertEquals(2, storage.getByMultipleIds(type, ids).size());
  }

  @Test
  public void testGetByMultipleIdsNonFound() {
    List<String> ids = Lists.newArrayList("TCD1", "TCD2", "TCD3");
    assertEquals(0, storage.getByMultipleIds(TestConcreteDoc.class, ids).size());
  }

  @Test(expected = IndexOutOfBoundsException.class)
  //FIXME: should not throw an exception
  public void testGetLastChanged() throws IOException {
    Class<TestConcreteDoc> type = TestConcreteDoc.class;
    for (TestConcreteDoc item : createTestDocListWithIds("TCD", "test1", "test2", "test3")) {
      storage.addItem(type, item);
    }

    assertEquals(2, storage.getLastChanged(2).size());
  }

  @Test(expected = IndexOutOfBoundsException.class)
  public void testGetLastChangedMoreThanFound() throws IOException {
    storage.getLastChanged(2);
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
    doc.getVariations().add(new Reference(ProjectAGeneralTestDoc.class, DEFAULT_ID, null));
    doc.getVariations().addAll(VariationHelper.createVariationsForType(GeneralTestDoc.class, DEFAULT_ID, "projecta", null));
    doc.getVariations().addAll(VariationHelper.createVariationsForType(TestConcreteDoc.class, DEFAULT_ID, "projecta", null));
    Class<ProjectAGeneralTestDoc> type = ProjectAGeneralTestDoc.class;
    storage.addItem(type, doc);

    ProjectAGeneralTestDoc revision = storage.getRevision(type, DEFAULT_ID, 0);

    assertEquals(null, MongoDiff.diffDocuments(doc, revision));
  }

  @Test
  public void testGetRevisionSuperType() throws IOException {
    ProjectAGeneralTestDoc doc = createProjectAGeneralTestDoc(DEFAULT_ID, "test", "testDocValue", "projectATestDocValue");
    storage.addItem(ProjectAGeneralTestDoc.class, doc);
    TestConcreteDoc revision = storage.getRevision(TestConcreteDoc.class, DEFAULT_ID, 0);

    TestConcreteDoc expected = createTestDoc(DEFAULT_ID, "test");
    expected.setCurrentVariation("projecta");
    expected.getVariations().add(new Reference(ProjectAGeneralTestDoc.class, DEFAULT_ID, null));
    expected.getVariations().addAll(VariationHelper.createVariationsForType(GeneralTestDoc.class, DEFAULT_ID, "projecta", null));
    expected.getVariations().addAll(VariationHelper.createVariationsForType(TestConcreteDoc.class, DEFAULT_ID, "projecta", null));

    assertEquals(null, MongoDiff.diffDocuments(expected, revision));
  }

  @Test
  public void testGetRevisionNonExistent() throws IOException {
    TestConcreteDoc doc = createTestDoc(DEFAULT_ID, "test");
    Class<TestConcreteDoc> type = TestConcreteDoc.class;
    storage.addItem(type, doc);
    TestConcreteDoc revision = storage.getRevision(type, DEFAULT_ID, 1);
    assertNull(revision);
  }

  @Test
  public void testGetRevisionOfNonExistingItem() throws IOException {
    TestConcreteDoc revision = storage.getRevision(TestConcreteDoc.class, DEFAULT_ID, 1);
    assertNull(revision);
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
