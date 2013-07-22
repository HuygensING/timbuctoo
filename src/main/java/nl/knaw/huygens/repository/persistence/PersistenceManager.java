package nl.knaw.huygens.repository.persistence;

public interface PersistenceManager {

  /**
   * @param collectionId
   * @param objectId
   * @return
   * @throws PersistenceException
   */
  String persistObject(String collectionId, String objectId) throws PersistenceException;

  /**
   * @param url
   * @return
   * @throws PersistenceException
   */
  String persistURL(String url) throws PersistenceException;

  /**
   * @param persistentId
   * @return
   * @throws PersistenceException
   */
  String getPersistentURL(String persistentId) throws PersistenceException;

}
