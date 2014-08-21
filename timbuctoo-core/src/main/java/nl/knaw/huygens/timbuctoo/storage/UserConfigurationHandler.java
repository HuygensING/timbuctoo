package nl.knaw.huygens.timbuctoo.storage;

import nl.knaw.huygens.timbuctoo.model.SystemEntity;

public class UserConfigurationHandler {

  public <T extends SystemEntity> String addSystemEntity(Class<T> type, T instance) {
    return null;
  }

  public <T extends SystemEntity> T findEntity(Class<T> type, T example) {
    // TODO Auto-generated method stub
    return null;
  }

  public <T extends SystemEntity> T getEntity(Class<T> type, String id) {
    // TODO Auto-generated method stub
    return null;
  }

  public <T extends SystemEntity> StorageIterator<T> getSystemEntities(Class<T> type) {
    // TODO Auto-generated method stub
    return null;
  }

  public <T extends SystemEntity> void updateSystemEntity(Class<T> type, T instance) throws StorageException {
    // TODO Auto-generated method stub

  }

  public <T extends SystemEntity> void deleteSystemEntity(T authorization) {
    // TODO Auto-generated method stub

  }

}
