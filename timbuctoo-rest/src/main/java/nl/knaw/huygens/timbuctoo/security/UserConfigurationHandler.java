package nl.knaw.huygens.timbuctoo.security;

import nl.knaw.huygens.timbuctoo.model.User;
import nl.knaw.huygens.timbuctoo.model.VREAuthorization;
import nl.knaw.huygens.timbuctoo.storage.FileCollection;
import nl.knaw.huygens.timbuctoo.storage.JsonFileHandler;
import nl.knaw.huygens.timbuctoo.storage.StorageException;
import nl.knaw.huygens.timbuctoo.storage.StorageIterator;
import nl.knaw.huygens.timbuctoo.storage.StorageIteratorStub;
import nl.knaw.huygens.timbuctoo.storage.UserFileCollection;
import nl.knaw.huygens.timbuctoo.storage.VREAuthorizationFileCollection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

public class UserConfigurationHandler {

  private static final Logger LOG = LoggerFactory.getLogger(UserConfigurationHandler.class);

  static final String USER_FILE_NAME = "users.json";
  private final JsonFileHandler jsonFileHandler;

  @Inject
  public UserConfigurationHandler(JsonFileHandler jsonFileHandler) {
    this.jsonFileHandler = jsonFileHandler;
  }

  private String createVREFileName(VREAuthorization authorization) {
    return String.format("%s.json", authorization.getVreId());
  }

  public synchronized void addUser(User user) throws StorageException {
    FileCollection<User> users = getUserCollection();
    users.add(user);
    jsonFileHandler.saveCollection(users, USER_FILE_NAME);
  }

  /**
   * Searches for a user.
   * @param user the example the user should match to
   * @return the user, null if the user is not found, an empty user if some exception with the storage occurred 
   */
  public synchronized User findUser(User user) {
    try {
      return getUserCollection().findItem(user);
    } catch (StorageException e) {
      LOG.error("Error finding user", e);
    }

    return new User();
  }

  /**
   * Get the user for id.
   * @param id the id of the user to get
   * @return the user, null if the user is not found, an empty user if some exception with the storage occurred 
   */
  public synchronized User getUser(String id) {
    try {
      return getUserCollection().get(id);
    } catch (StorageException e) {
      LOG.error("Error getting user", e);
    }

    return new User();
  }

  /**
   * Get the known users.
   * @return all the know users.
   */
  public synchronized StorageIterator<User> getUsers() {
    try {
      return getUserCollection().getAll();
    } catch (StorageException e) {
      LOG.error("Error getting all the users", e);
    }
    return StorageIteratorStub.newInstance();
  }

  public synchronized void updateUser(User user) throws StorageException {
    FileCollection<User> userCollection = getUserCollection();
    userCollection.updateItem(user);
    jsonFileHandler.saveCollection(userCollection, USER_FILE_NAME);
  }

  public synchronized void deleteUser(User user) throws StorageException {
    FileCollection<User> userCollection = getUserCollection();
    userCollection.deleteItem(user);
    jsonFileHandler.saveCollection(userCollection, USER_FILE_NAME);
  }

  private FileCollection<User> getUserCollection() throws StorageException {
    UserFileCollection users = jsonFileHandler.getCollection(UserFileCollection.class, USER_FILE_NAME);
    return users != null ? users : new UserFileCollection();
  }

  // VREAuthorization methods

  public synchronized String addVREAuthorization(VREAuthorization authorization) throws StorageException {
    String vreFileName = createVREFileName(authorization);
    VREAuthorizationFileCollection authorizations = getVREAuthorizationCollection(vreFileName);
    String id = authorizations.add(authorization);
    jsonFileHandler.saveCollection(authorizations, vreFileName);
    return id;
  }

  private VREAuthorizationFileCollection getVREAuthorizationCollection(String vreFileName) throws StorageException {
    VREAuthorizationFileCollection authorizations = jsonFileHandler.getCollection(VREAuthorizationFileCollection.class, vreFileName);
    return authorizations != null ? authorizations : new VREAuthorizationFileCollection();
  }

  public synchronized VREAuthorization findVREAuthorization(VREAuthorization example) {
    try {
      return getVREAuthorizationCollection(createVREFileName(example)).findItem(example);
    } catch (StorageException e) {
      LOG.error("Error finding vre authorization", e);
    }
    return new VREAuthorization();
  }

  public synchronized void updateVREAuthorization(VREAuthorization authorization) throws StorageException {
    String fileName = createVREFileName(authorization);
    VREAuthorizationFileCollection collection = getVREAuthorizationCollection(fileName);
    collection.updateItem(authorization);
    jsonFileHandler.saveCollection(collection, fileName);
  }

  public synchronized void deleteVREAuthorization(VREAuthorization authorization) throws StorageException {
    String fileName = createVREFileName(authorization);
    VREAuthorizationFileCollection collection = getVREAuthorizationCollection(fileName);
    collection.deleteItem(authorization);
    jsonFileHandler.saveCollection(collection, fileName);

  }

}
