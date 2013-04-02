package nl.knaw.huygens.repository.persistence;

import nl.knaw.huygens.repository.persistence.handle.HandleManager;
import nl.knaw.huygens.repository.util.Configuration;

public class TheFactory {

  private final Configuration config;

  public TheFactory(Configuration config) {
    this.config = config;
  }

  public PersistenceManager createPersistenceManager() {
    if (config.getBooleanSetting("use-handle-system", true)) {
      return HandleManager.newHandleManager(config);
    } else {
      return new DefaultPersistenceManager();
    }
  }

}
