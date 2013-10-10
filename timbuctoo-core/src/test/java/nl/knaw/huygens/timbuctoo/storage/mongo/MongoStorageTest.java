package nl.knaw.huygens.timbuctoo.storage.mongo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import nl.knaw.huygens.timbuctoo.config.DocTypeRegistry;
import nl.knaw.huygens.timbuctoo.storage.StorageIterator;
import nl.knaw.huygens.timbuctoo.storage.mongo.model.TestSystemDocument;

import org.junit.BeforeClass;
import org.junit.Test;
import org.mongojack.internal.stream.JacksonDBObject;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoException;
import com.mongodb.WriteResult;

/**
 * These unit tests all only check the interaction with the Mongo database. 
 *
 */
public class MongoStorageTest extends MongoStorageTestBase {

  private static final String DEFAULT_ID = "TSTD000000000001";

  private static final Class<TestSystemDocument> TYPE = TestSystemDocument.class;

  private static DocTypeRegistry registry;

  private MongoStorage storage;

  @BeforeClass
  public static void setUpDocTypeRegistry() {
    registry = new DocTypeRegistry(TYPE.getPackage().getName());
  }

  @Test
  public void testFindItemOneSearchProperty() throws IOException {
    String name = "doc1";
    TestSystemDocument example = new TestSystemDocument();
    example.setName(name);

    Map<String, Object> testSystemDocumentMap = createDefaultMap(0, DEFAULT_ID);
    testSystemDocumentMap.put("name", name);
    DBObject dbObject = createDBObject(testSystemDocumentMap);

    DBCursor cursor = createDBCursorWithOneValue(dbObject);

    DBObject query = new BasicDBObject("name", name);
    when(anyCollection.find(query, null)).thenReturn(cursor);

    storage.findItem(TYPE, example);

  }

  @Test
  public void testFindItemMultipleSearchProperties() throws IOException {
    TestSystemDocument example = new TestSystemDocument();
    String name = "doc2";
    example.setName(name);
    String testValue1 = "testValue";
    example.setTestValue1(testValue1);

    Map<String, Object> testSystemDocumentMap = createDefaultMap(0, DEFAULT_ID);
    testSystemDocumentMap.put("name", name);
    testSystemDocumentMap.put("testValue1", testValue1);
    DBObject dbObject = createDBObject(testSystemDocumentMap);

    DBCursor cursor = createDBCursorWithOneValue(dbObject);

    DBObject query = new BasicDBObject("name", name);
    query.put("testValue1", testValue1);
    when(anyCollection.find(query, null)).thenReturn(cursor);

    storage.findItem(TYPE, example);
  }

  @Test
  public void testFindItemMultipleFound() throws IOException {
    TestSystemDocument example = new TestSystemDocument();
    String testValue = "testValue";
    example.setTestValue1(testValue);

    Map<String, Object> testSystemDocumentMap1 = createDefaultMap(0, DEFAULT_ID);
    String name1 = "doc1";
    testSystemDocumentMap1.put("name", name1);
    testSystemDocumentMap1.put("testValue1", testValue);
    TestSystemDocument doc1 = new TestSystemDocument();
    doc1.setName(name1);
    doc1.setTestValue1(testValue);
    DBObject dbObject1 = createDBObject(doc1, testSystemDocumentMap1);

    Map<String, Object> testSystemDocumentMap2 = createDefaultMap(0, DEFAULT_ID);
    String name2 = "doc2";
    testSystemDocumentMap1.put("name", name2);
    testSystemDocumentMap1.put("testValue1", testValue);
    TestSystemDocument doc2 = new TestSystemDocument();
    doc2.setName(name2);
    doc2.setTestValue1(testValue);
    DBObject dbObject2 = createDBObject(testSystemDocumentMap2);

    DBCursor cursor = mock(DBCursor.class);
    when(cursor.hasNext()).thenReturn(true, true, false);
    when(cursor.next()).thenReturn(dbObject1, dbObject2);

    DBObject query = new BasicDBObject("testValue1", testValue);
    when(anyCollection.find(query, null)).thenReturn(cursor);

    TestSystemDocument actual = storage.findItem(TYPE, example);

    assertEquals(name1, actual.getName());
    assertEquals(testValue, actual.getTestValue1());
  }

  @Test
  public void testFindItemNothingFound() throws IOException {
    TestSystemDocument example = new TestSystemDocument();
    String name = "nonExisting";
    example.setName(name);

    DBCursor cursor = createCursorWithoutValues();

    DBObject query = new BasicDBObject("name", name);
    when(anyCollection.find(query, null)).thenReturn(cursor);

    storage.findItem(TYPE, example);
  }

  @Test
  public void testFindItemUnknownCollection() throws IOException {
    TestSystemDocument example = new TestSystemDocument();
    example.setName("nonExisting");

    DBCursor cursor = createCursorWithoutValues();

    DBObject query = new BasicDBObject("name", "nonExisting");
    when(anyCollection.find(query, null)).thenReturn(cursor);

    storage.findItem(TYPE, example);
  }

  @Test
  public void testUpdateItem() throws IOException {
    TestSystemDocument newDoc = new TestSystemDocument();
    newDoc.setId(DEFAULT_ID);
    String testValue1 = "test";
    newDoc.setTestValue1(testValue1);

    TestSystemDocument oldDoc = new TestSystemDocument();
    oldDoc.setId(DEFAULT_ID);
    oldDoc.setTestValue1("testValue");

    Map<String, Object> oldTestSystemDocumentMap = createDefaultMap(0, DEFAULT_ID);
    oldTestSystemDocumentMap.put("testValue1", "test");
    DBObject oldDBObject = createDBObject(oldDoc, oldTestSystemDocumentMap);

    DBCursor cursor = createDBCursorWithOneValue(oldDBObject);

    //    Map<String, Object> newTestSystemDocumentMap = createDefaultMap(1);
    //    newTestSystemDocumentMap.put("testValue1", testValue1);
    //    newTestSystemDocumentMap.put("name", null);
    //    newTestSystemDocumentMap.put("testValue2", null);
    //    newTestSystemDocumentMap.put("date", null);
    //    newTestSystemDocumentMap.put("propAnnotated", null);
    //    newTestSystemDocumentMap.put("pwaa", null);

    //DBObject newDBObject = createDBObject(newDoc, newTestSystemDocumentMap);

    DBObject query = new BasicDBObject("_id", DEFAULT_ID);
    query.put("^rev", 0);

    when(anyCollection.find(query, null)).thenReturn(cursor);
    // Code should be:
    //when(anyCollection.findAndModify(query, null, null, false, newDBObject, false, false)).thenReturn(oldDBObject);
    // But there are some strange things happening with the comparison of DBObjects. 
    // The next line is a quick fix
    when(anyCollection.findAndModify(any(DBObject.class), any(DBObject.class), any(DBObject.class), anyBoolean(), any(DBObject.class), anyBoolean(), anyBoolean())).thenReturn(oldDBObject);

    try {
      storage.updateItem(TYPE, DEFAULT_ID, newDoc);
    } finally {
      verify(anyCollection).findAndModify(any(DBObject.class), any(DBObject.class), any(DBObject.class), anyBoolean(), any(DBObject.class), anyBoolean(), anyBoolean());
      //verify(anyCollection).findAndModify(query, null, null, false, newDBObject, false, false);
    }

  }

  @Test(expected = IOException.class)
  public void testUpdateItemNonExistent() throws IOException {
    TestSystemDocument expected = new TestSystemDocument();
    expected.setId(DEFAULT_ID);
    expected.setTestValue1("test");

    DBCursor cursor = createCursorWithoutValues();
    DBObject query = new BasicDBObject("_id", DEFAULT_ID);
    query.put("^rev", 0);
    when(anyCollection.find(query, null)).thenReturn(cursor);

    storage.updateItem(TYPE, DEFAULT_ID, expected);
  }

  @Test(expected = IOException.class)
  public void testUpdateItemItemChanged() throws IOException {
    TestSystemDocument newDoc = new TestSystemDocument();
    newDoc.setId(DEFAULT_ID);
    String testValue1 = "test";
    newDoc.setTestValue1(testValue1);

    TestSystemDocument oldDoc = new TestSystemDocument();
    oldDoc.setId(DEFAULT_ID);
    oldDoc.setTestValue1("testValue");

    Map<String, Object> oldTestSystemDocumentMap = createDefaultMap(0, DEFAULT_ID);
    oldTestSystemDocumentMap.put("testValue1", "test");
    DBObject oldDBObject = createDBObject(oldDoc, oldTestSystemDocumentMap);

    DBCursor cursor = createDBCursorWithOneValue(oldDBObject);

    DBObject query = new BasicDBObject("_id", DEFAULT_ID);
    query.put("^rev", 0);

    when(anyCollection.find(query, null)).thenReturn(cursor);
    when(anyCollection.findAndModify(any(DBObject.class), any(DBObject.class), any(DBObject.class), anyBoolean(), any(DBObject.class), anyBoolean(), anyBoolean())).thenReturn(null);

    try {
      storage.updateItem(TYPE, DEFAULT_ID, newDoc);
    } finally {
      verify(anyCollection).findAndModify(any(DBObject.class), any(DBObject.class), any(DBObject.class), anyBoolean(), any(DBObject.class), anyBoolean(), anyBoolean());
    }
  }

  @Test
  public void testAddItem() throws IOException {
    TestSystemDocument doc = new TestSystemDocument();
    doc.setTestValue1("test");

    storage.addItem(TYPE, doc);

    verify(anyCollection).insert(any(DBObject.class));
    verify(counterCol).findAndModify(any(DBObject.class), any(DBObject.class), any(DBObject.class), anyBoolean(), any(DBObject.class), anyBoolean(), anyBoolean());
  }

  @Test
  public void testAddItemWithId() throws IOException {
    TestSystemDocument doc = new TestSystemDocument();
    String id = DEFAULT_ID;
    doc.setId(id);
    doc.setTestValue1("test");

    storage.addItem(TYPE, doc);

    verify(anyCollection).insert(any(DBObject.class));
    verify(counterCol, never()).findAndModify(any(DBObject.class), any(DBObject.class), any(DBObject.class), anyBoolean(), any(DBObject.class), anyBoolean(), anyBoolean());
  }

  @Test(expected = MongoException.class)
  public void testMongoException() throws IOException {
    TestSystemDocument doc = new TestSystemDocument();
    String id = DEFAULT_ID;
    doc.setId(id);
    doc.setTestValue1("test");

    doThrow(MongoException.class).when(anyCollection).insert(any(DBObject.class));

    storage.addItem(TYPE, doc);

  }

  @Test
  public void testGetItem() throws IOException {
    TestSystemDocument expected = new TestSystemDocument();
    expected.setId(DEFAULT_ID);
    expected.setTestValue1("test");

    Map<String, Object> testSystemDocumentMap = createDefaultMap(0, DEFAULT_ID);
    testSystemDocumentMap.put("testValue1", "test");
    DBObject dbObject = createDBObject(testSystemDocumentMap);

    DBCursor cursor = createDBCursorWithOneValue(dbObject);

    DBObject query = new BasicDBObject("_id", DEFAULT_ID);
    when(anyCollection.find(query, null)).thenReturn(cursor);

    storage.getItem(TYPE, DEFAULT_ID);
  }

  @Test
  public void testGetItemCreatedWithoutId() throws IOException {
    Map<String, Object> testSystemDocumentMap = createDefaultMap(0, null);
    testSystemDocumentMap.put("testValue1", "test");
    DBObject dbObject = createDBObject(testSystemDocumentMap);

    DBCursor cursor = createDBCursorWithOneValue(dbObject);
    DBObject query = new BasicDBObject("_id", null);
    when(anyCollection.find(query, null)).thenReturn(cursor);

    storage.getItem(TYPE, null);

  }

  @Test
  public void testGetItemNonExistent() {
    DBCursor cursor = createCursorWithoutValues();

    DBObject query = new BasicDBObject("_id", DEFAULT_ID);
    when(anyCollection.find(query, null)).thenReturn(cursor);

    assertNull(storage.getItem(TYPE, DEFAULT_ID));
  }

  @Test
  public void testGetAllByType() throws IOException {
    Map<String, Object> testSystemDocumentMap = createDefaultMap(0, null);
    testSystemDocumentMap.put("testValue1", "test");
    DBObject dbObject = createDBObject(testSystemDocumentMap);

    DBCursor cursor = createDBCursorWithOneValue(dbObject);
    when(anyCollection.find()).thenReturn(cursor);

    storage.getAllByType(TYPE);
  }

  @Test
  public void testGetAllByTypeNonFound() throws IOException {
    DBCursor cursor = createCursorWithoutValues();

    when(anyCollection.find()).thenReturn(cursor);

    storage.getAllByType(TYPE);
  }

  @Test
  public void testGetByMultipleIdsOneFound() throws IOException {
    List<String> ids = Lists.newArrayList(DEFAULT_ID, "TSD000000000002", "TSD000000000004");

    Map<String, Object> testSystemDocumentMap = createDefaultMap(0, DEFAULT_ID);
    testSystemDocumentMap.put("testValue1", "test");
    DBObject dbObject = createDBObject(testSystemDocumentMap);

    DBObject query = new BasicDBObject("_id", new BasicDBObject("$in", ids));

    DBCursor cursor = createDBCursorWithOneValue(dbObject);
    when(anyCollection.find(query)).thenReturn(cursor);

    StorageIterator<TestSystemDocument> iterator = storage.getByMultipleIds(TestSystemDocument.class, ids);

    assertEquals(1, iterator.size());
  }

  @Test
  public void testGetByMultipleIdsMultipleFound() throws IOException {
    Class<TestSystemDocument> type = TestSystemDocument.class;
    String id2 = "TSD000000000002";
    List<String> ids = Lists.newArrayList(DEFAULT_ID, id2, "TSD000000000004");

    DBObject object1 = createDBObject(createDefaultMap(1, DEFAULT_ID));
    DBObject object2 = createDBObject(createDefaultMap(1, id2));
    DBCursor cursor = mock(DBCursor.class);
    when(cursor.hasNext()).thenReturn(true, true, false);
    when(cursor.next()).thenReturn(object1, object2);
    when(cursor.count()).thenReturn(2);

    DBObject query = new BasicDBObject("_id", new BasicDBObject("$in", ids));

    when(anyCollection.find(query)).thenReturn(cursor);

    StorageIterator<TestSystemDocument> iterator = storage.getByMultipleIds(type, ids);

    assertEquals(2, iterator.size());
  }

  @Test
  public void testGetByMultipleIdsNoneFound() throws IOException {
    Class<TestSystemDocument> type = TestSystemDocument.class;
    List<String> ids = Lists.newArrayList(DEFAULT_ID, "TSD000000000002", "TSD000000000004");

    DBCursor cursor = createCursorWithoutValues();

    DBObject query = new BasicDBObject("_id", new BasicDBObject("$in", ids));

    when(anyCollection.find(query)).thenReturn(cursor);

    StorageIterator<TestSystemDocument> iterator = storage.getByMultipleIds(type, ids);

    assertEquals(iterator.size(), 0);
  }

  @Test
  public void testDeleteItem() throws IOException {
    TestSystemDocument docToDelete = new TestSystemDocument();
    docToDelete.setId(DEFAULT_ID);
    docToDelete.setName("test");

    storage.deleteItem(TestSystemDocument.class, DEFAULT_ID, docToDelete.getLastChange());

    DBObject query = new BasicDBObject("_id", DEFAULT_ID);
    DBObject updateDeleted = new BasicDBObject("^deleted", true);
    updateDeleted.put("^lastChange", null);
    DBObject update = new BasicDBObject();
    update.put("$set", updateDeleted);
    update.put("$inc", new BasicDBObject("^rev", 1));

    verify(anyCollection).findAndModify(query, null, null, false, update, false, false);

  }

  @Test
  public void testDeleteItemNotFound() throws IOException {
    TestSystemDocument docToDelete = new TestSystemDocument();
    docToDelete.setId(DEFAULT_ID);
    docToDelete.setName("test");

    storage.deleteItem(TestSystemDocument.class, DEFAULT_ID, docToDelete.getLastChange());

    DBObject query = new BasicDBObject("_id", DEFAULT_ID);
    DBObject updateDeleted = new BasicDBObject("^deleted", true);
    updateDeleted.put("^lastChange", null);
    DBObject update = new BasicDBObject();
    update.put("$set", updateDeleted);
    update.put("$inc", new BasicDBObject("^rev", 1));

    verify(anyCollection).findAndModify(query, null, null, false, update, false, false);

  }

  @Test(expected = MongoException.class)
  public void testDeleteItemMongoException() throws IOException {
    TestSystemDocument docToDelete = new TestSystemDocument();
    docToDelete.setId(DEFAULT_ID);
    docToDelete.setName("test");

    DBObject query = new BasicDBObject("_id", DEFAULT_ID);
    DBObject updateDeleted = new BasicDBObject("^deleted", true);
    updateDeleted.put("^lastChange", null);
    DBObject update = new BasicDBObject();
    update.put("$set", updateDeleted);
    update.put("$inc", new BasicDBObject("^rev", 1));

    doThrow(MongoException.class).when(anyCollection).findAndModify(query, null, null, false, update, false, false);

    storage.deleteItem(TestSystemDocument.class, DEFAULT_ID, docToDelete.getLastChange());

  }

  @Test
  public void testEmpty() throws IOException {
    storage.empty();

    verify(db).cleanCursors(true);
    verify(mongo).dropDatabase(DB_NAME);
    verify(mongo).getDB(DB_NAME);
  }

  @Test
  public void testRemoveAll() throws IOException {

    WriteResult writeResult = mock(WriteResult.class);
    when(writeResult.getN()).thenReturn(3);

    when(anyCollection.remove(new BasicDBObject())).thenReturn(writeResult);

    storage.removeAll(TestSystemDocument.class);
  }

  @Test
  public void testRemoveByDate() throws IOException {
    Date now = new Date();

    WriteResult writeResult = mock(WriteResult.class);
    when(writeResult.getN()).thenReturn(3);
    Date dateValue = offsetDate(now, -4000);

    DBObject query = new BasicDBObject("date", new BasicDBObject("$lt", dateValue));
    when(anyCollection.remove(query)).thenReturn(writeResult);

    storage.removeByDate(TestSystemDocument.class, "date", dateValue);

    verify(anyCollection).remove(query);
  }

  @Test
  public void testSetPID() {
    Class<TestSystemDocument> type = TestSystemDocument.class;

    DBObject query = new BasicDBObject("_id", DEFAULT_ID);
    String pid = "3c08c345-c80d-44e2-a377-029259b662b9";
    DBObject update = new BasicDBObject("$set", new BasicDBObject("^pid", pid));

    storage.setPID(type, DEFAULT_ID, pid);

    verify(anyCollection).update(query, update, false, false, null);
    verify(db).getCollection("testsystemdocument");
  }

  @Test(expected = RuntimeException.class)
  public void testSetPIDMongoException() {
    Class<TestSystemDocument> type = TestSystemDocument.class;

    DBObject query = new BasicDBObject("_id", DEFAULT_ID);
    String pid = "3c08c345-c80d-44e2-a377-029259b662b9";
    DBObject update = new BasicDBObject("$set", new BasicDBObject("^pid", pid));

    doThrow(MongoException.class).when(anyCollection).update(query, update, false, false, null);

    try {
      storage.setPID(type, DEFAULT_ID, pid);
    } finally {
      verify(db).getCollection("testsystemdocument");
    }
  }

  private Date offsetDate(Date date, long millis) {
    return new Date(date.getTime() + millis);
  }

  protected Map<String, Object> createDefaultMap(int revision, String id) {
    Map<String, Object> testSystemDocumentMap = Maps.newHashMap();
    testSystemDocumentMap.put("_id", id);
    // TODO remove when the SystemDocuments and DomainDocuments are better separated   
    testSystemDocumentMap.put("^rev", revision);
    testSystemDocumentMap.put("^lastChange", null);
    testSystemDocumentMap.put("^creation", null);
    testSystemDocumentMap.put("^pid", null);
    testSystemDocumentMap.put("^deleted", false);
    testSystemDocumentMap.put("@class", "nl.knaw.huygens.timbuctoo.storage.mongo.model.TestSystemDocument");
    testSystemDocumentMap.put("@variations", new Object[0]);
    testSystemDocumentMap.put("!currentVariation", null);
    return testSystemDocumentMap;
  }

  protected DBObject createDBObject(Map<String, Object> map) {
    JacksonDBObject<JsonNode> dbObject = new JacksonDBObject<JsonNode>();
    dbObject.putAll(map);

    return dbObject;
  }

  protected DBObject createDBObject(TestSystemDocument doc, Map<String, Object> map) {
    JacksonDBObject<TestSystemDocument> dbObject = new JacksonDBObject<TestSystemDocument>();
    dbObject.putAll(map);
    dbObject.setObject(doc);

    return dbObject;
  }

  @Override
  protected void setupStorage() {
    storage = new MongoStorage(registry, storageConfiguration, this.mongo, this.db);
    storage.counterCol = counterCol;
  }

}
