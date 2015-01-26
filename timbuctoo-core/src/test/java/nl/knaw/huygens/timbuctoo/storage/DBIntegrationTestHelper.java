package nl.knaw.huygens.timbuctoo.storage;

import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.model.ModelException;

public interface DBIntegrationTestHelper {

  public abstract void startCleanDB() throws Exception;

  public abstract void stopDB();

  public abstract Storage createStorage(TypeRegistry typeRegistry) throws ModelException;

}