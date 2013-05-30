package nl.knaw.huygens.repository.storage.mongo;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import nl.knaw.huygens.repository.storage.generic.StorageConfiguration;

import org.junit.BeforeClass;

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

}