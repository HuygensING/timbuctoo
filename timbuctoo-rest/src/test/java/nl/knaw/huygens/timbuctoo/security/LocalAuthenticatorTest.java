package nl.knaw.huygens.timbuctoo.security;

import static nl.knaw.huygens.timbuctoo.security.LoginMatcher.loginWithAuthString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import nl.knaw.huygens.security.client.UnauthorizedException;
import nl.knaw.huygens.timbuctoo.model.Login;
import nl.knaw.huygens.timbuctoo.storage.JsonFileHandler;
import nl.knaw.huygens.timbuctoo.storage.LoginCollection;
import nl.knaw.huygens.timbuctoo.storage.StorageException;

import org.junit.Before;
import org.junit.Test;

public class LocalAuthenticatorTest {

  private static final String USER_PID = "userPid";
  private static final String NORMALIZED_AUTH_STRING = "normalizedAuthString";
  private LoginCollection loginCollectionMock;
  private LocalAuthenticator instance;
  private JsonFileHandler jsonFileHandlerMock;

  @Before
  public void setUp() {
    loginCollectionMock = mock(LoginCollection.class);
    jsonFileHandlerMock = mock(JsonFileHandler.class);
    instance = new LocalAuthenticator(jsonFileHandlerMock);
  }

  @Test
  public void authenticateReturnsTheUserPidWhenTheUserNameAndPasswordExist() throws Exception {
    // setup
    Login value = new Login();
    value.setUserPid(USER_PID);
    value.setAuthString(NORMALIZED_AUTH_STRING);

    // when
    when(jsonFileHandlerMock.getCollection(LoginCollection.class, LocalAuthenticator.LOGIN_COLLECTION_FILE_NAME))//
        .thenReturn(loginCollectionMock);
    when(loginCollectionMock.findItem(argThat(loginWithAuthString(NORMALIZED_AUTH_STRING))))//
        .thenReturn(value);

    // action
    String actualUserPid = instance.authenticate(NORMALIZED_AUTH_STRING);

    // verify
    assertThat(actualUserPid, is(equalTo(USER_PID)));
  }

  @Test(expected = UnauthorizedException.class)
  public void authenticateThrowsAnUnauthorizedExceptionWhenTheUserIsNotKnown() throws Exception {
    // when
    when(jsonFileHandlerMock.getCollection(LoginCollection.class, LocalAuthenticator.LOGIN_COLLECTION_FILE_NAME))//
        .thenReturn(loginCollectionMock);
    when(loginCollectionMock.findItem(argThat(loginWithAuthString(NORMALIZED_AUTH_STRING))))//
        .thenReturn(null);

    // action
    instance.authenticate(NORMALIZED_AUTH_STRING);
  }

  @Test(expected = UnauthorizedException.class)
  public void authenticateThrowsAnUnauthroizedExceptionWhenTheLoginFileCannotBeRead() throws StorageException, UnauthorizedException {
    // when
    doThrow(StorageException.class).when(jsonFileHandlerMock).getCollection(LoginCollection.class, LocalAuthenticator.LOGIN_COLLECTION_FILE_NAME);

    // action
    instance.authenticate(NORMALIZED_AUTH_STRING);
  }
}
