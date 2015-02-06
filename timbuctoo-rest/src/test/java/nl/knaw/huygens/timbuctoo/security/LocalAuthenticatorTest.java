package nl.knaw.huygens.timbuctoo.security;

/*
 * #%L
 * Timbuctoo REST api
 * =======
 * Copyright (C) 2012 - 2015 Huygens ING
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

import static nl.knaw.huygens.timbuctoo.security.LoginMatcher.loginWithUserName;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import nl.knaw.huygens.security.client.UnauthorizedException;
import nl.knaw.huygens.timbuctoo.model.Login;
import nl.knaw.huygens.timbuctoo.storage.StorageException;
import nl.knaw.huygens.timbuctoo.storage.file.JsonFileHandler;
import nl.knaw.huygens.timbuctoo.storage.file.LoginCollection;

import org.apache.xml.security.utils.Base64;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;

public class LocalAuthenticatorTest {

  private static final String ENCRYPTED_PASSWORD = "encryptedPassword";
  private static final String AUTH_STRING = Base64.encode("userName:password".getBytes());
  private static final String USER_NAME = "userName";
  private static final String USER_PID = "userPid";
  private static final String PASSWORD = "password";
  private static final String OTHER_ENCRYPTED_PASSWORD = "otherPassword";
  private static final byte[] SALT = "salt".getBytes();
  private LoginCollection loginCollectionMock;
  private LocalAuthenticator instance;
  private JsonFileHandler jsonFileHandlerMock;
  private static PasswordEncrypter passwordEncrypterMock;

  @Before
  public void setUp() throws Exception {
    loginCollectionMock = mock(LoginCollection.class);
    jsonFileHandlerMock = mock(JsonFileHandler.class);
    passwordEncrypterMock = setupPasswordEncrypterMock();
    instance = new LocalAuthenticator(jsonFileHandlerMock, passwordEncrypterMock);
  }

  private PasswordEncrypter setupPasswordEncrypterMock() {
    PasswordEncrypter passwordEncrypterMock = mock(PasswordEncrypter.class);

    when(passwordEncrypterMock.encryptPassword(PASSWORD, SALT)).thenReturn(ENCRYPTED_PASSWORD);

    return passwordEncrypterMock;
  }

  private void setupJsonFileHandler() throws StorageException {
    when(jsonFileHandlerMock.getCollection(LoginCollection.class, LoginCollection.LOGIN_COLLECTION_FILE_NAME))//
        .thenReturn(loginCollectionMock);
  }

  @Test
  public void authenticateReturnsTheUserPidWhenTheUserNameAndPasswordExist() throws Exception {
    // setup
    Login login = createLogin(USER_PID, USER_NAME, ENCRYPTED_PASSWORD, SALT);

    // when
    setupJsonFileHandler();
    when(loginCollectionMock.findItem(argThat(loginWithUserName(USER_NAME))))//
        .thenReturn(login);

    // action
    Login actualLogin = instance.authenticate(AUTH_STRING);

    // verify
    assertThat(actualLogin, is(login));
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
    setupJsonFileHandler();
    try {
      // action
      instance.authenticate(AUTH_STRING);
    } finally {
      //verify
      verify(passwordEncrypterMock).encryptPassword(anyString(), Matchers.<byte[]> any());
    }
  }

  @Test(expected = UnauthorizedException.class)
  public void authenticateThrowsAnUnauthorizedExceptionWhenThePasswordIsNotEqual() throws Exception {
    // setup
    Login value = createLogin(USER_PID, USER_NAME, OTHER_ENCRYPTED_PASSWORD, SALT);

    // when
    setupJsonFileHandler();
    when(loginCollectionMock.findItem(argThat(loginWithUserName(USER_NAME))))//
        .thenReturn(value);

    try {
      // action
      instance.authenticate(AUTH_STRING);
    } finally {
      //verify
      verify(passwordEncrypterMock).encryptPassword(anyString(), Matchers.<byte[]> any());
    }

  }

  @Test(expected = UnauthorizedException.class)
  public void authenticateThrowsAnUnauthorizedExceptionWhenTheUserIsNotKnown() throws Exception {
    // when
    setupJsonFileHandler();
    when(loginCollectionMock.findItem(argThat(loginWithUserName(USER_NAME))))//
        .thenReturn(null);

    try {
      // action
      instance.authenticate(AUTH_STRING);
    } finally {
      //verify
      verify(passwordEncrypterMock).encryptPassword(anyString(), Matchers.<byte[]> any());
    }
  }

  @Test(expected = UnauthorizedException.class)
  public void authenticateThrowsAnUnauthroizedExceptionWhenTheLoginFileCannotBeRead() throws StorageException, UnauthorizedException {
    // when
    doThrow(StorageException.class).when(jsonFileHandlerMock).getCollection(LoginCollection.class, LoginCollection.LOGIN_COLLECTION_FILE_NAME);

    try {
      // action
      instance.authenticate(AUTH_STRING);
    } finally {
      //verify
      verify(passwordEncrypterMock).encryptPassword(anyString(), Matchers.<byte[]> any());
    }
  }
}
