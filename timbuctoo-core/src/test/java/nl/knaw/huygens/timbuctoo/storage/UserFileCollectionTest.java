package nl.knaw.huygens.timbuctoo.storage;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import nl.knaw.huygens.timbuctoo.model.User;

import org.junit.Before;
import org.junit.Test;

public class UserFileCollectionTest {
  private static final String PERSISTENT_ID = "persistentId";
  private UserFileCollection instance;

  @Before
  public void setUp() {
    instance = new UserFileCollection();
  }

  @Test
  public void addUserShouldGiveUserAndReturnTheId() {
    User user = createUserWithPersistentId();

    String expectedId = "USER000000000001";

    String actualId = instance.add(user);

    assertThat(actualId, is(equalTo(expectedId)));
    assertThat(user.getId(), is(equalTo(expectedId)));
  }

  @Test
  public void addUserShouldCreateAnIdHigherThanTheHighest() {
    User user1 = createUserWithPersistentId();
    User user2 = createUserWithPersistentId();
    User user3 = createUserWithPersistentId();

    String expectedId = "USER000000000003";

    instance.add(user1);
    instance.add(user2);
    String actualId = instance.add(user3);

    assertThat(actualId, is(equalTo(expectedId)));
  }

  private User createUserWithPersistentId() {
    User user = new User();
    user.setPersistentId(PERSISTENT_ID);
    return user;
  }

  @Test
  public void addUserShouldAddTheUserToItsInnerCollection() {
    User user = createUserWithPersistentId();

    String id = instance.add(user);
    User foundUser = instance.get(id);

    assertThat(foundUser, is(equalTo(user)));
  }

  @Test
  public void theAddedUserCannotBeFoundIfItDidNotContainAPersistentIdButCantBeGet() {
    User user = new User();

    String id = instance.add(user);

    assertThat(instance.findItem(user), is(nullValue(User.class)));
    assertThat(instance.get(id), is(equalTo(user)));
  }

  @Test
  public void findUserShouldSearchTheUserByPersistentId() {
    User user = createUserWithPersistentId();

    instance.add(user);
    User foundUser = instance.findItem(user);

    assertThat(foundUser, is(equalTo(user)));
  }

  @Test
  public void findUserReturnsNullIfTheUserParametersHasNoPersistentId() {
    User user = createUserWithPersistentId();
    User userToFind = new User();

    instance.add(user);
    User foundUser = instance.findItem(userToFind);

    assertThat(foundUser, is(nullValue(User.class)));
  }

  @Test
  public void findUserReturnsNullIfTheUserParametersIsNull() {
    User user = createUserWithPersistentId();
    User userToFind = null;

    instance.add(user);
    User foundUser = instance.findItem(userToFind);

    assertThat(foundUser, is(nullValue(User.class)));
  }

  @Test
  public void getAllShouldReturnAStorageIteratorWithAllTheKnownUsers() {
    User user1 = createUserWithPersistentId();
    User user2 = createUserWithPersistentId();
    User user3 = createUserWithPersistentId();

    instance.add(user1);
    instance.add(user2);
    instance.add(user3);

    // action
    StorageIterator<User> users = instance.getAll();

    // verify
    assertThat(users, is(notNullValue()));
    assertThat(users.getAll(), containsInAnyOrder(user1, user2, user3));
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
    User user1 = createUserWithPersistentId();
    String id = instance.add(user1);

    User user2 = createUserWithPersistentId();
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
    User user1 = createUserWithPersistentId();
    String id = instance.add(user1);

    User user2 = createUserWithPersistentId();
    user2.setCommonName("test");

    // action
    instance.updateItem(user2);

    // verify
    assertThat(instance.get(id), is(sameInstance(user1)));
  }

  @Test
  public void deleteUserSearchesTheUserByIdAndRemovesItFromTheCollection() {
    // setup
    User user1 = createUserWithPersistentId();
    String id = instance.add(user1);

    User user2 = createUserWithPersistentId();
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
    User user1 = createUserWithPersistentId();
    String id = instance.add(user1);

    User user2 = createUserWithPersistentId();
    user2.setCommonName("test");

    // action
    instance.deleteItem(user2);

    // verify
    assertThat(instance.get(id), is(sameInstance(user1)));
  }
}
