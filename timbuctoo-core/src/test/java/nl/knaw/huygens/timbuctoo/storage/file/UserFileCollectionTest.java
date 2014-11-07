package nl.knaw.huygens.timbuctoo.storage.file;

/*
 * #%L
 * Timbuctoo core
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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;

import java.util.List;

import nl.knaw.huygens.timbuctoo.model.User;
import nl.knaw.huygens.timbuctoo.storage.StorageIterator;
import nl.knaw.huygens.timbuctoo.storage.file.FileCollection;
import nl.knaw.huygens.timbuctoo.storage.file.UserFileCollection;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;

public class UserFileCollectionTest extends FileCollectionTest<User> {

  private static final String PERSISTENT_ID = "persistentId";
  private static final String PERSISTENT_ID2 = "pid2";
  private static final String PERSISTENT_ID3 = "pid3";

  private UserFileCollection instance;

  @Before
  public void setUp() {
    instance = new UserFileCollection();
  }

  @Test
  public void addUserShouldGiveUserAndReturnTheId() {
    User user = createUserWithPersistentId(PERSISTENT_ID);
    String expectedId = "USER000000000001";

    verifyAddReturnsAnIdAndAddsItToTheEntity(user, expectedId);
  }

  @Test
  public void addUserIncrementsTheId() {
    User user1 = createUserWithPersistentId(PERSISTENT_ID);
    User user2 = createUserWithPersistentId(PERSISTENT_ID2);
    User user3 = createUserWithPersistentId(PERSISTENT_ID3);

    String expectedId = "USER000000000003";

    verifyAddIncrementsTheId(user1, user2, user3, expectedId);
  }

  private User createUserWithPersistentId(String pid) {
    User user = new User();
    user.setPersistentId(pid);
    return user;
  }

  @Test
  public void addAddsTheUserToItsInnerCollection() {
    User user = createUserWithPersistentId(PERSISTENT_ID);

    verifyAddAddsTheEntityToItsCollection(user);
  }

  @Test
  public void addUserWithTheSamePersitentIdTwiceReturnsTheSecondTimeJustTheId() {
    // setup
    User user1 = createUserWithPersistentId(PERSISTENT_ID);
    User user2 = createUserWithPersistentId(PERSISTENT_ID);

    // action
    String firstId = instance.add(user1);
    String secondId = instance.add(user2);

    // verify
    assertThat(secondId, is(equalTo(firstId)));
    assertThat(instance.get(firstId), is(sameInstance(user1)));
  }

  @Test
  public void addedUserCannotBeFoundIfItDidNotContainAPersistentIdButCantBeGet() {
    User user = new User();

    String id = instance.add(user);

    assertThat(instance.findItem(user), is(nullValue(User.class)));
    assertThat(instance.get(id), is(equalTo(user)));
  }

  @Test
  public void findUserShouldSearchTheUserByPersistentId() {
    User user = createUserWithPersistentId(PERSISTENT_ID);

    instance.add(user);
    User foundUser = instance.findItem(user);

    assertThat(foundUser, is(equalTo(user)));
  }

  @Test
  public void findUserReturnsNullIfTheUserParametersHasNoPersistentId() {
    User user = createUserWithPersistentId(PERSISTENT_ID);
    User userToFind = new User();

    instance.add(user);
    User foundUser = instance.findItem(userToFind);

    assertThat(foundUser, is(nullValue(User.class)));
  }

  @Test
  public void findUserReturnsNullIfTheUserParametersIsNull() {
    User user = createUserWithPersistentId(PERSISTENT_ID);
    User userToFind = null;

    instance.add(user);
    User foundUser = instance.findItem(userToFind);

    assertThat(foundUser, is(nullValue(User.class)));
  }

  @Test
  public void findUserReturnsNullIfTheUserIsNotKnown() {
    User user = createUserWithPersistentId(PERSISTENT_ID);

    User foundUser = instance.findItem(user);

    assertThat(foundUser, is(nullValue(User.class)));
  }

  @Test
  public void getReturnsNullIfTheIdInsertedIsNull() {
    User user = createUserWithPersistentId(PERSISTENT_ID);

    instance.add(user);
    User foundUser = instance.get(null);

    assertThat(foundUser, is(nullValue(User.class)));
  }

  @Test
  public void getAllShouldReturnAStorageIteratorWithAllTheKnownUsers() {
    // setup
    User user1 = createUserWithPersistentId(PERSISTENT_ID);
    User user2 = createUserWithPersistentId(PERSISTENT_ID2);
    User user3 = createUserWithPersistentId(PERSISTENT_ID3);

    addUsersToInstance(user1, user2, user3);

    verifyGetAllReturnsAllTheKnownEntities(containsInAnyOrder(user1, user2, user3));
  }

  private void addUsersToInstance(User... users) {
    for (User user : users) {
      instance.add(user);
    }
  }

  @Test
  public void getAlldReturnsAnEmptyStorageIteratorWhenNoUsersAreKnown() {

    // action
    StorageIterator<User> users = instance.getAll();

    // verify
    assertThat(users, is(notNullValue()));
    assertThat(users.getAll(), is(empty()));
  }

  @Test
  public void updateUserSearchesTheUserByIdAndReplacesTheUserInTheCollection() {
    // setup
    User user1 = createUserWithPersistentId(PERSISTENT_ID);
    String id = instance.add(user1);

    User user2 = createUserWithPersistentId(PERSISTENT_ID);
    user2.setId(id);
    user2.setCommonName("test");

    // action
    instance.updateItem(user2);

    // verify
    assertThat(instance.get(id), is(sameInstance(user2)));
  }

  @Test
  public void updateUserDoesNothingIfTheUpdateUserHasNoId() {
    // setup
    User user1 = createUserWithPersistentId(PERSISTENT_ID);
    String id = instance.add(user1);

    User user2 = createUserWithPersistentId(PERSISTENT_ID);
    user2.setCommonName("test");

    // action
    instance.updateItem(user2);

    // verify
    assertThat(instance.get(id), is(sameInstance(user1)));
  }

  @Test
  public void deleteUserSearchesTheUserByIdAndRemovesItFromTheCollection() {
    // setup
    User user1 = createUserWithPersistentId(PERSISTENT_ID);
    String id = instance.add(user1);

    User user2 = createUserWithPersistentId(PERSISTENT_ID);
    user2.setId(id);
    user2.setCommonName("test");

    // action
    instance.deleteItem(user2);

    // verify
    assertThat(instance.get(id), is(nullValue(User.class)));
  }

  @Test
  public void deleteUserDoesNothingIfTheUpdateUserHasNoId() {
    // setup
    User user1 = createUserWithPersistentId(PERSISTENT_ID);
    String id = instance.add(user1);

    User user2 = createUserWithPersistentId(PERSISTENT_ID);
    user2.setCommonName("test");

    // action
    instance.deleteItem(user2);

    // verify
    assertThat(instance.get(id), is(sameInstance(user1)));
  }

  @Test
  public void intializeWithAListOfUsersMakesItPossibleToRetrieveThem() {
    // setup
    User user1 = createUserWithPersistentId(PERSISTENT_ID);
    String userId = "test";
    user1.setId(userId);
    List<User> users = Lists.newArrayList(user1);

    // action
    UserFileCollection collections = new UserFileCollection(users);

    // verify
    assertThat(collections.get(userId), sameInstance(user1));
    assertThat(collections.findItem(user1), sameInstance(user1));
  }

  @Test
  public void asArrayContainsAllTheItemsOfTheFileCollection() {
    // setup
    User user1 = createUserWithPersistentId(PERSISTENT_ID);
    User user2 = createUserWithPersistentId(PERSISTENT_ID2);
    User user3 = createUserWithPersistentId(PERSISTENT_ID3);

    addUsersToInstance(user1, user2, user3);

    // action
    User[] users = instance.asArray();

    assertThat(users.length, is(equalTo(3)));
  }

  @Override
  protected FileCollection<User> getInstance() {
    return instance;
  }

}
