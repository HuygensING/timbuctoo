package nl.knaw.huygens.repository.persistence;

import java.util.UUID;

/**
 * This PersistenceManager implementation simply creates a unique id.
 * However, this id is not registered anywhere...
 */
public class DefaultPersistenceManager implements PersistenceManager {

  @Override
  public String persistObject(String collectionId, String objectId) throws PersistenceException {
    return createId();
  }

  @Override
  public String persistURL(String url) throws PersistenceException {
    return createId();
  }

  @Override
  public String getPersistentURL(String persistentId) throws PersistenceException {
    return "URL";
  }

  protected String createId() {
    return UUID.randomUUID().toString();
  }

}
