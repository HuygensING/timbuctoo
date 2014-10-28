package nl.knaw.huygens.timbuctoo.storage.file;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItemInArray;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import nl.knaw.huygens.timbuctoo.model.Login;
import nl.knaw.huygens.timbuctoo.storage.file.LoginCollection;

import org.junit.Test;

import com.google.common.collect.Lists;

public class LoginCollectionTest {
  private static final String AUTH_STRING = "test";
  private static final String DIFFERENT_AUTH_STRING = "differentAuthString";

  @Test
  public void findReturnsTheLoginFoundByTheAuthString() {
    // setup
    Login login = createLoginWithAuthString(AUTH_STRING);
    Login otherLogin = createLoginWithAuthString(DIFFERENT_AUTH_STRING);

    LoginCollection instance = new LoginCollection(Lists.newArrayList(login, otherLogin));

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
    LoginCollection instance = new LoginCollection(Lists.newArrayList(login));

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

    LoginCollection instance = new LoginCollection(Lists.newArrayList(login, otherLogin));

    // action
    Login[] logins = instance.asArray();

    // verify
    assertThat(logins, hasItemInArray(login));
    assertThat(logins, hasItemInArray(otherLogin));
  }
}
