package nl.knaw.huygens.timbuctoo.storage.mongo;

import static nl.knaw.huygens.timbuctoo.storage.FieldMapper.propertyName;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Date;
import java.util.Map;

import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.variation.model.TestSystemEntity;

import org.junit.BeforeClass;
import org.junit.Test;
import org.mongojack.internal.stream.JacksonDBObject;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Maps;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoException;
import com.mongodb.WriteResult;

/**
 * These unit tests all only check the interaction with the Mongo database.
 */
public class MongoStorageTest extends MongoStorageTestBase {

  private static final String DEFAULT_ID = "TSTD000000000001";

  private static final Class<TestSystemEntity> TYPE = TestSystemEntity.class;

  private static TypeRegistry registry;

  private MongoStorage storage;

  @BeforeClass
  public static void setupTypeRegistry() {
    registry = new TypeRegistry(TYPE.getPackage().getName());
  }

  @Override
  protected void setupStorage() {
    storage = new MongoStorage(registry, mongo, db, entityIds);
  }

  @Test
  public void testFindItemOneSearchProperty() throws IOException {
    String name = "doc1";
    TestSystemEntity example = new TestSystemEntity();
    example.setName(name);

    Map<String, Object> map = createDefaultMap(0, DEFAULT_ID);
    map.put(propertyName(TYPE, "name"), name);
    DBObject dbObject = createDBObject(map);

    DBCursor cursor = createDBCursorWithOneValue(dbObject);

    DBObject query = queries.selectByProperty(TYPE, "name", name);
    when(anyCollection.find(query, null)).thenReturn(cursor);

    storage.findItem(TYPE, example);
  }

  @Test
  public void testFindItemMultipleSearchProperties() throws IOException {
    TestSystemEntity example = new TestSystemEntity();
    String name = "doc2";
    example.setName(name);
    String testValue1 = "testValue";
    example.setTestValue1(testValue1);

    Map<String, Object> map = createDefaultMap(0, DEFAULT_ID);
    map.put(propertyName(TYPE, "name"), name);
    map.put(propertyName(TYPE, "testValue1"), testValue1);
    DBObject dbObject = createDBObject(map);

    DBCursor cursor = createDBCursorWithOneValue(dbObject);

    DBObject query = queries.selectByProperty(TYPE, "name", name);
    query.put(propertyName(TYPE, "testValue1"), testValue1);
    when(anyCollection.find(query, null)).thenReturn(cursor);

    storage.findItem(TYPE, example);
  }

  @Test
  public void testFindItemUnknownCollection() throws IOException {
    TestSystemEntity example = new TestSystemEntity();
    example.setName("nonExisting");

    DBCursor cursor = createCursorWithoutValues();

    DBObject query = queries.selectByProperty(TYPE, "name", "nonExisting");
    when(anyCollection.find(query, null)).thenReturn(cursor);

    storage.findItem(TYPE, example);
  }

  @Test(expected = IOException.class)
  public void testUpdateItemNonExistent() throws IOException {
    TestSystemEntity entity = new TestSystemEntity(DEFAULT_ID);
    entity.setTestValue1("test");

    DBCursor cursor = createCursorWithoutValues();
    DBObject query = queries.selectByIdAndRevision(DEFAULT_ID, 0);
    when(anyCollection.find(query)).thenReturn(cursor);

    storage.updateItem(TYPE, DEFAULT_ID, entity);
  }

  @Test
  public void testAddItem() throws IOException {
    TestSystemEntity entity = new TestSystemEntity();
    entity.setTestValue1("test");

    storage.addItem(TYPE, entity);

    verify(anyCollection).insert(any(DBObject.class));
  }

  @Test
  public void testAddItemWithId() throws IOException {
    TestSystemEntity entity = new TestSystemEntity(DEFAULT_ID);
    entity.setTestValue1("test");

    storage.addItem(TYPE, entity);

    verify(anyCollection).insert(any(DBObject.class));
  }

  @Test(expected = MongoException.class)
  public void testMongoException() throws IOException {
    TestSystemEntity entity = new TestSystemEntity(DEFAULT_ID);
    entity.setTestValue1("test");

    doThrow(MongoException.class).when(anyCollection).insert(any(DBObject.class));

    storage.addItem(TYPE, entity);
  }

  @Test
  public void testGetItem() throws IOException {
    TestSystemEntity entity = new TestSystemEntity(DEFAULT_ID);
    entity.setTestValue1("test");

    Map<String, Object> map = createDefaultMap(0, DEFAULT_ID);
    map.put("testValue1", "test");
    DBObject dbObject = createDBObject(map);

    DBCursor cursor = createDBCursorWithOneValue(dbObject);

    DBObject query = queries.selectById(DEFAULT_ID);
    when(anyCollection.find(query)).thenReturn(cursor);

    storage.getItem(TYPE, DEFAULT_ID);
  }

  @Test
  public void testGetItemCreatedWithoutId() throws IOException {
    Map<String, Object> map = createDefaultMap(0, null);
    map.put("testValue1", "test");
    DBObject dbObject = createDBObject(map);

    DBCursor cursor = createDBCursorWithOneValue(dbObject);
    DBObject query = queries.selectById(null);
    when(anyCollection.find(query)).thenReturn(cursor);

    storage.getItem(TYPE, (String) null);
  }

  @Test
  public void testGetItemNonExistent() throws IOException {
    DBCursor cursor = createCursorWithoutValues();

    DBObject query = queries.selectById(DEFAULT_ID);
    when(anyCollection.find(query)).thenReturn(cursor);

    assertNull(storage.getItem(TYPE, DEFAULT_ID));
  }

  @Test
  public void testGetAllByType() throws IOException {
    Map<String, Object> map = createDefaultMap(0, null);
    map.put("testValue1", "test");
    DBObject dbObject = createDBObject(map);

    DBCursor cursor = createDBCursorWithOneValue(dbObject);
    when(anyCollection.find()).thenReturn(cursor);

    storage.getAllByType(TYPE);
  }

  @Test
  public void testRemoveItem() throws IOException {
    storage.removeItem(TYPE, DEFAULT_ID);
    // just verify that the underlying storage is called
    // whether that call is successful or not is irrelevant
    verify(anyCollection).remove(any(DBObject.class));
  }

  @Test
  public void testRemoveAll() throws IOException {
    WriteResult writeResult = mock(WriteResult.class);
    when(writeResult.getN()).thenReturn(3);

    DBObject query = queries.selectAll();
    when(anyCollection.remove(query)).thenReturn(writeResult);

    storage.removeAll(TYPE);
  }

  @Test
  public void testRemoveByDate() throws IOException {
    Date now = new Date();

    WriteResult writeResult = mock(WriteResult.class);
    when(writeResult.getN()).thenReturn(3);
    Date dateValue = offsetDate(now, -4000);

    DBObject query = new BasicDBObject("date", new BasicDBObject("$lt", dateValue));
    when(anyCollection.remove(query)).thenReturn(writeResult);

    storage.removeByDate(TYPE, "date", dateValue);

    verify(anyCollection).remove(query);
  }

  private Date offsetDate(Date date, long millis) {
    return new Date(date.getTime() + millis);
  }

  private Map<String, Object> createDefaultMap(int revision, String id) {
    Map<String, Object> map = Maps.newHashMap();
    map.put("_id", id);
    map.put("^rev", revision);
    map.put("^lastChange", null);
    map.put("^creation", null);
    map.put(DomainEntity.PID, null);
    map.put(DomainEntity.DELETED, false);
    return map;
  }

  private DBObject createDBObject(Map<String, Object> map) {
    JacksonDBObject<JsonNode> dbObject = new JacksonDBObject<JsonNode>();
    dbObject.putAll(map);
    return dbObject;
  }

}
