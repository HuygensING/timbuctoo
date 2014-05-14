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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.UnknownHostException;
import java.util.List;
import java.util.Map;

import nl.knaw.huygens.timbuctoo.config.TypeNames;
import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.storage.StorageException;
import nl.knaw.huygens.timbuctoo.variation.model.BaseDomainEntity;
import nl.knaw.huygens.timbuctoo.variation.model.TestConcreteDoc;
import nl.knaw.huygens.timbuctoo.variation.model.projecta.ProjectADomainEntity;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mongojack.DBQuery;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoException;

public class MongoVariationStorageTest extends MongoStorageTestBase {

  private static final String DEFAULT_ID = "TCD000000001";

  private static TypeRegistry registry;

  private MongoStorage storage;
  private DBObject returnIdField;

  @BeforeClass
  public static void setupRegistry() {
    registry = TypeRegistry.getInstance();
    registry.init("timbuctoo.model timbuctoo.variation.model timbuctoo.variation.model.projecta timbuctoo.variation.model.projectb");
  }

  @AfterClass
  public static void clearRegistry() {
    registry = null;
  }

  @Override
  protected void setupStorage() throws UnknownHostException, MongoException {
    storage = new MongoStorage(registry, mongo, db, entityIds);
    returnIdField = new BasicDBObject("_id", 1);
  }

  private DBObject createTestConcreteDocDBObject(String id, String name) {
    Map<String, Object> map = createDefaultMap(id);
    map.put(propertyName(TestConcreteDoc.class, "name"), name);
    return createDBJsonNode(map);
  }

  private DBObject createGeneralTestDocDBObject(String id, String name, String generalTestDocValue) {
    Map<String, Object> map = createDefaultMap(id);
    map.put(propertyName(TestConcreteDoc.class, "name"), name);
    map.put(propertyName(BaseDomainEntity.class, "generalTestDocValue"), generalTestDocValue);
    return createDBJsonNode(map);
  }

  private Map<String, Object> createSimpleMap(String id, Object value) {
    Map<String, Object> map = Maps.newHashMap();
    map.put(id, value);
    return map;
  }

  private Map<String, Object> createDefaultMap(String id) {
    Map<String, Object> map = Maps.newHashMap();
    map.put("_id", id);
    map.put("^rev", 0);
    map.put(DomainEntity.DELETED, false);
    return map;
  }

  /**
   * Creates a JsonNode from a map. This is used is several tests.
   */
  private DBJsonNode createDBJsonNode(Map<String, Object> map) {
    ObjectMapper mapper = new ObjectMapper();
    return new DBJsonNode(mapper.valueToTree(map));
  }

  // -------------------------------------------------------------------

  @Test
  public void testGetItem() throws Exception {
    String name = "getItem";
    BaseDomainEntity expected = new BaseDomainEntity(DEFAULT_ID);
    expected.name = name;

    DBObject dbObject = createTestConcreteDocDBObject(DEFAULT_ID, name);

    DBObject query = queries.selectById(DEFAULT_ID);
    when(anyCollection.findOne(query)).thenReturn(dbObject);

    assertEquals(DEFAULT_ID, storage.getItem(BaseDomainEntity.class, DEFAULT_ID).getId());
  }

  @Test
  public void testGetItemNonExistent() throws Exception {
    assertNull(storage.getItem(BaseDomainEntity.class, "TCD000000001"));
  }

  @Test
  // Reported as failure [#1919] with expected value 5
  // But by using createCursorWithoutValues you obviously get 0
  public void testGetAllVariationsWithoutRelations() throws Exception {
    DBObject value = createGeneralTestDocDBObject(DEFAULT_ID, "subType", "test");
    when(anyCollection.findOne(any(DBObject.class))).thenReturn(value);

    DBCursor cursor = createCursorWithoutValues();
    when(anyCollection.find(any(DBObject.class))).thenReturn(cursor);

    assertEquals(0, storage.getAllVariations(BaseDomainEntity.class, DEFAULT_ID).size());
  }

  @Test
  public void testGetEntities() throws Exception {
    DBObject query = queries.selectAll();
    DBCursor cursor = createCursorWithoutValues();
    when(anyCollection.find(query)).thenReturn(cursor);

    storage.getEntities(ProjectADomainEntity.class);
    verify(anyCollection).find(query);
  }

  @Test
  public void testGetAllRevisions() throws Exception {
    storage.getAllRevisions(BaseDomainEntity.class, DEFAULT_ID);
    verify(anyCollection).findOne(new BasicDBObject("_id", DEFAULT_ID));
  }

  @Test
  public void testGetRevision() throws Exception {
    int revisionId = 0;
    storage.getRevision(ProjectADomainEntity.class, DEFAULT_ID, revisionId);

    DBObject query = queries.selectById(DEFAULT_ID);
    DBObject projection = queries.getRevisionProjection(revisionId);

    verify(anyCollection).findOne(query, projection);
  }

  @Test
  public void testGetAllIdsWithoutPIDOfType() throws Exception {
    String collection = TypeNames.getInternalName(BaseDomainEntity.class);
    DBObject query = queries.selectVariation(collection);
    query.putAll(DBQuery.notExists(DomainEntity.PID));

    String id1 = "TSD0000000001";
    DBObject dbObject = createDBJsonNode(createSimpleMap("_id", id1));

    DBCursor cursor = createDBCursorWithOneValue(dbObject);
    when(anyCollection.find(query, returnIdField)).thenReturn(cursor);

    List<String> ids = storage.getAllIdsWithoutPIDOfType(BaseDomainEntity.class);

    assertTrue(ids.contains(id1));

    verify(anyCollection).find(query, returnIdField);
    verify(db).getCollection(collection);
  }

  @Test
  public void testGetAllIdsWithoutPIDOfTypeMultipleFound() throws Exception {
    String collection = TypeNames.getInternalName(BaseDomainEntity.class);
    DBObject query = queries.selectVariation(collection);
    query.putAll(DBQuery.notExists(DomainEntity.PID));

    String id1 = DEFAULT_ID;
    DBObject dbObject1 = createDBJsonNode(createSimpleMap("_id", id1));
    String id2 = "TCD000000002";
    DBObject dbObject2 = createDBJsonNode(createSimpleMap("_id", id2));
    String id3 = "TCD000000003";
    DBObject dbObject3 = createDBJsonNode(createSimpleMap("_id", id3));

    DBCursor cursor = mock(DBCursor.class);
    when(cursor.next()).thenReturn(dbObject1, dbObject2, dbObject3);
    when(cursor.hasNext()).thenReturn(true, true, true, false);

    when(anyCollection.find(query, returnIdField)).thenReturn(cursor);

    List<String> ids = storage.getAllIdsWithoutPIDOfType(BaseDomainEntity.class);

    assertTrue(ids.contains(id1));
    assertTrue(ids.contains(id2));
    assertTrue(ids.contains(id3));

    verify(anyCollection).find(query, returnIdField);
    verify(db).getCollection(collection);
  }

  @Test
  public void testGetAllIdsWithoutPIDOfTypeNoneFound() throws Exception {
    String collection = TypeNames.getInternalName(BaseDomainEntity.class);
    DBObject query = queries.selectVariation(collection);
    query.putAll(DBQuery.notExists(DomainEntity.PID));

    DBCursor cursor = createCursorWithoutValues();
    when(anyCollection.find(query, returnIdField)).thenReturn(cursor);

    List<String> ids = storage.getAllIdsWithoutPIDOfType(BaseDomainEntity.class);

    assertTrue(ids.isEmpty());

    verify(anyCollection).find(query, returnIdField);
    verify(db).getCollection(collection);
  }

  @Test(expected = StorageException.class)
  public void testGetAllIdsWithoutPIDFindThrowsException() throws Exception {
    String collection = TypeNames.getInternalName(BaseDomainEntity.class);
    DBObject query = queries.selectVariation(collection);
    query.putAll(DBQuery.notExists(DomainEntity.PID));

    doThrow(MongoException.class).when(anyCollection).find(query, returnIdField);

    storage.getAllIdsWithoutPIDOfType(BaseDomainEntity.class);
  }

  @Test(expected = StorageException.class)
  public void testGetAllIdsWithoutPIDCursorNextThrowsException() throws Exception {
    String collection = TypeNames.getInternalName(BaseDomainEntity.class);
    DBObject query = queries.selectVariation(collection);
    query.putAll(DBQuery.notExists(DomainEntity.PID));

    DBCursor cursor = mock(DBCursor.class);
    when(cursor.hasNext()).thenReturn(true);
    doThrow(MongoException.class).when(cursor).next();

    when(anyCollection.find(query, returnIdField)).thenReturn(cursor);

    storage.getAllIdsWithoutPIDOfType(BaseDomainEntity.class);
  }

  @Test(expected = StorageException.class)
  public void testGetAllIdsWithoutPIDCursorHasNextThrowsException() throws Exception {
    String collection = TypeNames.getInternalName(BaseDomainEntity.class);
    DBObject query = queries.selectVariation(collection);
    query.putAll(DBQuery.notExists(DomainEntity.PID));

    DBCursor cursor = mock(DBCursor.class);
    doThrow(MongoException.class).when(cursor).hasNext();

    when(anyCollection.find(query, returnIdField)).thenReturn(cursor);

    storage.getAllIdsWithoutPIDOfType(BaseDomainEntity.class);
  }

  @Test
  public void testGetRelationIds() throws Exception {
    List<String> inputIds = Lists.newArrayList(DEFAULT_ID, "TCD000000002", "TCD000000003");

    DBObject query = createRelatedToQuery(inputIds);

    String relationId1 = "RELA000000000001";
    String relationId2 = "RELA000000000002";
    String relationId3 = "RELA000000000003";

    DBObject dbObject1 = createDBJsonNode(createSimpleMap("_id", relationId1));
    DBObject dbObject2 = createDBJsonNode(createSimpleMap("_id", relationId2));
    DBObject dbObject3 = createDBJsonNode(createSimpleMap("_id", relationId3));

    DBCursor cursor = mock(DBCursor.class);
    when(cursor.next()).thenReturn(dbObject1, dbObject2, dbObject3);
    when(cursor.hasNext()).thenReturn(true, true, true, false);

    when(anyCollection.find(query, returnIdField)).thenReturn(cursor);

    try {
      List<String> relationsIds = storage.getRelationIds(inputIds);

      assertTrue(relationsIds.contains(relationId1));
      assertTrue(relationsIds.contains(relationId2));
      assertTrue(relationsIds.contains(relationId3));
    } finally {
      verify(anyCollection).find(query, returnIdField);
      verify(db).getCollection("relation");
    }
  }

  @Test(expected = StorageException.class)
  public void testGetRelationFindThrowsException() throws Exception {
    List<String> inputIds = Lists.newArrayList(DEFAULT_ID, "TCD000000002", "TCD000000003");
    doThrow(MongoException.class).when(anyCollection).find(any(DBObject.class), any(DBObject.class));

    storage.getRelationIds(inputIds);
  }

  @Test(expected = StorageException.class)
  public void testGetRelationCursorNextThrowsException() throws Exception {
    List<String> inputIds = Lists.newArrayList(DEFAULT_ID, "TCD000000002", "TCD000000003");

    DBObject query = createRelatedToQuery(inputIds);

    DBCursor cursor = mock(DBCursor.class);
    when(cursor.hasNext()).thenReturn(true);
    doThrow(MongoException.class).when(cursor).next();

    when(anyCollection.find(query, returnIdField)).thenReturn(cursor);

    storage.getRelationIds(inputIds);

  }

  @Test(expected = StorageException.class)
  public void testGetRelationCursorHasNextThrowsException() throws Exception {
    List<String> inputIds = Lists.newArrayList(DEFAULT_ID, "TCD000000002", "TCD000000003");

    DBObject query = createRelatedToQuery(inputIds);

    DBCursor cursor = mock(DBCursor.class);
    doThrow(MongoException.class).when(cursor).hasNext();

    when(anyCollection.find(query, returnIdField)).thenReturn(cursor);

    storage.getRelationIds(inputIds);
  }

  protected DBObject createRelatedToQuery(List<String> inputIds) {
    //You can not use DBQuery.or or DBQuery.in in tests, because the equals will fail.
    BasicDBObject inCollection = new BasicDBObject("$in", inputIds);
    DBObject sourceIdIn = new BasicDBObject("^sourceId", inCollection);
    DBObject targetIdIn = new BasicDBObject("^targetId", inCollection);
    DBObject query = new BasicDBObject("$or", Lists.newArrayList(sourceIdIn, targetIdIn));
    return query;
  }

  @Test
  public void testRemovePermanently() throws Exception {
    List<String> ids = Lists.newArrayList("TCD000000001", "TCD000000003", "TCD000000005");
    DBObject query = new BasicDBObject("_id", new BasicDBObject("$in", ids));
    query.put(DomainEntity.PID, null);

    storage.deleteNonPersistent(BaseDomainEntity.class, ids);

    verify(anyCollection).remove(query);
    verify(db).getCollection(TypeNames.getInternalName(BaseDomainEntity.class));
  }

  @Test(expected = StorageException.class)
  public void testRemovePemanentlyDBThrowsException() throws Exception {
    List<String> ids = Lists.newArrayList("TCD000000001", "TCD000000003", "TCD000000005");
    DBObject query = new BasicDBObject("_id", new BasicDBObject("$in", ids));
    query.put(DomainEntity.PID, null);
    doThrow(MongoException.class).when(anyCollection).remove(query);

    storage.deleteNonPersistent(BaseDomainEntity.class, ids);
  }

}
