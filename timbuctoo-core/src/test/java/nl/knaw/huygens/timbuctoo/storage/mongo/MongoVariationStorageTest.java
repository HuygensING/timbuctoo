package nl.knaw.huygens.timbuctoo.storage.mongo;

import static nl.knaw.huygens.timbuctoo.storage.FieldMapper.propertyName;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
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
import nl.knaw.huygens.timbuctoo.variation.model.projectb.ProjectBDomainEntity;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
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
  public static void setupTypeRegistry() {
    registry = new TypeRegistry("timbuctoo.model timbuctoo.variation.model timbuctoo.variation.model.projecta timbuctoo.variation.model.projectb");
  }

  @Override
  protected void setupStorage() throws UnknownHostException, MongoException {
    storage = new MongoStorage(registry, mongo, db, entityIds);
    returnIdField = new BasicDBObject("_id", 1);
  }

  @Test
  public void testAddItem() throws IOException {
    BaseDomainEntity input = new BaseDomainEntity(DEFAULT_ID, "test");

    storage.addItem(BaseDomainEntity.class, input);

    // Two additions one normal addition and one addition in the version table.
    verify(anyCollection, times(2)).insert(any(DBObject.class));
  }

  @Test
  public void addItemSubType() throws IOException {
    BaseDomainEntity input = new BaseDomainEntity();
    input.setId(DEFAULT_ID);
    input.name = "subType";
    input.generalTestDocValue = "test";

    storage.addItem(BaseDomainEntity.class, input);

    // Two additions one normal addition and one addition in the version table.
    verify(anyCollection, times(2)).insert(any(DBObject.class));
  }

  @Test
  public void testUpdateItem() throws IOException {
    BaseDomainEntity input = new BaseDomainEntity(DEFAULT_ID, "test");

    when(anyCollection.findOne(any(DBObject.class))).thenReturn(createTestConcreteDocDBObject(DEFAULT_ID, "test"));

    storage.updateItem(BaseDomainEntity.class, DEFAULT_ID, input);

    //Update current version and the version collection
    verify(anyCollection, times(2)).update(any(DBObject.class), any(DBObject.class));
  }

  @Test(expected = IOException.class)
  public void testUpdateItemNonExistent() throws IOException {
    BaseDomainEntity expected = new BaseDomainEntity(DEFAULT_ID, "test");
    storage.updateItem(BaseDomainEntity.class, DEFAULT_ID, expected);
  }

  @Test(expected = IOException.class)
  public void testUpdateItemMalformed() throws IOException {
    BaseDomainEntity item = new BaseDomainEntity(DEFAULT_ID, "test");

    when(anyCollection.findOne(any(DBObject.class))).thenReturn(new BasicDBObject("test", "test"));

    storage.updateItem(BaseDomainEntity.class, DEFAULT_ID, item);
  }

  @Test
  public void testDeletetem() throws IOException {
    DBObject dbObject = createTestConcreteDocDBObject(DEFAULT_ID, "test");

    JsonNode revisionNode = mock(JsonNode.class);
    when(revisionNode.asInt()).thenReturn(1);

    ObjectNode objectNode = mock(ObjectNode.class);
    when(objectNode.isObject()).thenReturn(true);
    when(objectNode.put(anyString(), anyBoolean())).thenReturn(objectNode);
    when(objectNode.put(anyString(), anyInt())).thenReturn(objectNode);
    when(objectNode.get("^rev")).thenReturn(revisionNode);

    DBJsonNode dbJsonNode = mock(DBJsonNode.class);
    when(dbJsonNode.getDelegate()).thenReturn(objectNode);

    when(anyCollection.findOne(new BasicDBObject("_id", DEFAULT_ID))).thenReturn(dbObject);

    storage.deleteItem(BaseDomainEntity.class, DEFAULT_ID, null);

    // Update the current version
    // Update the version number.
    verify(anyCollection, times(2)).update(any(DBObject.class), any(DBObject.class));
  }

  @Test(expected = IOException.class)
  public void testDeleteItemNonExistent() throws IOException {
    storage.deleteItem(BaseDomainEntity.class, DEFAULT_ID, null);
  }

  @Test
  public void testGetItem() throws IOException {
    String name = "getItem";
    BaseDomainEntity expected = new BaseDomainEntity(DEFAULT_ID);
    expected.name = name;

    DBObject dbObject = createTestConcreteDocDBObject(DEFAULT_ID, name);

    DBObject query = queries.selectById(DEFAULT_ID);
    when(anyCollection.findOne(query)).thenReturn(dbObject);

    assertEquals(DEFAULT_ID, storage.getItem(BaseDomainEntity.class, DEFAULT_ID).getId());
  }

  @Test
  public void testGetItemNonExistent() throws StorageException, IOException {
    assertNull(storage.getItem(BaseDomainEntity.class, "TCD000000001"));
  }

  @Ignore("See Redmine #1919")
  @Test
  public void testGetAllVariationsWithoutRelations() throws IOException {
    DBObject value = createGeneralTestDocDBObject(DEFAULT_ID, "subType", "test");
    when(anyCollection.findOne(any(DBObject.class))).thenReturn(value);

    DBCursor cursor = createCursorWithoutValues();
    when(anyCollection.find(any(DBObject.class))).thenReturn(cursor);

    assertEquals(5, storage.getAllVariations(BaseDomainEntity.class, DEFAULT_ID).size());
  }

  @Ignore("See Redmine #1919")
  @Test
  public void testGetVariation() throws IOException {
    DBObject query = new MongoQueries().selectById(DEFAULT_ID);

    String name = "name";
    DBObject projectAGeneralTestDBNode = createProjectAGeneralTestDBObject(DEFAULT_ID, name, "value1", "value2");

    when(anyCollection.findOne(query)).thenReturn(projectAGeneralTestDBNode);

    BaseDomainEntity actual = storage.getVariation(BaseDomainEntity.class, DEFAULT_ID, "projecta");

    assertEquals(name, actual.name);
    assertEquals(DEFAULT_ID, actual.getId());
  }

  @Test
  public void testGetVariationVariationNonExisting() throws IOException {
    DBObject query = queries.selectById(DEFAULT_ID);

    String name = "projecta";
    DBObject node = createProjectAGeneralTestDBObject(DEFAULT_ID, name, "value1", "value2");
    when(anyCollection.findOne(query)).thenReturn(node);

    TestConcreteDoc actual = storage.getVariation(TestConcreteDoc.class, DEFAULT_ID, "projectb");

    assertEquals(name, actual.name);
    assertEquals(DEFAULT_ID, actual.getId());
  }

  @Test
  public void testGetAllByType() throws IOException {
    DBObject query = queries.selectAll();
    DBCursor cursor = createCursorWithoutValues();
    when(anyCollection.find(query)).thenReturn(cursor);

    storage.getAllByType(ProjectBDomainEntity.class);
    verify(anyCollection).find(query);
  }

  @Test
  public void testGetAllRevisions() throws IOException {
    storage.getAllRevisions(BaseDomainEntity.class, DEFAULT_ID);
    verify(anyCollection).findOne(new BasicDBObject("_id", DEFAULT_ID));
  }

  @Test
  public void testGetRevision() throws IOException {
    int revisionId = 0;
    storage.getRevision(ProjectADomainEntity.class, DEFAULT_ID, revisionId);

    DBObject query = queries.selectById(DEFAULT_ID);
    query.put("versions.^rev", revisionId);

    verify(anyCollection).findOne(query);
  }

  @Test
  public void testGetAllIdsWithoutPIDOfType() throws IOException {
    String collection = TypeNames.getInternalName(BaseDomainEntity.class);
    DBObject query = queries.selectVariation(collection);
    query.put(DomainEntity.PID, null);

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
  public void testGetAllIdsWithoutPIDOfTypeMultipleFound() throws IOException {
    String collection = TypeNames.getInternalName(BaseDomainEntity.class);
    DBObject query = queries.selectVariation(collection);
    query.put(DomainEntity.PID, null);

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
  public void testGetAllIdsWithoutPIDOfTypeNoneFound() throws IOException {
    String collection = TypeNames.getInternalName(BaseDomainEntity.class);
    DBObject query = queries.selectVariation(collection);
    query.put(DomainEntity.PID, null);

    DBCursor cursor = createCursorWithoutValues();
    when(anyCollection.find(query, returnIdField)).thenReturn(cursor);

    List<String> ids = storage.getAllIdsWithoutPIDOfType(BaseDomainEntity.class);

    assertTrue(ids.isEmpty());

    verify(anyCollection).find(query, returnIdField);
    verify(db).getCollection(collection);
  }

  @Test(expected = IOException.class)
  public void testGetAllIdsWithoutPIDFindThrowsException() throws IOException {
    String collection = TypeNames.getInternalName(BaseDomainEntity.class);
    DBObject query = queries.selectVariation(collection);
    query.put(DomainEntity.PID, null);

    doThrow(MongoException.class).when(anyCollection).find(query, returnIdField);

    storage.getAllIdsWithoutPIDOfType(BaseDomainEntity.class);
  }

  @Test(expected = IOException.class)
  public void testGetAllIdsWithoutPIDCursorNextThrowsException() throws IOException {
    String collection = TypeNames.getInternalName(BaseDomainEntity.class);
    DBObject query = queries.selectVariation(collection);
    query.put(DomainEntity.PID, null);

    DBCursor cursor = mock(DBCursor.class);
    when(cursor.hasNext()).thenReturn(true);
    doThrow(MongoException.class).when(cursor).next();

    when(anyCollection.find(query, returnIdField)).thenReturn(cursor);

    storage.getAllIdsWithoutPIDOfType(BaseDomainEntity.class);
  }

  @Test(expected = IOException.class)
  public void testGetAllIdsWithoutPIDCursorHasNextThrowsException() throws IOException {
    String collection = TypeNames.getInternalName(BaseDomainEntity.class);
    DBObject query = queries.selectVariation(collection);
    query.put(DomainEntity.PID, null);

    DBCursor cursor = mock(DBCursor.class);
    doThrow(MongoException.class).when(cursor).hasNext();

    when(anyCollection.find(query, returnIdField)).thenReturn(cursor);

    storage.getAllIdsWithoutPIDOfType(BaseDomainEntity.class);
  }

  @Test
  public void testGetRelationIds() throws IOException {
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

  @Test(expected = IOException.class)
  public void testGetRelationFindThrowsException() throws IOException {
    List<String> inputIds = Lists.newArrayList(DEFAULT_ID, "TCD000000002", "TCD000000003");
    doThrow(MongoException.class).when(anyCollection).find(any(DBObject.class), any(DBObject.class));

    storage.getRelationIds(inputIds);
  }

  @Test(expected = IOException.class)
  public void testGetRelationCursorNextThrowsException() throws IOException {
    List<String> inputIds = Lists.newArrayList(DEFAULT_ID, "TCD000000002", "TCD000000003");

    DBObject query = createRelatedToQuery(inputIds);

    DBCursor cursor = mock(DBCursor.class);
    when(cursor.hasNext()).thenReturn(true);
    doThrow(MongoException.class).when(cursor).next();

    when(anyCollection.find(query, returnIdField)).thenReturn(cursor);

    storage.getRelationIds(inputIds);

  }

  @Test(expected = IOException.class)
  public void testGetRelationCursorHasNextThrowsException() throws IOException {
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
  public void testRemovePermanently() throws IOException {
    List<String> ids = Lists.newArrayList("TCD000000001", "TCD000000003", "TCD000000005");
    DBObject query = new BasicDBObject("_id", new BasicDBObject("$in", ids));
    query.put(DomainEntity.PID, null);

    storage.removeNonPersistent(BaseDomainEntity.class, ids);

    verify(anyCollection).remove(query);
    verify(db).getCollection(TypeNames.getInternalName(BaseDomainEntity.class));
  }

  @Test(expected = IOException.class)
  public void testRemovePemanentlyDBThrowsException() throws IOException {
    List<String> ids = Lists.newArrayList("TCD000000001", "TCD000000003", "TCD000000005");
    DBObject query = new BasicDBObject("_id", new BasicDBObject("$in", ids));
    query.put(DomainEntity.PID, null);
    doThrow(MongoException.class).when(anyCollection).remove(query);

    storage.removeNonPersistent(BaseDomainEntity.class, ids);
  }

  @Test
  public void testSetPID() {
    DBObject query = new BasicDBObject("_id", DEFAULT_ID);
    String pid = "3c08c345-c80d-44e2-a377-029259b662b9";
    DBObject update = queries.setProperty(DomainEntity.PID, pid);

    storage.setPID(ProjectADomainEntity.class, DEFAULT_ID, pid);

    verify(anyCollection).update(query, update);
    verify(db).getCollection(TypeNames.getInternalName(BaseDomainEntity.class));
  }

  @Test(expected = RuntimeException.class)
  public void testSetPIDMongoException() {
    DBObject query = new BasicDBObject("_id", DEFAULT_ID);
    String pid = "3c08c345-c80d-44e2-a377-029259b662b9";
    DBObject update = queries.setProperty(DomainEntity.PID, pid);

    doThrow(MongoException.class).when(anyCollection).update(query, update);

    try {
      storage.setPID(ProjectADomainEntity.class, DEFAULT_ID, pid);
    } finally {
      verify(db).getCollection(TypeNames.getInternalName(BaseDomainEntity.class));
    }
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

  private DBObject createProjectAGeneralTestDBObject(String id, String name, String generalTestDocValue, String projectAGeneralTestDocValue) {
    Map<String, Object> map = createDefaultMap(id);
    map.put(propertyName(TestConcreteDoc.class, "name"), "projecta");
    map.put(propertyName(BaseDomainEntity.class, "generalTestDocValue"), generalTestDocValue);
    map.put(propertyName(ProjectADomainEntity.class, "projectAGeneralTestDocValue"), projectAGeneralTestDocValue);
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

}
