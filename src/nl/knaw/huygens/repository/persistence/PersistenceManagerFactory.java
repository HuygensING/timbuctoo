package nl.knaw.huygens.repository.persistence;

import nl.knaw.huygens.repository.config.Configuration;

public class PersistenceManagerFactory {

  public static PersistenceManager newPersistenceManager(Configuration config) {
    if (config.getBooleanSetting("handle.enabled", true)) {
      return HandleManager.newHandleManager(config);
    } else {
      return new DefaultPersistenceManager();
    }
  }

}
