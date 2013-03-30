package nl.knaw.huygens.repository.persistence;

public interface PersistenceManager {

  /**
   * @param objectId
   * @param collectionId
   * @return
   * @throws PersistenceException
   */
  String persistObject(String objectId, String collectionId) throws PersistenceException;

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
