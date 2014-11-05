package nl.knaw.huygens.timbuctoo.security;

import static nl.knaw.huygens.timbuctoo.security.LocalLoggedInUsers.LOCAL_SESSION_KEY_PREFIX;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.security.Principal;
import java.util.EnumSet;

import nl.knaw.huygens.security.client.UnauthorizedException;
import nl.knaw.huygens.security.client.model.SecurityInformation;
import nl.knaw.huygens.security.core.model.Affiliation;
import nl.knaw.huygens.timbuctoo.model.Login;

import org.junit.Before;
import org.junit.Test;

public class LocalLoggedInUsersTest {
  private LocalLoggedInUsers instance;
  private LoginConverter loginConverterMock;

  @Before
  public void setUp() {
    loginConverterMock = mock(LoginConverter.class);
    instance = new LocalLoggedInUsers(loginConverterMock);
  }

  @Test
  public void addMakesThePidOfTheUserRetrievableAndReturnsTheKeyToRetrieveIt() {
    Login login = new Login();

    // action
    String sessionKey = instance.add(login);

    // verify
    Login retrievedLogin = instance.get(sessionKey);
    assertThat(retrievedLogin, is(login));
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

    String sessionKey = instance.add(login);

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

    // action
    instance.getSecurityInformation(unknownSessionKey);
  }

}
