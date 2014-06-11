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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.UnknownHostException;
import java.util.List;
import java.util.Map;

import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.model.ModelException;
import nl.knaw.huygens.timbuctoo.storage.EntityInducer;
import nl.knaw.huygens.timbuctoo.storage.EntityReducer;
import nl.knaw.huygens.timbuctoo.storage.StorageException;
import nl.knaw.huygens.timbuctoo.variation.model.BaseDomainEntity;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoException;

public class MongoVariationStorageTest extends MongoStorageTestBase {

  private static TypeRegistry registry;

  @BeforeClass
  public static void setupRegistry() throws ModelException {
    registry = TypeRegistry.getInstance().init("timbuctoo.model timbuctoo.variation.model.*");
  }

  @AfterClass
  public static void clearRegistry() {
    registry = null;
  }

  // ---------------------------------------------------------------------------

  private MongoStorage storage;
  private DBObject returnIdField;

  @Override
  protected void setupStorage() throws UnknownHostException, MongoException {
    EntityInducer inducer = new EntityInducer();
    EntityReducer reducer = new EntityReducer(registry);
    storage = new MongoStorage(new MongoDB(mongo, db), entityIds, inducer, reducer);
    returnIdField = new BasicDBObject("_id", 1);
  }

  private Map<String, Object> createSimpleMap(String id, Object value) {
    Map<String, Object> map = Maps.newHashMap();
    map.put(id, value);
    return map;
  }

  /**
   * Creates a JsonNode from a map. This is used is several tests.
   */
  private DBJsonNode createDBJsonNode(Map<String, Object> map) {
    ObjectMapper mapper = new ObjectMapper();
    return new DBJsonNode(mapper.valueToTree(map));
  }

  // ---------------------------------------------------------------------------

  @Test
  public void testGetAllIdsWithoutPIDOfType() throws Exception {
    DBObject query = queries.selectVariationWithoutPID(BaseDomainEntity.class);

    String id1 = "TSD0000000001";
    DBObject dbObject = createDBJsonNode(createSimpleMap("_id", id1));

    DBCursor cursor = createDBCursorWithOneValue(dbObject);
    when(dbCollection.find(query, returnIdField)).thenReturn(cursor);

    List<String> ids = storage.getAllIdsWithoutPIDOfType(BaseDomainEntity.class);

    assertEquals(1, ids.size());
    assertTrue(ids.contains(id1));
  }

  @Test
  public void testGetAllIdsWithoutPIDOfTypeMultipleFound() throws Exception {
    DBObject query = queries.selectVariationWithoutPID(BaseDomainEntity.class);

    String id1 = DEFAULT_ID;
    DBObject dbObject1 = createDBJsonNode(createSimpleMap("_id", id1));
    String id2 = "TCD000000002";
    DBObject dbObject2 = createDBJsonNode(createSimpleMap("_id", id2));
    String id3 = "TCD000000003";
    DBObject dbObject3 = createDBJsonNode(createSimpleMap("_id", id3));

    DBCursor cursor = mock(DBCursor.class);
    when(cursor.next()).thenReturn(dbObject1, dbObject2, dbObject3);
    when(cursor.hasNext()).thenReturn(true, true, true, false);

    when(dbCollection.find(query, returnIdField)).thenReturn(cursor);

    List<String> ids = storage.getAllIdsWithoutPIDOfType(BaseDomainEntity.class);

    assertEquals(3, ids.size());
    assertTrue(ids.contains(id1));
    assertTrue(ids.contains(id2));
    assertTrue(ids.contains(id3));
  }

  @Test
  public void testGetAllIdsWithoutPIDOfTypeNoneFound() throws Exception {
    DBObject query = queries.selectVariationWithoutPID(BaseDomainEntity.class);
    DBCursor cursor = createCursorWithoutValues();
    when(dbCollection.find(query, returnIdField)).thenReturn(cursor);

    List<String> ids = storage.getAllIdsWithoutPIDOfType(BaseDomainEntity.class);

    assertTrue(ids.isEmpty());
  }

  @Test(expected = StorageException.class)
  public void testGetAllIdsWithoutPIDFindThrowsException() throws Exception {
    DBObject query = queries.selectVariationWithoutPID(BaseDomainEntity.class);
    doThrow(MongoException.class).when(dbCollection).find(query, returnIdField);

    storage.getAllIdsWithoutPIDOfType(BaseDomainEntity.class);
  }

  @Test(expected = StorageException.class)
  public void testGetAllIdsWithoutPIDCursorNextThrowsException() throws Exception {
    DBObject query = queries.selectVariationWithoutPID(BaseDomainEntity.class);
    DBCursor cursor = mock(DBCursor.class);
    when(cursor.hasNext()).thenReturn(true);
    doThrow(MongoException.class).when(cursor).next();
    when(dbCollection.find(query, returnIdField)).thenReturn(cursor);

    storage.getAllIdsWithoutPIDOfType(BaseDomainEntity.class);
  }

  @Test(expected = StorageException.class)
  public void testGetAllIdsWithoutPIDCursorHasNextThrowsException() throws Exception {
    DBObject query = queries.selectVariationWithoutPID(BaseDomainEntity.class);
    DBCursor cursor = mock(DBCursor.class);
    doThrow(MongoException.class).when(cursor).hasNext();

    when(dbCollection.find(query, returnIdField)).thenReturn(cursor);

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

    when(dbCollection.find(query, returnIdField)).thenReturn(cursor);

    try {
      List<String> relationsIds = storage.getRelationIds(inputIds);

      assertTrue(relationsIds.contains(relationId1));
      assertTrue(relationsIds.contains(relationId2));
      assertTrue(relationsIds.contains(relationId3));
    } finally {
      verify(dbCollection).find(query, returnIdField);
      verify(db).getCollection("relation");
    }
  }

  @Test(expected = StorageException.class)
  public void testGetRelationFindThrowsException() throws Exception {
    List<String> inputIds = Lists.newArrayList(DEFAULT_ID, "TCD000000002", "TCD000000003");
    doThrow(MongoException.class).when(dbCollection).find(any(DBObject.class), any(DBObject.class));

    storage.getRelationIds(inputIds);
  }

  @Test(expected = StorageException.class)
  public void testGetRelationCursorNextThrowsException() throws Exception {
    List<String> inputIds = Lists.newArrayList(DEFAULT_ID, "TCD000000002", "TCD000000003");

    DBObject query = createRelatedToQuery(inputIds);

    DBCursor cursor = mock(DBCursor.class);
    when(cursor.hasNext()).thenReturn(true);
    doThrow(MongoException.class).when(cursor).next();

    when(dbCollection.find(query, returnIdField)).thenReturn(cursor);

    storage.getRelationIds(inputIds);

  }

  @Test(expected = StorageException.class)
  public void testGetRelationCursorHasNextThrowsException() throws Exception {
    List<String> inputIds = Lists.newArrayList(DEFAULT_ID, "TCD000000002", "TCD000000003");

    DBObject query = createRelatedToQuery(inputIds);

    DBCursor cursor = mock(DBCursor.class);
    doThrow(MongoException.class).when(cursor).hasNext();

    when(dbCollection.find(query, returnIdField)).thenReturn(cursor);

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

}
