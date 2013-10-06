package nl.knaw.huygens.timbuctoo.persistence;

import nl.knaw.huygens.persistence.PersistenceException;
import nl.knaw.huygens.persistence.PersistenceManager;

public class PersistenceWrapper {

  private PersistenceManager persistenceManager;
  private String baseUrl;

  public PersistenceWrapper(String baseUrl, PersistenceManager persistenceManager) {
    this.baseUrl = baseUrl.endsWith("/") ? baseUrl : baseUrl + "/";
    this.persistenceManager = persistenceManager;
  }

  public String persistUrl(String url) throws PersistenceException {
    return persistenceManager.persistURL(url);
  }

  public String getPersistentUrl(String persistentId) throws PersistenceException {
    return persistenceManager.getPersistentURL(persistentId);
  }

  public String persistObject(String collectionId, String objectId) throws PersistenceException {
    String url = createUrl(collectionId, objectId);
    return persistenceManager.persistURL(url);
  }

  private String createUrl(String collectionId, String id) {
    return baseUrl + "resources/" + collectionId + "/" + id;
  }
}
