package nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop;

import nl.knaw.huygens.timbuctoo.storage.DBIntegrationTestHelper;
import nl.knaw.huygens.timbuctoo.storage.StorageIntegrationTest;
import org.junit.Ignore;
import org.junit.Test;

public class TinkerPopLegacyStorageWrapperIntegrationTest extends StorageIntegrationTest {

  @Override
  protected DBIntegrationTestHelper createDBIntegrationTestHelper() {
    return new TinkerPopDBIntegrationTestHelper();
  }

  @Test
  @Ignore
  @Override
  public void setPIDDoesNotAlterAnyRelationsOfTheEntity() throws Exception {
    super.setPIDDoesNotAlterAnyRelationsOfTheEntity();
  }
}
