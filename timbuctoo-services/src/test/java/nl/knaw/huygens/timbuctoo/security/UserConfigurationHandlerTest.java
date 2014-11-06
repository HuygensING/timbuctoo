package nl.knaw.huygens.timbuctoo.security;

/*
 * #%L
 * Timbuctoo REST api
 * =======
 * Copyright (C) 2012 - 2014 Huygens ING
 * =======
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import static nl.knaw.huygens.timbuctoo.security.UserConfigurationHandler.USER_FILE_NAME;
import static nl.knaw.huygens.timbuctoo.security.UserRoles.UNVERIFIED_USER_ROLE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import nl.knaw.huygens.timbuctoo.model.SystemEntity;
import nl.knaw.huygens.timbuctoo.model.User;
import nl.knaw.huygens.timbuctoo.model.VREAuthorization;
import nl.knaw.huygens.timbuctoo.storage.StorageException;
import nl.knaw.huygens.timbuctoo.storage.StorageIterator;
import nl.knaw.huygens.timbuctoo.storage.StorageIteratorStub;
import nl.knaw.huygens.timbuctoo.storage.file.FileCollection;
import nl.knaw.huygens.timbuctoo.storage.file.JsonFileHandler;
import nl.knaw.huygens.timbuctoo.storage.file.UserFileCollection;
import nl.knaw.huygens.timbuctoo.storage.file.VREAuthorizationFileCollection;

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
  public void addUserShouldGiveUserAnIdAndAddItToTheUserCollection() throws StorageException {
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
    inOrder.verify(jsonFileHandler).saveCollection(users, USER_FILE_NAME);
  }

  @Test(expected = StorageException.class)
  public void addUserThrowsAStorageExceptionWhenGettingTheCollectionThrowsOne() throws StorageException {
    // setup
    User user = new User();
    setUpGetCollectionThrowsAnException(USER_COLLECTION_TYPE, USER_FILE_NAME);

    // action
    instance.addUser(user);
  }

  @Test(expected = StorageException.class)
  public void addUserThrowsAStorageExceptionWhenSaveCollectionThrowsOne() throws StorageException {
    // setup
    User user = new User();
    UserFileCollection users = setUpUserCollection();

    setUpSaveCollectionThrowsAnException(users, USER_FILE_NAME);

    // action
    instance.addUser(user);

    // verify
    verify(users).updateItem(user);
    verify(jsonFileHandler).saveCollection(users, USER_FILE_NAME);
  }

  @Test
  public void testFindUser() throws StorageException {
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
  public void findUserReturnsANewUserWhenJsonFileHandlerThrowsAStoragetException() throws StorageException {
    // setup
    User user = new User();
    doThrow(StorageException.class).when(jsonFileHandler).getCollection(USER_COLLECTION_TYPE, USER_FILE_NAME);

    // action
    User foundUser = instance.findUser(user);

    // verify
    assertThat(foundUser, is(notNullValue(User.class)));
    verify(jsonFileHandler).getCollection(USER_COLLECTION_TYPE, USER_FILE_NAME);
  }

  @Test
  public void getUser() throws StorageException {
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
  public void getUserReturnsANewUserWhenJsonFileHandlerThrowsAStoragetException() throws StorageException {
    setUpGetCollectionThrowsAnException(USER_COLLECTION_TYPE, USER_FILE_NAME);
    String userId = "userId";

    // action
    User foundUser = instance.getUser(userId);

    // verify
    assertThat(foundUser, is(notNullValue(User.class)));
    verify(jsonFileHandler).getCollection(USER_COLLECTION_TYPE, USER_FILE_NAME);
  }

  private void setUpGetCollectionThrowsAnException(Class<? extends FileCollection<? extends SystemEntity>> type, String fileName) throws StorageException {
    doThrow(StorageException.class).when(jsonFileHandler).getCollection(type, fileName);
  }

  @Test
  public void getUsers() throws StorageException {
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
  public void getUsersReturnsAnEmptyStorageIteratorWhenJsonFileHandlerThrowsAnException() throws StorageException {
    // setup
    setUpGetCollectionThrowsAnException(USER_COLLECTION_TYPE, USER_FILE_NAME);

    // action
    StorageIterator<User> returnedIterator = instance.getUsers();

    // verify
    assertThat(returnedIterator, is(notNullValue()));
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
    verify(jsonFileHandler).saveCollection(users, USER_FILE_NAME);
  }

  @Test(expected = StorageException.class)
  public void updateUserThrowsAStorageExceptionWhenGettingTheCollectionThrowsOne() throws StorageException {
    // setup
    User user = new User();
    setUpGetCollectionThrowsAnException(USER_COLLECTION_TYPE, USER_FILE_NAME);

    // action
    instance.updateUser(user);
  }

  @Test(expected = StorageException.class)
  public void updateUserThrowsAStorageExceptionWhenSaveCollectionThrowsOne() throws StorageException {
    // setup
    User user = new User();
    UserFileCollection users = setUpUserCollection();

    setUpSaveCollectionThrowsAnException(users, USER_FILE_NAME);

    // action
    instance.updateUser(user);

    // verify
    verify(users).updateItem(user);
    verify(jsonFileHandler).saveCollection(users, USER_FILE_NAME);
  }

  private <T extends FileCollection<? extends SystemEntity>> void setUpSaveCollectionThrowsAnException(T collection, String fileName) throws StorageException {
    doThrow(StorageException.class).when(jsonFileHandler).saveCollection(collection, fileName);

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
    verify(jsonFileHandler).saveCollection(users, USER_FILE_NAME);
  }

  @Test(expected = StorageException.class)
  public void deleteUserThrowsAStorageExceptionWhenGettingTheCollectionThrowsOne() throws StorageException {
    // setup
    User user = new User();
    setUpGetCollectionThrowsAnException(USER_COLLECTION_TYPE, USER_FILE_NAME);

    // action
    instance.deleteUser(user);
  }

  @Test(expected = StorageException.class)
  public void deleteUserThrowsAStorageExceptionWhenSaveCollectionThrowsOne() throws StorageException {
    // setup
    User user = new User();
    UserFileCollection users = setUpUserCollection();

    setUpSaveCollectionThrowsAnException(users, USER_FILE_NAME);

    // action
    instance.deleteUser(user);

    // verify
    verify(users).updateItem(user);
    verify(jsonFileHandler).saveCollection(users, USER_FILE_NAME);
  }

  private UserFileCollection setUpUserCollection() throws StorageException {
    UserFileCollection users = mock(USER_COLLECTION_TYPE);
    when(jsonFileHandler.getCollection(USER_COLLECTION_TYPE, USER_FILE_NAME)).thenReturn(users);
    return users;
  }

  // VREAuthorization tests

  @Test
  public void testAddVREAuthorization() throws StorageException {
    // setup
    VREAuthorizationFileCollection vreAuthorizations = setUpVREAuthorizationCollection();
    VREAuthorization authorization = createVREAuthorizationFor(VRE_ID);

    // action
    instance.addVREAuthorization(authorization);

    // verify
    InOrder inOrder = Mockito.inOrder(jsonFileHandler, vreAuthorizations);
    inOrder.verify(jsonFileHandler).getCollection(VRE_AUTH_COLL_TYPE, VRE_FILE_NAME);
    inOrder.verify(vreAuthorizations).add(authorization);
    inOrder.verify(jsonFileHandler).saveCollection(vreAuthorizations, VRE_FILE_NAME);
  }

  @Test(expected = StorageException.class)
  public void addVREAuthorizationAStorageExceptionWhenGettingTheCollectionThrowsOne() throws StorageException {
    // setup
    VREAuthorization authorization = createVREAuthorizationFor(VRE_ID);
    setUpGetCollectionThrowsAnException(VRE_AUTH_COLL_TYPE, VRE_FILE_NAME);

    // action
    instance.addVREAuthorization(authorization);

    // verify
    verify(jsonFileHandler).getCollection(VRE_AUTH_COLL_TYPE, VRE_FILE_NAME);
  }

  @Test(expected = StorageException.class)
  public void addVREAuthorizationAStorageExceptionWhenSavingTheCollectionThrowsOne() throws StorageException {
    // setup
    VREAuthorizationFileCollection vreAuthorizations = setUpVREAuthorizationCollection();
    VREAuthorization authorization = createVREAuthorizationFor(VRE_ID);
    setUpSaveCollectionThrowsAnException(vreAuthorizations, VRE_FILE_NAME);

    // action
    instance.addVREAuthorization(authorization);

    // verify
    InOrder inOrder = Mockito.inOrder(jsonFileHandler, vreAuthorizations);
    inOrder.verify(jsonFileHandler).getCollection(VRE_AUTH_COLL_TYPE, VRE_FILE_NAME);
    inOrder.verify(vreAuthorizations).add(authorization);
    inOrder.verify(jsonFileHandler).saveCollection(vreAuthorizations, VRE_FILE_NAME);
  }

  @Test
  public void testFindVREAuthorization() throws StorageException {
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
  public void findVREAuthorizationReturnsANewVREAuthorizationWhenGettingOfTheCollectionThrowsAStorageException() throws StorageException {
    // setup
    VREAuthorization vreAuthorization = createVREAuthorizationFor(VRE_ID);

    setUpGetCollectionThrowsAnException(VRE_AUTH_COLL_TYPE, VRE_FILE_NAME);

    // action
    VREAuthorization foundAuthorization = instance.findVREAuthorization(vreAuthorization);

    // verify
    assertThat(foundAuthorization, is(notNullValue(VREAuthorization.class)));
    verify(jsonFileHandler).getCollection(VRE_AUTH_COLL_TYPE, VRE_FILE_NAME);
  }

  @Test
  public void testUpdateVREAuthrozation() throws StorageException {
    // setup
    VREAuthorization vreAuthorization = createVREAuthorizationFor(VRE_ID);
    VREAuthorizationFileCollection vreAuthorizations = setUpVREAuthorizationCollection();

    // action
    instance.updateVREAuthorization(vreAuthorization);

    // verify
    verify(vreAuthorizations).updateItem(vreAuthorization);
    verify(jsonFileHandler).saveCollection(vreAuthorizations, VRE_FILE_NAME);
  }

  @Test(expected = StorageException.class)
  public void updateVREAuthorizationAStorageExceptionWhenGettingTheCollectionThrowsOne() throws StorageException {
    // setup
    VREAuthorization authorization = createVREAuthorizationFor(VRE_ID);
    setUpGetCollectionThrowsAnException(VRE_AUTH_COLL_TYPE, VRE_FILE_NAME);

    // action
    instance.updateVREAuthorization(authorization);

    // verify
    verify(jsonFileHandler).getCollection(VRE_AUTH_COLL_TYPE, VRE_FILE_NAME);
  }

  @Test(expected = StorageException.class)
  public void updateVREAuthorizationAStorageExceptionWhenSavingTheCollectionThrowsOne() throws StorageException {
    // setup
    VREAuthorizationFileCollection vreAuthorizations = setUpVREAuthorizationCollection();
    VREAuthorization authorization = createVREAuthorizationFor(VRE_ID);
    setUpSaveCollectionThrowsAnException(vreAuthorizations, VRE_FILE_NAME);

    // action
    instance.updateVREAuthorization(authorization);

    // verify
    InOrder inOrder = Mockito.inOrder(jsonFileHandler, vreAuthorizations);
    inOrder.verify(jsonFileHandler).getCollection(VRE_AUTH_COLL_TYPE, VRE_FILE_NAME);
    inOrder.verify(vreAuthorizations).updateItem(authorization);
    inOrder.verify(jsonFileHandler).saveCollection(vreAuthorizations, VRE_FILE_NAME);
  }

  @Test
  public void testDeleteVREAuthrozation() throws StorageException {
    // setup
    VREAuthorization vreAuthorization = createVREAuthorizationFor(VRE_ID);
    VREAuthorizationFileCollection vreAuthorizations = setUpVREAuthorizationCollection();

    // action
    instance.deleteVREAuthorization(vreAuthorization);

    // verify
    verify(vreAuthorizations).deleteItem(vreAuthorization);
    verify(jsonFileHandler).saveCollection(vreAuthorizations, VRE_FILE_NAME);
  }

  @Test(expected = StorageException.class)
  public void deleteVREAuthorizationAStorageExceptionWhenGettingTheCollectionThrowsOne() throws StorageException {
    // setup
    VREAuthorization authorization = createVREAuthorizationFor(VRE_ID);
    setUpGetCollectionThrowsAnException(VRE_AUTH_COLL_TYPE, VRE_FILE_NAME);

    // action
    instance.deleteVREAuthorization(authorization);

    // verify
    verify(jsonFileHandler).getCollection(VRE_AUTH_COLL_TYPE, VRE_FILE_NAME);
  }

  @Test(expected = StorageException.class)
  public void deleteVREAuthorizationAStorageExceptionWhenSavingTheCollectionThrowsOne() throws StorageException {
    // setup
    VREAuthorizationFileCollection vreAuthorizations = setUpVREAuthorizationCollection();
    VREAuthorization authorization = createVREAuthorizationFor(VRE_ID);
    setUpSaveCollectionThrowsAnException(vreAuthorizations, VRE_FILE_NAME);

    // action
    instance.addVREAuthorization(authorization);

    // verify
    InOrder inOrder = Mockito.inOrder(jsonFileHandler, vreAuthorizations);
    inOrder.verify(jsonFileHandler).getCollection(VRE_AUTH_COLL_TYPE, VRE_FILE_NAME);
    inOrder.verify(vreAuthorizations).deleteItem(authorization);
    inOrder.verify(jsonFileHandler).saveCollection(vreAuthorizations, VRE_FILE_NAME);
  }

  private VREAuthorizationFileCollection setUpVREAuthorizationCollection() throws StorageException {
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
