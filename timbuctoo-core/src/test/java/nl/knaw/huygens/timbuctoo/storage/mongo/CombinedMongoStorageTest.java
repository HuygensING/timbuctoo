package nl.knaw.huygens.timbuctoo.storage.mongo;

import static nl.knaw.huygens.timbuctoo.storage.FieldMapper.propertyName;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.model.util.Change;
import nl.knaw.huygens.timbuctoo.variation.model.BaseDomainEntity;
import nl.knaw.huygens.timbuctoo.variation.model.TestConcreteDoc;
import nl.knaw.huygens.timbuctoo.variation.model.TestSystemEntity;
import nl.knaw.huygens.timbuctoo.variation.model.projecta.ProjectADomainEntity;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mongojack.internal.stream.JacksonDBObject;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;

public class CombinedMongoStorageTest {

  private static final String DEFAULT_ID = "TEST000000000007";

  private static TypeRegistry registry;

  private MongoDB mongoDB;
  private DBCollection anyCollection;
  private EntityIds entityIds;
  private MongoStorage storage;
  private ObjectMapper mapper;

  @BeforeClass
  public static void setupTypeRegistry() {
    registry = new TypeRegistry("timbuctoo.model timbuctoo.variation.model timbuctoo.variation.model.projecta timbuctoo.variation.model.projectb");
  }

  @Before
  public void setup() throws Exception {
    mongoDB = mock(MongoDB.class);
    anyCollection = mock(DBCollection.class);
    entityIds = mock(EntityIds.class);
    storage = new MongoStorage(registry, mongoDB, entityIds);

    mapper = new ObjectMapper();

    when(mongoDB.getCollection(anyString())).thenReturn(anyCollection);
  }

  private Map<String, Object> createDefaultMap(String id) {
    Map<String, Object> map = Maps.newHashMap();
    map.put("_id", id);
    map.put("^rev", 0);
    map.put(DomainEntity.DELETED, false);
    return map;
  }

  private DBObject createTestConcreteDocDBObject(String id, String name) {
    Map<String, Object> map = createDefaultMap(id);
    map.put(propertyName(TestConcreteDoc.class, "name"), name);
    return new DBJsonNode(mapper.valueToTree(map));
  }

  // -------------------------------------------------------------------

  @Test
  public void testAddSystemEntity() throws IOException {
    String generatedId = "X" + DEFAULT_ID;
    TestSystemEntity entity = new TestSystemEntity(DEFAULT_ID);

    when(entityIds.getNextId(TestSystemEntity.class)).thenReturn(generatedId);

    assertEquals(generatedId, storage.addSystemEntity(TestSystemEntity.class, entity));

    verify(entityIds).getNextId(TestSystemEntity.class);
    verify(mongoDB).insert(any(DBCollection.class), any(String.class), any(DBObject.class));
  }

  @Test
  public void testAddDomainEntity() throws IOException {
    String generatedId = "X" + DEFAULT_ID;
    ProjectADomainEntity entity = new ProjectADomainEntity(DEFAULT_ID);

    when(entityIds.getNextId(ProjectADomainEntity.class)).thenReturn(generatedId);

    assertEquals(generatedId, storage.addDomainEntity(ProjectADomainEntity.class, entity, new Change()));
    verify(entityIds).getNextId(ProjectADomainEntity.class);
    verify(mongoDB).insert(any(DBCollection.class), any(String.class), any(DBObject.class));
  }

  @Test(expected = IOException.class)
  public void testUpdateSystemEntityNonExistent() throws IOException {
    TestSystemEntity entity = new TestSystemEntity(DEFAULT_ID);

    storage.updateSystemEntity(TestSystemEntity.class, entity);
  }

  @Test
  public void testUpdateDomainEntity() throws IOException {
    ProjectADomainEntity entity = new ProjectADomainEntity(DEFAULT_ID);

    JsonNode jsonNode = mapper.createObjectNode();
    DBObject dbObject = new JacksonDBObject<JsonNode>(jsonNode, JsonNode.class);
    when(anyCollection.findOne(any(DBObject.class))).thenReturn(dbObject);

    storage.updateDomainEntity(ProjectADomainEntity.class, entity, new Change());

    verify(mongoDB).update(any(DBCollection.class), any(DBObject.class), any(DBObject.class));
  }

  @Test(expected = IOException.class)
  public void testUpdateDomainEntityForMissingEntity() throws IOException {
    ProjectADomainEntity entity = new ProjectADomainEntity(DEFAULT_ID);

    storage.updateDomainEntity(ProjectADomainEntity.class, entity, new Change());
  }

  @Test
  public void testDeleteDomainEntity() throws IOException {
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

    Change change = new Change("test", "test");
    storage.deleteDomainEntity(BaseDomainEntity.class, DEFAULT_ID, change);

    verify(mongoDB).update(any(DBCollection.class), any(DBObject.class), any(DBObject.class));
  }

  @Test(expected = IOException.class)
  public void testDeleteItemNonExistent() throws IOException {
    storage.deleteDomainEntity(BaseDomainEntity.class, DEFAULT_ID, null);
  }

  @Test
  public void testSetPID() throws IOException {
    String pid = "3c08c345-c80d-44e2-a377-029259b662b9";

    JsonNode jsonNode = mapper.createObjectNode();
    DBObject dbObject = new JacksonDBObject<JsonNode>(jsonNode, JsonNode.class);
    when(anyCollection.findOne(any(DBObject.class))).thenReturn(dbObject);

    storage.setPID(ProjectADomainEntity.class, DEFAULT_ID, pid);

    verify(mongoDB, times(2)).update(any(DBCollection.class), any(DBObject.class), any(DBObject.class));
  }

  @Test
  public void testGetAllByIds() {
    List<String> ids = Lists.newArrayList("TEST0000000001", "TEST0000000002", "TEST0000000003");
    storage.getAllByIds(ProjectADomainEntity.class, ids);

    DBObject query = new BasicDBObject(Entity.ID, new BasicDBObject("$in", ids));

    verify(mongoDB).find(anyCollection, query);
  }

}
