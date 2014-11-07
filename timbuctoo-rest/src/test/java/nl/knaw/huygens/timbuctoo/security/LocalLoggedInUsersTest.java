package nl.knaw.huygens.timbuctoo.security;

import static nl.knaw.huygens.timbuctoo.security.LocalLoggedInUsers.LOCAL_SESSION_KEY_PREFIX;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.security.Principal;
import java.util.EnumSet;

import nl.knaw.huygens.security.client.UnauthorizedException;
import nl.knaw.huygens.security.client.model.SecurityInformation;
import nl.knaw.huygens.security.core.model.Affiliation;
import nl.knaw.huygens.timbuctoo.model.Login;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.google.common.cache.Cache;

public class LocalLoggedInUsersTest {
  private LocalLoggedInUsers instance;
  private LoginConverter loginConverterMock;
  @Mock
  private Cache<String, Login> cacheMock;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    loginConverterMock = mock(LoginConverter.class);
    instance = new LocalLoggedInUsers(loginConverterMock, cacheMock);
  }

  @Test
  public void addMakesThePidOfTheUserRetrievableAndReturnsTheKeyToRetrieveIt() {
    Login login = new Login();

    // action
    String sessionKey = instance.add(login);

    // verify
    verify(cacheMock).put(sessionKey, login);
  }

  @Test
  public void addReturnsAKeyThatStartsWithTheLocalSessionIdPrefix() {
    // action
    String sessionKey = instance.add(new Login());

    // verify
    assertThat(sessionKey, startsWith(LOCAL_SESSION_KEY_PREFIX));
  }

  @Test
  public void getSecurityInformationReturnsTheSecurityInformationWithTheUserPID() throws Exception {
    // setup
    Login login = new Login();
    String sessionKey = "anyString";

    // when
    when(cacheMock.getIfPresent(anyString())).thenReturn(login);

    SecurityInformation securityInformation = createSecurityInformation();

    // when
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

    try {
      // action
      instance.getSecurityInformation(unknownSessionKey);
    } finally {
      verify(cacheMock).getIfPresent(unknownSessionKey);
    }
  }

}
