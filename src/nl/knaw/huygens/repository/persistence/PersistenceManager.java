package nl.knaw.huygens.repository.persistence;


public interface PersistenceManager {
  String persistURL(String urlToPersist) throws PersistenceException;
  
  String persistObject(String objectId, String collectionId) throws PersistenceException;

  String getPersistentURL(String persistenceID) throws PersistenceException;
}
