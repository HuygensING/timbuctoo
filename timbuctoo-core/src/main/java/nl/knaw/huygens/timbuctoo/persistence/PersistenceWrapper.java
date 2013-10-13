package nl.knaw.huygens.timbuctoo.persistence;

import nl.knaw.huygens.persistence.PersistenceException;
import nl.knaw.huygens.persistence.PersistenceManager;

import org.apache.commons.lang.StringUtils;

public class PersistenceWrapper {

  private final PersistenceManager manager;
  private final String baseUrl;

  public PersistenceWrapper(String baseUrl, PersistenceManager persistenceManager) {
    this.baseUrl = StringUtils.chomp(baseUrl, "/");
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
    // FIX implicit dependence on rest module
    return String.format("%s/%s/%s/%s", baseUrl, "resources", collection, id);
  }

}
