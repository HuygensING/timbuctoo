package nl.knaw.huygens.repository.persistence;


public class PersistenceManagerFactory {

  public static PersistenceManager newPersistenceManager(boolean handleEnabled, String publicUrl, String cipher, String namingAuthority, String prefix, String pathToPrivateKey) {
    if (handleEnabled) {
      return HandleManager.newHandleManager(publicUrl, cipher, namingAuthority, prefix, pathToPrivateKey);
    } else {
      return new DefaultPersistenceManager();
    }
  }

}
