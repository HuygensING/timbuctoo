package nl.knaw.huygens.repository.storage.mongo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;

import nl.knaw.huygens.repository.model.Document;
import nl.knaw.huygens.repository.storage.StorageConfiguration;

import org.junit.BeforeClass;

import com.mongodb.DB;

public class MongoStorageTestBase {

  private static final String DB_NAME = "test";
  protected static StorageConfiguration storageConfiguration;

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

  protected void verifyCollectionSize(long expectedSize, String collectionName, DB db) {
    assertEquals(expectedSize, db.getCollection(collectionName).getCount());
  }

  protected <T extends Document> void assertEqualDocs(T expected, T actual) {
    try {
      assertNull(MongoDiff.diffDocuments(expected, actual));
    } catch (IOException e) {
      fail(e.getMessage());
    }
  }

}
