package nl.knaw.huygens.timbuctoo.storage.mongo;

/*
 * #%L
 * Timbuctoo core
 * =======
 * Copyright (C) 2012 - 2015 Huygens ING
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
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;

import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.model.ModelException;
import nl.knaw.huygens.timbuctoo.model.util.Change;
import nl.knaw.huygens.timbuctoo.storage.EntityInducer;
import nl.knaw.huygens.timbuctoo.storage.EntityReducer;
import nl.knaw.huygens.timbuctoo.storage.NoSuchEntityException;
import nl.knaw.huygens.timbuctoo.storage.Properties;
import nl.knaw.huygens.timbuctoo.storage.StorageException;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mongojack.internal.stream.JacksonDBObject;

import test.variation.model.BaseVariationDomainEntity;
import test.variation.model.TestSystemEntity;
import test.variation.model.projecta.ProjectADomainEntity;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Maps;
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
  public static void setupTypeRegistry() throws ModelException {
    registry = TypeRegistry.getInstance().init("timbuctoo.model timbuctoo.variation.model.*");
  }

  @AfterClass
  public static void clearRegistry() {
    registry = null;
  }

  @Before
  public void setup() throws Exception {
    mongoDB = mock(MongoDB.class);
    anyCollection = mock(DBCollection.class);
    entityIds = mock(EntityIds.class);
    Properties properties = new MongoProperties();
    EntityInducer inducer = new EntityInducer(properties);
    EntityReducer reducer = new EntityReducer(properties, registry);
    storage = new MongoStorage(mongoDB, entityIds, properties, inducer, reducer);

    mapper = new ObjectMapper();

    when(mongoDB.getCollection(anyString())).thenReturn(anyCollection);
  }

  // -------------------------------------------------------------------

  @Test
  public void testAddSystemEntity() throws Exception {
    String generatedId = "X" + DEFAULT_ID;
    TestSystemEntity entity = new TestSystemEntity(DEFAULT_ID);

    when(entityIds.getNextId(TestSystemEntity.class)).thenReturn(generatedId);

    assertEquals(generatedId, storage.addSystemEntity(TestSystemEntity.class, entity));

    verify(entityIds).getNextId(TestSystemEntity.class);
    verify(mongoDB).insert(any(DBCollection.class), any(String.class), any(DBObject.class));
  }

  @Test
  public void testAddDomainEntity() throws Exception {
    String generatedId = "X" + DEFAULT_ID;
    ProjectADomainEntity entity = new ProjectADomainEntity(DEFAULT_ID);

    when(entityIds.getNextId(ProjectADomainEntity.class)).thenReturn(generatedId);

    assertEquals(generatedId, storage.addDomainEntity(ProjectADomainEntity.class, entity, new Change()));
    verify(entityIds).getNextId(ProjectADomainEntity.class);
    verify(mongoDB).insert(any(DBCollection.class), any(String.class), any(DBObject.class));
  }

  @Test(expected = StorageException.class)
  public void testUpdateSystemEntityNonExistent() throws Exception {
    TestSystemEntity entity = new TestSystemEntity(DEFAULT_ID);

    storage.updateSystemEntity(TestSystemEntity.class, entity);
  }

  @Test
  public void testUpdateDomainEntity() throws Exception {
    // setup
    ProjectADomainEntity entity = new ProjectADomainEntity(DEFAULT_ID);
    JsonNode jsonNode = mapper.createObjectNode();
    DBObject dbObject = new JacksonDBObject<JsonNode>(jsonNode, JsonNode.class);

    when(mongoDB.exist(any(DBCollection.class), any(DBObject.class))).thenReturn(true);
    when(anyCollection.findOne(any(DBObject.class))).thenReturn(dbObject);

    // action
    storage.updateDomainEntity(ProjectADomainEntity.class, entity, new Change());

    // verify
    verify(mongoDB).update(any(DBCollection.class), any(DBObject.class), any(DBObject.class));
  }

  @Test(expected = NoSuchEntityException.class)
  public void testUpdateDomainEntityForMissingEntity() throws Exception {
    ProjectADomainEntity entity = new ProjectADomainEntity(DEFAULT_ID);

    storage.updateDomainEntity(ProjectADomainEntity.class, entity, new Change());
  }

  @Test(expected = StorageException.class)
  public void testDeleteItemNonExistent() throws Exception {
    storage.deleteDomainEntity(BaseVariationDomainEntity.class, DEFAULT_ID, null);
  }

  @Test
  public void testSetPID() throws Exception {
    String pid = "3c08c345-c80d-44e2-a377-029259b662b9";

    JsonNode jsonNode = mapper.createObjectNode();
    DBObject dbObject = new JacksonDBObject<JsonNode>(jsonNode, JsonNode.class);
    when(anyCollection.findOne(any(DBObject.class))).thenReturn(dbObject);

    storage.setPID(ProjectADomainEntity.class, DEFAULT_ID, pid);

    verify(mongoDB, times(2)).update(any(DBCollection.class), any(DBObject.class), any(DBObject.class));
  }

  @Test(expected = IllegalStateException.class)
  public void testSetPIDObjectAllreadyHasAPID() throws Exception {
    String pid = "3c08c345-c80d-44e2-a377-029259b662b9";

    DBObject dbObject = createDomainEnityJsonNode(pid);
    when(anyCollection.findOne(any(DBObject.class))).thenReturn(dbObject);

    try {
      storage.setPID(ProjectADomainEntity.class, DEFAULT_ID, pid);
    } finally {
      verify(mongoDB, never()).update(any(DBCollection.class), any(DBObject.class), any(DBObject.class));
    }
  }

  private DBJsonNode createDomainEnityJsonNode(String pid) {
    Map<String, Object> map = Maps.newHashMap();
    map.put("_id", DEFAULT_ID);
    map.put("^pid", pid);

    return createDBJsonNode(map);
  }

  /**
   * Creates a JsonNode from a map. This is used is several tests.
   */
  private DBJsonNode createDBJsonNode(Map<String, Object> map) {
    ObjectMapper mapper = new ObjectMapper();
    return new DBJsonNode(mapper.valueToTree(map));
  }

}
