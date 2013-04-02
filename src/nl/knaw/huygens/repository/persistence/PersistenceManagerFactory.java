package nl.knaw.huygens.repository.persistence;

import nl.knaw.huygens.repository.persistence.handle.HandleManager;
import nl.knaw.huygens.repository.util.Configuration;

public class PersistenceManagerFactory {

  private final Configuration config;

  public PersistenceManagerFactory(Configuration config) {
    this.config = config;
  }

  public PersistenceManager newPersistenceManager() {
    if (config.getBooleanSetting("use-handle-system", true)) {
      return HandleManager.newHandleManager(config);
    } else {
      return new DefaultPersistenceManager();
    }
  }

}
