package nl.knaw.huygens.timbuctoo.security;

import static nl.knaw.huygens.timbuctoo.security.LoginMatcher.loginWithUserName;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.security.NoSuchAlgorithmException;

import nl.knaw.huygens.security.client.UnauthorizedException;
import nl.knaw.huygens.timbuctoo.model.Login;
import nl.knaw.huygens.timbuctoo.storage.StorageException;
import nl.knaw.huygens.timbuctoo.storage.file.JsonFileHandler;
import nl.knaw.huygens.timbuctoo.storage.file.LoginCollection;

import org.apache.xml.security.utils.Base64;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class LocalAuthenticatorTest {

  private static final String AUTH_STRING = Base64.encode("userName:password".getBytes());
  private static final String USER_NAME = "userName";
  private static final String USER_PID = "userPid";
  private static String password = "password";
  private static String otherPassword = "otherPassword";
  private static final byte[] SALT = "salt".getBytes();
  private LoginCollection loginCollectionMock;
  private LocalAuthenticator instance;
  private JsonFileHandler jsonFileHandlerMock;
  private static PasswordEncrypter passwordEncrypter;

  @BeforeClass
  public static void generatePasswords() throws NoSuchAlgorithmException {
    passwordEncrypter = new PasswordEncrypter();
    password = passwordEncrypter.encryptPassword("password", SALT);
    otherPassword = passwordEncrypter.encryptPassword("otherPassword", SALT);
  }

  @Before
  public void setUp() {
    loginCollectionMock = mock(LoginCollection.class);
    jsonFileHandlerMock = mock(JsonFileHandler.class);
    instance = new LocalAuthenticator(jsonFileHandlerMock, passwordEncrypter);
  }

  @Test
  public void authenticateReturnsTheUserPidWhenTheUserNameAndPasswordExist() throws Exception {
    // setup
    Login login = createLogin(USER_PID, USER_NAME, password, SALT);

    // when
    when(jsonFileHandlerMock.getCollection(LoginCollection.class, LoginCollection.LOGIN_COLLECTION_FILE_NAME))//
        .thenReturn(loginCollectionMock);
    when(loginCollectionMock.findItem(argThat(loginWithUserName(USER_NAME))))//
        .thenReturn(login);

    // action
    String actualUserPid = instance.authenticate(AUTH_STRING);

    // verify
    assertThat(actualUserPid, is(equalTo(USER_PID)));
  }

  private Login createLogin(String userPid, String userName, String password, byte[] salt) {
    Login login = new Login();
    login.setUserPid(userPid);
    login.setUserName(userName);
    login.setPassword(password);
    login.setSalt(salt);

    return login;
  }

  @Test(expected = UnauthorizedException.class)
  public void authenticateThrowsAnUnauthorizedExceptionWhenTheAuthenticationStringIsEmpty() throws Exception {
    instance.authenticate("");
  }

  @Test(expected = UnauthorizedException.class)
  public void authenticateThrowsAnUnauthorizedExceptionWhenThePasswordIsNotEqual() throws Exception {
    // setup
    Login value = createLogin(USER_PID, USER_NAME, otherPassword, SALT);

    // when
    when(jsonFileHandlerMock.getCollection(LoginCollection.class, LoginCollection.LOGIN_COLLECTION_FILE_NAME))//
        .thenReturn(loginCollectionMock);
    when(loginCollectionMock.findItem(argThat(loginWithUserName(USER_NAME))))//
        .thenReturn(value);

    // action
    instance.authenticate(AUTH_STRING);

  }

  @Test(expected = UnauthorizedException.class)
  public void authenticateThrowsAnUnauthorizedExceptionWhenTheUserIsNotKnown() throws Exception {
    // when
    when(jsonFileHandlerMock.getCollection(LoginCollection.class, LoginCollection.LOGIN_COLLECTION_FILE_NAME))//
        .thenReturn(loginCollectionMock);
    when(loginCollectionMock.findItem(argThat(loginWithUserName(USER_NAME))))//
        .thenReturn(null);

    // action
    instance.authenticate(AUTH_STRING);
  }

  @Test(expected = UnauthorizedException.class)
  public void authenticateThrowsAnUnauthroizedExceptionWhenTheLoginFileCannotBeRead() throws StorageException, UnauthorizedException {
    // when
    doThrow(StorageException.class).when(jsonFileHandlerMock).getCollection(LoginCollection.class, LoginCollection.LOGIN_COLLECTION_FILE_NAME);

    // action
    instance.authenticate(USER_NAME);
  }
}
