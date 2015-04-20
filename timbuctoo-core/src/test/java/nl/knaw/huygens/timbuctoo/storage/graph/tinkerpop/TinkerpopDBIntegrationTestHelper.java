package nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop;

import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.model.ModelException;
import nl.knaw.huygens.timbuctoo.storage.DBIntegrationTestHelper;
import nl.knaw.huygens.timbuctoo.storage.Storage;
import nl.knaw.huygens.timbuctoo.storage.graph.GraphLegacyStorageWrapper;

public class TinkerpopDBIntegrationTestHelper implements DBIntegrationTestHelper {

  @Override
  public void startCleanDB() throws Exception {}

  @Override
  public void stopDB() {}

  @Override
  public Storage createStorage(TypeRegistry typeRegistry) throws ModelException {
    return new GraphLegacyStorageWrapper(new TinkerpopStorage());
  }

}
