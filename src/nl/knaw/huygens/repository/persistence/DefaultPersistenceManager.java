package nl.knaw.huygens.repository.persistence;

import java.util.UUID;

public class DefaultPersistenceManager implements PersistenceManager {

  @Override
  public String persistObject(String objectId, String collectionId) throws PersistenceException {
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

  private String createId() {
    return UUID.randomUUID().toString();
  }

}
