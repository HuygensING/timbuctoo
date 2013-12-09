package nl.knaw.huygens.timbuctoo.storage.mongo;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;

import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.variation.model.TestSystemEntity;
import nl.knaw.huygens.timbuctoo.variation.model.projecta.ProjectADomainEntity;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.Mongo;

public class CombinedMongoStorageTest {

  private static final String DEFAULT_ID = "TEST000000000007";

  private static TypeRegistry registry;

  private Mongo mongo;
  private DB db;
  private DBCollection anyCollection;
  private EntityIds entityIds;
  private MongoStorage storage;
  private MongoQueries queries;

  @BeforeClass
  public static void setupTypeRegistry() {
    registry = new TypeRegistry("timbuctoo.model timbuctoo.variation.model timbuctoo.variation.model.projecta timbuctoo.variation.model.projectb");
  }

  @Before
  public void setup() throws Exception {
    mongo = mock(Mongo.class);
    db = mock(DB.class);
    anyCollection = mock(DBCollection.class);
    entityIds = mock(EntityIds.class);
    storage = new MongoStorage(registry, mongo, db, entityIds);

    queries = new MongoQueries();

    when(db.getCollection(anyString())).thenReturn(anyCollection);
  }

  // -------------------------------------------------------------------

  @Test
  public void testAddSystemEntity() throws IOException {
    String generatedId = "X" + DEFAULT_ID;
    TestSystemEntity entity = new TestSystemEntity(DEFAULT_ID, "test");

    when(entityIds.getNextId(TestSystemEntity.class)).thenReturn(generatedId);
    DBObject query = queries.selectById(generatedId);
    when(anyCollection.findOne(query)).thenReturn(new BasicDBObject());

    assertEquals(generatedId, storage.addSystemEntity(TestSystemEntity.class, entity));
    verify(entityIds).getNextId(TestSystemEntity.class);
    verify(anyCollection).insert(any(DBObject.class));
  }

  @Test
  public void testAddDomainEntity() throws IOException {
    String generatedId = "X" + DEFAULT_ID;
    ProjectADomainEntity entity = new ProjectADomainEntity(DEFAULT_ID, "test");

    when(entityIds.getNextId(ProjectADomainEntity.class)).thenReturn(generatedId);
    DBObject query = queries.selectById(generatedId);
    when(anyCollection.findOne(query)).thenReturn(new BasicDBObject());

    assertEquals(generatedId, storage.addDomainEntity(ProjectADomainEntity.class, entity));
    verify(entityIds).getNextId(ProjectADomainEntity.class);
    // Two additions: in regular and in version collection.
    verify(anyCollection, times(2)).insert(any(DBObject.class));
  }

}
