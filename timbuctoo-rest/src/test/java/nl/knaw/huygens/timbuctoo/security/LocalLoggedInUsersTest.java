package nl.knaw.huygens.timbuctoo.security;

import static nl.knaw.huygens.timbuctoo.security.LocalLoggedInUsers.LOCAL_SESSION_KEY_PREFIX;
import static nl.knaw.huygens.timbuctoo.storage.file.LoginCollection.LOGIN_COLLECTION_FILE_NAME;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.security.Principal;
import java.util.EnumSet;

import nl.knaw.huygens.security.client.UnauthorizedException;
import nl.knaw.huygens.security.client.model.SecurityInformation;
import nl.knaw.huygens.security.core.model.Affiliation;
import nl.knaw.huygens.timbuctoo.model.Login;
import nl.knaw.huygens.timbuctoo.storage.StorageException;
import nl.knaw.huygens.timbuctoo.storage.file.JsonFileHandler;
import nl.knaw.huygens.timbuctoo.storage.file.LoginCollection;

import org.junit.Before;
import org.junit.Test;

public class LocalLoggedInUsersTest {
  private static final String AUTHENTICATION_STRING = "test";
  private static final String LOGIN_ID = "loginId";
  private static final String PERSISTENT_ID = "persistentId";
  private LocalLoggedInUsers instance;
  private JsonFileHandler jsonFileHandlerMock;
  private LoginConverter loginConverterMock;

  @Before
  public void setUp() {
    jsonFileHandlerMock = mock(JsonFileHandler.class);
    loginConverterMock = mock(LoginConverter.class);
    instance = new LocalLoggedInUsers(jsonFileHandlerMock, loginConverterMock);
  }

  @Test
  public void addMakesThePidOfTheUserRetrievableAndReturnsTheKeyToRetrieveIt() {
    // action
    String sessionKey = instance.add(PERSISTENT_ID);

    // verify
    String retrievedPID = instance.get(sessionKey);
    assertThat(retrievedPID, is(equalTo(PERSISTENT_ID)));
  }

  @Test
  public void addReturnsAKeyThatStartsWithTheLocalSessionIdPrefix() {
    // action
    String sessionKey = instance.add(PERSISTENT_ID);

    // verify
    assertThat(sessionKey, startsWith(LOCAL_SESSION_KEY_PREFIX));
  }

  @Test
  public void getSecurityInformationReturnsTheSecurityInformationWithTheUserPID() throws Exception {
    // setup
    LoginCollection loginCollection = new LoginCollection();
    Login login = new Login();
    login.setId(LOGIN_ID);
    login.setUserPid(PERSISTENT_ID);
    login.setAuthString(AUTHENTICATION_STRING);
    loginCollection.add(login);

    String sessionKey = instance.add(PERSISTENT_ID);

    SecurityInformation securityInformation = createSecurityInformation();

    // when
    when(jsonFileHandlerMock.getCollection(LoginCollection.class, LOGIN_COLLECTION_FILE_NAME))//
        .thenReturn(loginCollection);
    when(loginConverterMock.toSecurityInformation(login)).thenReturn(securityInformation);

    // action
    SecurityInformation actualSecurityInformation = instance.getSecurityInformation(sessionKey);

    // verify
    assertThat(actualSecurityInformation, is(securityInformation));

  }

  private SecurityInformation createSecurityInformation() {
    return new SecurityInformation() {

      @Override
      public String getSurname() {
        // TODO Auto-generated method stub
        return null;
      }

      @Override
      public Principal getPrincipal() {
        // TODO Auto-generated method stub
        return null;
      }

      @Override
      public String getPersistentID() {
        // TODO Auto-generated method stub
        return null;
      }

      @Override
      public String getOrganization() {
        // TODO Auto-generated method stub
        return null;
      }

      @Override
      public String getGivenName() {
        // TODO Auto-generated method stub
        return null;
      }

      @Override
      public String getEmailAddress() {
        // TODO Auto-generated method stub
        return null;
      }

      @Override
      public String getDisplayName() {
        // TODO Auto-generated method stub
        return null;
      }

      @Override
      public String getCommonName() {
        // TODO Auto-generated method stub
        return null;
      }

      @Override
      public EnumSet<Affiliation> getAffiliations() {
        // TODO Auto-generated method stub
        return null;
      }
    };
  }

  @Test(expected = UnauthorizedException.class)
  public void getSecurityInformationItThrowsAnUnauthorizedExceptionWhenTheSessionCannotBeFound() throws Exception {
    // setup
    String unknownSessionKey = "unknownSessionKey";

    // action
    instance.getSecurityInformation(unknownSessionKey);
  }

  @Test(expected = UnauthorizedException.class)
  public void getSecurityInformationThrowsAnUnauthorizedExceptionWhenThePidCannotBeFoundInTheLoginsCollection() throws Exception {
    // setup
    LoginCollection loginCollection = new LoginCollection();
    when(jsonFileHandlerMock.getCollection(LoginCollection.class, LOGIN_COLLECTION_FILE_NAME))//
        .thenReturn(loginCollection);
    String sessionKey = instance.add(PERSISTENT_ID);

    // action
    instance.getSecurityInformation(sessionKey);
  }

  @Test(expected = UnauthorizedException.class)
  public void getSecurityInformationThrowsAnUnauthorizedExceptionWhenTheLoginCollectionCannotBeFound() throws Exception {
    // setup
    doThrow(StorageException.class).when(jsonFileHandlerMock).getCollection(LoginCollection.class, LOGIN_COLLECTION_FILE_NAME);
    String sessionKey = instance.add(PERSISTENT_ID);

    // action
    instance.getSecurityInformation(sessionKey);

  }
}
