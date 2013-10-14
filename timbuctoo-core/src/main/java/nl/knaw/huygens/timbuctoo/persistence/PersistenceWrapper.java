package nl.knaw.huygens.timbuctoo.persistence;

import nl.knaw.huygens.persistence.PersistenceException;
import nl.knaw.huygens.persistence.PersistenceManager;

import com.google.common.base.CharMatcher;
import com.google.common.base.Joiner;

public class PersistenceWrapper {

  private final PersistenceManager manager;
  private final String baseUrl;

  public PersistenceWrapper(String baseUrl, PersistenceManager persistenceManager) {
    this.baseUrl = CharMatcher.is('/').trimTrailingFrom(baseUrl);
    this.manager = persistenceManager;
  }

  public String persistUrl(String url) throws PersistenceException {
    return manager.persistURL(url);
  }

  public String getPersistentUrl(String persistentId) throws PersistenceException {
    return manager.getPersistentURL(persistentId);
  }

  public String persistObject(String collection, String objectId) throws PersistenceException {
    String url = createUrl(collection, objectId);
    return manager.persistURL(url);
  }

  private String createUrl(String collection, String id) {
    // FIXME implicit dependence on rest module
    return Joiner.on('/').join(baseUrl, "resources", collection, id);
  }

}
