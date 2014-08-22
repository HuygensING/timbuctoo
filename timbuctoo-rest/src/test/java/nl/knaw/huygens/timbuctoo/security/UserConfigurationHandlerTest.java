package nl.knaw.huygens.timbuctoo.security;

import static nl.knaw.huygens.timbuctoo.security.UserConfigurationHandler.USER_FILE_NAME;
import static nl.knaw.huygens.timbuctoo.security.UserRoles.UNVERIFIED_USER_ROLE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import nl.knaw.huygens.timbuctoo.model.User;
import nl.knaw.huygens.timbuctoo.model.VREAuthorization;
import nl.knaw.huygens.timbuctoo.storage.JsonFileHandler;
import nl.knaw.huygens.timbuctoo.storage.StorageException;
import nl.knaw.huygens.timbuctoo.storage.StorageIterator;
import nl.knaw.huygens.timbuctoo.storage.StorageIteratorStub;
import nl.knaw.huygens.timbuctoo.storage.UserFileCollection;
import nl.knaw.huygens.timbuctoo.storage.VREAuthorizationFileCollection;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class UserConfigurationHandlerTest {

  private static final Class<VREAuthorizationFileCollection> VRE_AUTH_COLL_TYPE = VREAuthorizationFileCollection.class;
  private static final Class<UserFileCollection> USER_COLLECTION_TYPE = UserFileCollection.class;
  private static final String VRE_FILE_NAME = "vreId.json";
  private static final String VRE_ID = "vreId";
  private UserConfigurationHandler instance;
  private JsonFileHandler jsonFileHandler;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    jsonFileHandler = mock(JsonFileHandler.class);
    instance = new UserConfigurationHandler(jsonFileHandler);
  }

  @Test
  public void addUserShouldGiveUserAnIdAndAddItToTheUserCollection() {
    // setup
    Class<UserFileCollection> type = USER_COLLECTION_TYPE;
    User user = new User();

    UserFileCollection users = setUpUserCollection();

    // action
    instance.addUser(user);

    // verify
    InOrder inOrder = Mockito.inOrder(jsonFileHandler, users);
    inOrder.verify(jsonFileHandler).getCollection(type, USER_FILE_NAME);
    inOrder.verify(users).add(user);
    inOrder.verify(jsonFileHandler).saveCollection(type, users, USER_FILE_NAME);
  }

  @Test
  public void testFindUser() {
    // setup
    User user = new User();
    Class<UserFileCollection> type = USER_COLLECTION_TYPE;

    UserFileCollection users = setUpUserCollection();

    when(users.findItem(user)).thenReturn(user);

    // action
    User foundUser = instance.findUser(user);

    // verify
    assertThat(foundUser, is(notNullValue(User.class)));
    verify(jsonFileHandler).getCollection(type, USER_FILE_NAME);
    verify(users).findItem(user);
  }

  @Test
  public void getUser() {
    UserFileCollection users = setUpUserCollection();
    String userId = "userId";
    when(users.get(userId)).thenReturn(new User());

    // action
    User foundUser = instance.getUser(userId);

    // verify
    assertThat(foundUser, is(notNullValue(User.class)));
    verify(jsonFileHandler).getCollection(USER_COLLECTION_TYPE, USER_FILE_NAME);
    verify(users).get(userId);
  }

  @Test
  public void getUsers() {
    // setup
    StorageIterator<User> storageIterator = StorageIteratorStub.newInstance();
    UserFileCollection users = setUpUserCollection();

    when(users.getAll()).thenReturn(storageIterator);
    // action
    StorageIterator<User> returnedIterator = instance.getUsers();

    // verify
    assertThat(returnedIterator, is(equalTo(storageIterator)));
    verify(users).getAll();
  }

  @Test
  public void updateUser() throws StorageException {
    // setup
    User user = new User();
    UserFileCollection users = setUpUserCollection();

    // action
    instance.updateUser(user);

    // verify
    verify(users).updateItem(user);
    verify(jsonFileHandler).saveCollection(USER_COLLECTION_TYPE, users, USER_FILE_NAME);
  }

  @Test
  public void deleteUser() throws StorageException {
    // setup
    User user = new User();
    UserFileCollection users = setUpUserCollection();

    // action
    instance.deleteUser(user);

    // verify
    verify(users).deleteItem(user);
    verify(jsonFileHandler).saveCollection(USER_COLLECTION_TYPE, users, USER_FILE_NAME);
  }

  private UserFileCollection setUpUserCollection() {
    UserFileCollection users = mock(USER_COLLECTION_TYPE);
    when(jsonFileHandler.getCollection(USER_COLLECTION_TYPE, USER_FILE_NAME)).thenReturn(users);
    return users;
  }

  // VREAuthorization tests

  @Test
  public void testAddVREAuthorization() {
    // setup
    VREAuthorizationFileCollection vreAuthorizations = setUpVREAuthorizationCollection();
    VREAuthorization authorization = createVREAuthorizationFor(VRE_ID);

    // action
    instance.addVREAuthorization(authorization);

    // verify
    InOrder inOrder = Mockito.inOrder(jsonFileHandler, vreAuthorizations);
    inOrder.verify(jsonFileHandler).getCollection(VRE_AUTH_COLL_TYPE, VRE_FILE_NAME);
    inOrder.verify(vreAuthorizations).add(authorization);
    inOrder.verify(jsonFileHandler).saveCollection(VRE_AUTH_COLL_TYPE, vreAuthorizations, VRE_FILE_NAME);
  }

  @Test
  public void testFindVREAuthorization() {
    // setup
    VREAuthorization vreAuthorization = createVREAuthorizationFor(VRE_ID);
    VREAuthorizationFileCollection vreAuthorizations = setUpVREAuthorizationCollection();
    when(vreAuthorizations.findItem(vreAuthorization)).thenReturn(vreAuthorization);

    // action
    VREAuthorization foundAuthorization = instance.findVREAuthorization(vreAuthorization);

    // verify
    assertThat(foundAuthorization, is(notNullValue(VREAuthorization.class)));
    verify(jsonFileHandler).getCollection(VRE_AUTH_COLL_TYPE, VRE_FILE_NAME);
    verify(vreAuthorizations).findItem(vreAuthorization);
  }

  @Test
  public void testUpdateVREAuthroization() throws StorageException {
    // setup
    VREAuthorization vreAuthorization = createVREAuthorizationFor(VRE_ID);
    VREAuthorizationFileCollection vreAuthorizations = setUpVREAuthorizationCollection();

    // action
    instance.updateVREAuthorization(vreAuthorization);

    // verify
    verify(vreAuthorizations).updateItem(vreAuthorization);
    verify(jsonFileHandler).saveCollection(VRE_AUTH_COLL_TYPE, vreAuthorizations, VRE_FILE_NAME);
  }

  @Test
  public void testDeleteVREAuthroization() throws StorageException {
    // setup
    VREAuthorization vreAuthorization = createVREAuthorizationFor(VRE_ID);
    VREAuthorizationFileCollection vreAuthorizations = setUpVREAuthorizationCollection();

    // action
    instance.deleteVREAuthorization(vreAuthorization);

    // verify
    verify(vreAuthorizations).deleteItem(vreAuthorization);
    verify(jsonFileHandler).saveCollection(VRE_AUTH_COLL_TYPE, vreAuthorizations, VRE_FILE_NAME);
  }

  private VREAuthorizationFileCollection setUpVREAuthorizationCollection() {
    VREAuthorizationFileCollection vreAuthorizations = mock(VRE_AUTH_COLL_TYPE);
    when(jsonFileHandler.getCollection(VRE_AUTH_COLL_TYPE, VRE_FILE_NAME)).thenReturn(vreAuthorizations);
    return vreAuthorizations;
  }

  private VREAuthorization createVREAuthorizationFor(String vreId) {
    String userId = "userId";
    VREAuthorization authorization = new VREAuthorization(vreId, userId, UNVERIFIED_USER_ROLE);
    return authorization;
  }
}
