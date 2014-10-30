package nl.knaw.huygens.timbuctoo.storage.file;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItemInArray;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import nl.knaw.huygens.timbuctoo.model.Login;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;

public class LoginCollectionTest extends FileCollectionTest<Login> {
  private static final String DIFFERENT_ID = "test2";
  private static final String ID = "test";
  private static final String DIFFERENT_USER_PID = "differentUserPid";
  private static final String USER_PID = "userPid";
  private static final String AUTH_STRING = ID;
  private static final String DIFFERENT_AUTH_STRING = "differentAuthString";
  private LoginCollection instance;

  @Before
  public void setup() {
    instance = new LoginCollection();
  }

  @Test
  public void addAddsTheEntityToItsCollection() {
    Login login = createLoginWithAuthStringAndUserPid(AUTH_STRING, USER_PID);
    verifyAddAddsTheEntityToItsCollection(login);
  }

  @Test
  public void addReturnsAnIdAndAddsItToTheEntity() {
    Login login = createLoginWithAuthStringAndUserPid(AUTH_STRING, USER_PID);
    String expectedId = "LOGI000000000001";

    verifyAddReturnsAnIdAndAddsItToTheEntity(login, expectedId);
  }

  @Test(expected = IllegalArgumentException.class)
  public void addThrowsAnIllegalArgumentExceptionWhenTheLoginDoesNotContainAnAuthString() {
    Login loginWithoutAuthString = new Login();
    instance.add(loginWithoutAuthString);
  }

  @Test
  public void addDoesNotAddASecondItemWithTheSameAuthStringItReturnsTheExistingId() {
    // setup
    Login firstLogin = createLoginWithAuthStringAndUserPid(AUTH_STRING, USER_PID);
    Login secondLogin = createLoginWithAuthStringAndUserPid(AUTH_STRING, DIFFERENT_USER_PID);

    // action
    String firstId = instance.add(firstLogin);
    String secondId = instance.add(secondLogin);

    // verify
    assertThat(secondId, is(firstId));
  }

  @Test
  public void addIncrementsTheId() {
    Login entity1 = createLoginWithAuthStringAndUserPid(AUTH_STRING, USER_PID);
    Login entity2 = createLoginWithAuthStringAndUserPid(DIFFERENT_AUTH_STRING, DIFFERENT_USER_PID);
    Login entity3 = createLoginWithAuthStringAndUserPid("anotherAuthString", USER_PID);
    String expectedId = "LOGI000000000003";

    verifyAddIncrementsTheId(entity1, entity2, entity3, expectedId);
  }

  @Test
  public void findReturnsTheLoginFoundByTheAuthString() {
    // setup
    Login login = createLoginWithAuthStringAndUserPid(AUTH_STRING, USER_PID);
    Login otherLogin = createLoginWithAuthStringAndUserPid(DIFFERENT_AUTH_STRING, DIFFERENT_USER_PID);

    instance.add(login);
    instance.add(otherLogin);

    Login example = createLoginWithAuthStringAndUserPid(AUTH_STRING, USER_PID);

    // action
    Login actualLogin = instance.findItem(example);

    // verify
    assertThat(actualLogin, is(login));
  }

  @Test
  public void findSearchesByUserPidIfTheAuthStringIsEmpty() {
    // setup
    Login login = createLoginWithAuthStringAndUserPid(AUTH_STRING, USER_PID);

    instance.add(login);

    Login example = new Login();
    example.setUserPid(USER_PID);

    // action
    Login foundLogin = instance.findItem(example);

    // verify
    assertThat(foundLogin, is(login));
  }

  @Test
  public void findReturnsNullIfBothTheAuthStringAndUserPidAreNull() {
    Login login = new Login();
    String userPid = USER_PID;
    login.setUserPid(userPid);
    login.setAuthString(AUTH_STRING);

    instance.add(login);

    Login example = new Login();

    // action
    Login foundLogin = instance.findItem(example);

    // verify
    assertThat(foundLogin, is(nullValue(Login.class)));
  }

  private Login createLoginWithAuthStringAndUserPid(String authString, String userPid) {
    Login login = new Login();
    login.setAuthString(authString);
    login.setUserPid(userPid);
    return login;
  }

  @Test
  public void findReturnsNullIfNoLoginInTheCollectionExistsWhithTheAuthString() {
    // setup
    Login login = createLoginWithAuthStringAndUserPid(DIFFERENT_AUTH_STRING, DIFFERENT_USER_PID);
    instance.add(login);

    Login example = new Login();
    example.setAuthString(AUTH_STRING);

    // action
    Login actualLogin = instance.findItem(example);

    // verify
    assertThat(actualLogin, is(nullValue(Login.class)));
  }

  @Test
  public void asArrayReturnsAllTheValuesRepresentedInAnArray() {
    // setup
    Login login = createLoginWithAuthStringAndUserPid(AUTH_STRING, USER_PID);
    Login otherLogin = createLoginWithAuthStringAndUserPid(DIFFERENT_AUTH_STRING, DIFFERENT_USER_PID);

    instance.add(login);
    instance.add(otherLogin);

    // action
    Login[] logins = instance.asArray();

    // verify
    assertThat(logins, hasItemInArray(login));
    assertThat(logins, hasItemInArray(otherLogin));
  }

  @Override
  protected FileCollection<Login> getInstance() {
    return instance;
  }

  @Test
  public void intializeWithLoginsAddsTheLoginsToTheCollection() {
    // setup
    Login login = createLoginWithAuthStringAndUserPid(AUTH_STRING, USER_PID);
    login.setId(ID);

    Login otherLogin = createLoginWithAuthStringAndUserPid(DIFFERENT_AUTH_STRING, DIFFERENT_USER_PID);
    otherLogin.setId(DIFFERENT_ID);

    // action
    LoginCollection loginCollection = new LoginCollection(Lists.newArrayList(login, otherLogin));

    // verify
    assertThat(loginCollection.getAll().size(), is(equalTo(2)));
  }
}
