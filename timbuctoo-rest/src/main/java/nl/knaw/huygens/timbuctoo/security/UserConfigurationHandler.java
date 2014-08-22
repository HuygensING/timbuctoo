package nl.knaw.huygens.timbuctoo.security;

import nl.knaw.huygens.timbuctoo.model.User;
import nl.knaw.huygens.timbuctoo.model.VREAuthorization;
import nl.knaw.huygens.timbuctoo.storage.JsonFileHandler;
import nl.knaw.huygens.timbuctoo.storage.StorageException;
import nl.knaw.huygens.timbuctoo.storage.StorageIterator;
import nl.knaw.huygens.timbuctoo.storage.UserFileCollection;
import nl.knaw.huygens.timbuctoo.storage.VREAuthorizationFileCollection;

import com.google.inject.Inject;

public class UserConfigurationHandler {

  static final String USER_FILE_NAME = "users.json";
  private final JsonFileHandler jsonFileHandler;

  @Inject
  public UserConfigurationHandler(JsonFileHandler jsonFileHandler) {
    this.jsonFileHandler = jsonFileHandler;
  }

  private String createVREFileName(VREAuthorization authorization) {
    return String.format("%s.json", authorization.getVreId());
  }

  public synchronized void addUser(User user) {
    UserFileCollection users = getUserCollection();
    users.add(user);
    jsonFileHandler.saveCollection(UserFileCollection.class, users, USER_FILE_NAME);
  }

  public synchronized User findUser(User user) {
    return getUserCollection().findItem(user);
  }

  public synchronized User getUser(String id) {
    return getUserCollection().get(id);
  }

  public synchronized StorageIterator<User> getUsers() {
    return getUserCollection().getAll();
  }

  public synchronized void updateUser(User user) throws StorageException {
    UserFileCollection userCollection = getUserCollection();
    userCollection.updateItem(user);
    jsonFileHandler.saveCollection(UserFileCollection.class, userCollection, USER_FILE_NAME);
  }

  public synchronized void deleteUser(User user) {
    UserFileCollection userCollection = getUserCollection();
    userCollection.deleteItem(user);
    jsonFileHandler.saveCollection(UserFileCollection.class, userCollection, USER_FILE_NAME);
  }

  private UserFileCollection getUserCollection() {
    UserFileCollection users = jsonFileHandler.getCollection(UserFileCollection.class, USER_FILE_NAME);
    return users;
  }

  // VREAuthorization methods

  public synchronized String addVREAuthorization(VREAuthorization authorization) {
    String vreFileName = createVREFileName(authorization);
    VREAuthorizationFileCollection authorizations = getVREAuthorizationCollection(vreFileName);
    String id = authorizations.add(authorization);
    jsonFileHandler.saveCollection(VREAuthorizationFileCollection.class, authorizations, vreFileName);
    return id;
  }

  private VREAuthorizationFileCollection getVREAuthorizationCollection(String vreFileName) {
    VREAuthorizationFileCollection authorizations = jsonFileHandler.getCollection(VREAuthorizationFileCollection.class, vreFileName);
    return authorizations;
  }

  public synchronized VREAuthorization findVREAuthorization(VREAuthorization example) {
    return getVREAuthorizationCollection(createVREFileName(example)).findItem(example);
  }

  public synchronized void updateVREAuthorization(VREAuthorization authorization) throws StorageException {
    String fileName = createVREFileName(authorization);
    VREAuthorizationFileCollection collection = getVREAuthorizationCollection(fileName);
    collection.updateItem(authorization);
    jsonFileHandler.saveCollection(VREAuthorizationFileCollection.class, collection, fileName);
  }

  public synchronized void deleteVREAuthorization(VREAuthorization authorization) {
    String fileName = createVREFileName(authorization);
    VREAuthorizationFileCollection collection = getVREAuthorizationCollection(fileName);
    collection.deleteItem(authorization);
    jsonFileHandler.saveCollection(VREAuthorizationFileCollection.class, collection, fileName);

  }

}
