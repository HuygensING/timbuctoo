package nl.knaw.huygens.timbuctoo.storage.mongo;

/*
 * #%L
 * Timbuctoo core
 * =======
 * Copyright (C) 2012 - 2014 Huygens ING
 * =======
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import static nl.knaw.huygens.timbuctoo.storage.FieldMapper.propertyName;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.util.Date;
import java.util.Map;

import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.storage.StorageException;
import nl.knaw.huygens.timbuctoo.variation.model.TestSystemEntity;

import org.junit.AfterClass;
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

  private static TypeRegistry registry;

  private MongoStorage storage;

  @BeforeClass
  public static void setupRegistry() {
    registry = TypeRegistry.getInstance();
    registry.init(TestSystemEntity.class.getPackage().getName());
  }

  @AfterClass
  public static void clearRegistry() {
    registry = null;
  }

  @Override
  protected void setupStorage() {
    storage = new MongoStorage(registry, mongo, db, entityIds);
  }

  @Test
  public void testFindItemOneSearchProperty() throws Exception {
    String name = "doc1";
    TestSystemEntity example = new TestSystemEntity();
    example.setName(name);

    Map<String, Object> map = createDefaultMap(0, DEFAULT_ID);
    map.put(propertyName(TestSystemEntity.class, "name"), name);
    DBObject dbObject = createDBObject(map);

    DBCursor cursor = createDBCursorWithOneValue(dbObject);

    DBObject query = queries.selectByProperty(TestSystemEntity.class, "name", name);
    when(anyCollection.find(query, null)).thenReturn(cursor);

    storage.findItem(TestSystemEntity.class, example);
  }

  @Test
  public void testFindItemMultipleSearchProperties() throws Exception {
    TestSystemEntity example = new TestSystemEntity();
    String name = "doc2";
    example.setName(name);
    String testValue1 = "testValue";
    example.setTestValue1(testValue1);

    Map<String, Object> map = createDefaultMap(0, DEFAULT_ID);
    map.put(propertyName(TestSystemEntity.class, "name"), name);
    map.put(propertyName(TestSystemEntity.class, "testValue1"), testValue1);
    DBObject dbObject = createDBObject(map);

    DBCursor cursor = createDBCursorWithOneValue(dbObject);

    DBObject query = queries.selectByProperty(TestSystemEntity.class, "name", name);
    query.put(propertyName(TestSystemEntity.class, "testValue1"), testValue1);
    when(anyCollection.find(query, null)).thenReturn(cursor);

    storage.findItem(TestSystemEntity.class, example);
  }

  @Test
  public void testFindItemUnknownCollection() throws Exception {
    TestSystemEntity example = new TestSystemEntity();
    example.setName("nonExisting");

    DBCursor cursor = createCursorWithoutValues();

    DBObject query = queries.selectByProperty(TestSystemEntity.class, "name", "nonExisting");
    when(anyCollection.find(query, null)).thenReturn(cursor);

    storage.findItem(TestSystemEntity.class, example);
  }

  @Test(expected = StorageException.class)
  public void testMongoException() throws Exception {
    TestSystemEntity entity = new TestSystemEntity(DEFAULT_ID, "test");

    doThrow(MongoException.class).when(anyCollection).insert(any(DBObject.class));

    storage.addSystemEntity(TestSystemEntity.class, entity);
  }

  @Test
  public void testGetItem() throws Exception {
    TestSystemEntity entity = new TestSystemEntity(DEFAULT_ID);
    entity.setTestValue1("test");

    Map<String, Object> map = createDefaultMap(0, DEFAULT_ID);
    map.put("testValue1", "test");
    DBObject dbObject = createDBObject(map);

    DBCursor cursor = createDBCursorWithOneValue(dbObject);

    DBObject query = queries.selectById(DEFAULT_ID);
    when(anyCollection.find(query)).thenReturn(cursor);

    storage.getItem(TestSystemEntity.class, DEFAULT_ID);
  }

  @Test
  public void testGetItemCreatedWithoutId() throws Exception {
    Map<String, Object> map = createDefaultMap(0, null);
    map.put("testValue1", "test");
    DBObject dbObject = createDBObject(map);

    DBCursor cursor = createDBCursorWithOneValue(dbObject);
    DBObject query = queries.selectById(null);
    when(anyCollection.find(query)).thenReturn(cursor);

    storage.getItem(TestSystemEntity.class, (String) null);
  }

  @Test
  public void testGetItemNonExistent() throws Exception {
    DBCursor cursor = createCursorWithoutValues();

    DBObject query = queries.selectById(DEFAULT_ID);
    when(anyCollection.find(query)).thenReturn(cursor);

    assertNull(storage.getItem(TestSystemEntity.class, DEFAULT_ID));
  }

  @Test
  public void testRemoveItem() throws Exception {
    storage.deleteSystemEntity(TestSystemEntity.class, DEFAULT_ID);
    // just verify that the underlying storage is called
    // whether that call is successful or not is irrelevant
    verify(anyCollection).remove(any(DBObject.class));
  }

  @Test
  public void testRemoveAll() throws Exception {
    WriteResult writeResult = mock(WriteResult.class);
    when(writeResult.getN()).thenReturn(3);

    DBObject query = queries.selectAll();
    when(anyCollection.remove(query)).thenReturn(writeResult);

    storage.deleteAll(TestSystemEntity.class);
  }

  @Test
  public void testRemoveByDate() throws Exception {
    injectMockMongoQueries();

    Date date = new Date();
    DBObject query = new BasicDBObject();
    when(queries.selectByDate(TestSystemEntity.class, "date", date)).thenReturn(query);

    storage.deleteByDate(TestSystemEntity.class, "date", date);

    verify(anyCollection).remove(query);
  }

  private void injectMockMongoQueries() throws Exception {
    queries = mock(MongoQueries.class);
    Field field = MongoStorage.class.getDeclaredField("queries");
    field.setAccessible(true);
    field.set(storage, queries);
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
