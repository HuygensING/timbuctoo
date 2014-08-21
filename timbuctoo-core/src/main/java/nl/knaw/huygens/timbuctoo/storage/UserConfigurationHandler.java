package nl.knaw.huygens.timbuctoo.storage;

import nl.knaw.huygens.timbuctoo.config.Configuration;
import nl.knaw.huygens.timbuctoo.model.User;
import nl.knaw.huygens.timbuctoo.model.VREAuthorization;

import com.google.inject.Inject;

public class UserConfigurationHandler {

  private final JsonFileHandler jsonFileHandler;
  private final Configuration configuration;

  @Inject
  public UserConfigurationHandler(JsonFileHandler jsonFileHandler, Configuration configuration) {
    this.jsonFileHandler = jsonFileHandler;
    this.configuration = configuration;
  }

  public String addVREAuthorization(VREAuthorization authorization) {
    // TODO Auto-generated method stub
    return null;
  }

  public void addUser(User user) {
    // TODO Auto-generated method stub

  }

  public VREAuthorization findVREAuthorization(VREAuthorization example) {
    // TODO Auto-generated method stub
    return null;
  }

  public User findUser(User user) {
    // TODO Auto-generated method stub
    return null;
  }

  public User getUser(String id) {
    // TODO Auto-generated method stub
    return null;
  }

  public StorageIterator<User> getUsers() {
    // TODO Auto-generated method stub
    return null;
  }

  public void updateUser(User user) throws StorageException {
    // TODO Auto-generated method stub

  }

  public void updateVREAuthorization(VREAuthorization authorization) throws StorageException {
    // TODO Auto-generated method stub

  }

  public void deleteUser(User user) {
    // TODO Auto-generated method stub

  }

  public void deleteVREAuthorization(VREAuthorization any) {
    // TODO Auto-generated method stub

  }

}
