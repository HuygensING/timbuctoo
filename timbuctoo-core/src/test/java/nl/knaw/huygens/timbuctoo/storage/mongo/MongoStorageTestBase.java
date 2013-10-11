package nl.knaw.huygens.timbuctoo.storage.mongo;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.UnknownHostException;

import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.storage.StorageConfiguration;

import org.junit.Before;
import org.junit.BeforeClass;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.MongoException;
import com.mongodb.MongoOptions;

public abstract class MongoStorageTestBase {

  protected static final String DB_NAME = "test";
  protected static StorageConfiguration storageConfiguration;
  protected DB db;
  protected Mongo mongo;
  protected DBCollection anyCollection;
  protected EntityIds entityIds;
  protected MongoOptions mongoOptions;

  public MongoStorageTestBase() {
    super();
  }

  @BeforeClass
  public static void setUpStorageConfiguration() {
    storageConfiguration = mock(StorageConfiguration.class);
    when(storageConfiguration.getDbName()).thenReturn(DB_NAME);
    when(storageConfiguration.getHost()).thenReturn("127.0.0.1");
    when(storageConfiguration.getPort()).thenReturn(27017);
    when(storageConfiguration.getUser()).thenReturn("test");
    when(storageConfiguration.getPassword()).thenReturn("test");
  }

  @Before
  public void setUp() throws UnknownHostException, MongoException {
    anyCollection = mock(DBCollection.class);
    entityIds = mock(EntityIds.class);
    db = mock(DB.class);
    when(db.getCollection(anyString())).thenReturn(anyCollection);
    mongo = mock(Mongo.class);
    setupStorage();
  }

  protected abstract void setupStorage() throws UnknownHostException, MongoException;

  protected <T extends Entity> void assertEqualDocs(T expected, T actual) {
    try {
      assertNull(MongoDiff.diffDocuments(expected, actual));
    } catch (IOException e) {
      fail(e.getMessage());
    }
  }

  protected DBCursor createDBCursorWithOneValue(DBObject dbObject) {
    DBCursor cursor = mock(DBCursor.class);
    when(cursor.hasNext()).thenReturn(true, false);
    when(cursor.next()).thenReturn(dbObject);
    when(cursor.count()).thenReturn(1);
    return cursor;
  }

  protected DBCursor createCursorWithoutValues() {
    DBCursor cursor = mock(DBCursor.class);
    when(cursor.hasNext()).thenReturn(false);
    when(cursor.count()).thenReturn(0);
    return cursor;
  }
}
