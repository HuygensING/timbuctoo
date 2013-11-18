package nl.knaw.huygens.timbuctoo.storage.mongo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.UnknownHostException;

import nl.knaw.huygens.timbuctoo.model.Entity;

import org.junit.Before;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.MongoException;

public abstract class MongoStorageTestBase {

  protected Mongo mongo;
  protected DB db;
  protected DBCollection anyCollection;
  protected EntityIds entityIds;
  protected MongoQueries queries;

  @Before
  public void setUp() throws UnknownHostException, MongoException {
    mongo = mock(Mongo.class);
    db = mock(DB.class);
    anyCollection = mock(DBCollection.class);
    entityIds = mock(EntityIds.class);
    queries = new MongoQueries();

    when(db.getCollection(anyString())).thenReturn(anyCollection);

    setupStorage();
  }

  protected abstract void setupStorage() throws UnknownHostException, MongoException;

  protected <T extends Entity> void assertEqualDocs(T expected, T actual) {
    try {
      // Use assertEquals instead of assertNull for a clearer message when a test fails.
      assertEquals(null, MongoDiff.diffDocuments(expected, actual));
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
