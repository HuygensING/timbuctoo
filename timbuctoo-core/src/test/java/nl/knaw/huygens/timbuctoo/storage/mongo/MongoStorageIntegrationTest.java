package nl.knaw.huygens.timbuctoo.storage.mongo;

import nl.knaw.huygens.timbuctoo.storage.DBIntegrationTestHelper;
import nl.knaw.huygens.timbuctoo.storage.StorageIntegrationTest;

public class MongoStorageIntegrationTest extends StorageIntegrationTest {

  @Override
  protected DBIntegrationTestHelper createDBIntegrationTestHelper() {
    return new MongoDBIntegrationTestHelper();
  }

}
