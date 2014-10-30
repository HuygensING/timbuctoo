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
  private static final String AUTH_STRING = "test";
  private static final String DIFFERENT_AUTH_STRING = "differentAuthString";
  private LoginCollection instance;

  @Before
  public void setup() {
    instance = new LoginCollection();
  }

  @Test
  public void addAddsTheEntityToItsCollection() {
    Login login = createLoginWithAuthString(AUTH_STRING);
    verifyAddAddsTheEntityToItsCollection(login);
  }

  @Test
  public void addReturnsAnIdAndAddsItToTheEntity() {
    Login login = createLoginWithAuthString(AUTH_STRING);
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
    Login firstLogin = createLoginWithAuthString(AUTH_STRING);
    Login secondLogin = createLoginWithAuthString(AUTH_STRING);

    // action
    String firstId = instance.add(firstLogin);
    String secondId = instance.add(secondLogin);

    // verify
    assertThat(secondId, is(firstId));
  }

  @Test
  public void addIncrementsTheId() {
    Login entity1 = createLoginWithAuthString(AUTH_STRING);
    Login entity2 = createLoginWithAuthString(DIFFERENT_AUTH_STRING);
    Login entity3 = createLoginWithAuthString("anotherAuthString");
    String expectedId = "LOGI000000000003";

    verifyAddIncrementsTheId(entity1, entity2, entity3, expectedId);
  }

  @Test
  public void findReturnsTheLoginFoundByTheAuthString() {
    // setup
    Login login = createLoginWithAuthString(AUTH_STRING);
    Login otherLogin = createLoginWithAuthString(DIFFERENT_AUTH_STRING);

    instance.add(login);
    instance.add(otherLogin);

    Login example = createLoginWithAuthString(AUTH_STRING);

    // action
    Login actualLogin = instance.findItem(example);

    // verify
    assertThat(actualLogin, is(login));
  }

  private Login createLoginWithAuthString(String authString) {
    Login login = new Login();
    login.setAuthString(authString);
    return login;
  }

  @Test
  public void findReturnsNullIfNoLoginInTheCollectionExistsWhithTheAuthString() {
    // setup
    Login login = createLoginWithAuthString(DIFFERENT_AUTH_STRING);
    instance.add(login);

    Login example = createLoginWithAuthString(AUTH_STRING);

    // action
    Login actualLogin = instance.findItem(example);

    // verify
    assertThat(actualLogin, is(nullValue(Login.class)));
  }

  @Test
  public void asArrayReturnsAllTheValuesRepresentedInAnArray() {
    // setup
    Login login = createLoginWithAuthString(AUTH_STRING);
    Login otherLogin = createLoginWithAuthString(DIFFERENT_AUTH_STRING);

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
    Login login = createLoginWithAuthString(AUTH_STRING);
    login.setId("test");
    Login otherLogin = createLoginWithAuthString(DIFFERENT_AUTH_STRING);
    otherLogin.setId("test2");

    // action
    LoginCollection loginCollection = new LoginCollection(Lists.newArrayList(login, otherLogin));

    // verify
    assertThat(loginCollection.getAll().size(), is(equalTo(2)));
  }
}
