package nl.knaw.huygens.timbuctoo.security;

import nl.knaw.huygens.hamcrest.OptionalPresentMatcher;
import nl.knaw.huygens.security.client.AuthenticationHandler;
import nl.knaw.huygens.security.client.model.SecurityInformation;
import nl.knaw.huygens.security.core.model.Affiliation;
import nl.knaw.huygens.timbuctoo.security.dto.User;
import org.junit.Before;
import org.junit.Test;
import sun.rmi.runtime.Log;

import javax.annotation.Nullable;
import java.security.Principal;
import java.util.EnumSet;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

public class BasicUserValidatorTest {

  private AuthenticationHandler authenticationHandler;
  private UserStore userStore;
  private BasicUserValidator basicUserValidator;
  private LoggedInUsers loggedInUsers;

  @Before
  public void setUp() throws Exception {
    authenticationHandler = mock(AuthenticationHandler.class);
    userStore = mock(UserStore.class);
    loggedInUsers = mock(LoggedInUsers.class);
    basicUserValidator = new BasicUserValidator(authenticationHandler, userStore, null);
  }

  @Test
  public void getUserFromAccessTokenReturnsEmptyWhenAccessTokenIsNull() throws Exception {
    Optional<User> user = basicUserValidator.getUserFromAccessToken(null);

    assertThat(user, is(Optional.empty()));
  }

  @Test
  public void getUserFromAccessTokenReturnsUserWhenAccessTokenIsValid() throws Exception {
    given(loggedInUsers.userFor("validAccessToken")).willReturn(Optional.of(createMockUser()));

    BasicUserValidator basicUserValidator2 = new BasicUserValidator(authenticationHandler, userStore, loggedInUsers);

    Optional<User> user = basicUserValidator2.getUserFromAccessToken("validAccessToken");

    assertThat(user, is(OptionalPresentMatcher.present()));
  }

  @Test
  public void getUserFromAccessTokenReturnsEmptyWhenAccessTokenIsInvalid() throws Exception {
    given(loggedInUsers.userFor("invalidAccessToken")).willReturn(
      null
    );

    BasicUserValidator basicUserValidator2 = new BasicUserValidator(authenticationHandler, userStore, loggedInUsers);

    Optional<User> user = basicUserValidator2.getUserFromAccessToken("validAccessToken");

    assertThat(user, is(Optional.empty()));
  }

  @Test
  public void getUserFromIdReturnsEmptyWhenIdIsNull() throws Exception {
    Optional<User> user = basicUserValidator.getUserFromId(null);

    assertThat(user, is(Optional.empty()));
  }

  @Test
  public void getUserFromIdReturnsUserWhenIdIsValid() throws Exception {
    given(userStore.userForId("testUserId")).willReturn(Optional.of(createMockUser()));

    Optional<User> user = basicUserValidator.getUserFromId("testUserId");

    assertThat(user, is(OptionalPresentMatcher.present()));
  }

  private SecurityInformation createMockSecurityInformation(final String persistentId) {
    return new SecurityInformation() {
      @Override
      public String getDisplayName() {
        return "";
      }

      @Override
      public Principal getPrincipal() {
        return null;
      }

      @Override
      public String getCommonName() {
        return null;
      }

      @Override
      public String getGivenName() {
        return null;
      }

      @Override
      public String getSurname() {
        return null;
      }

      @Override
      public String getEmailAddress() {
        return null;
      }

      @Override
      public EnumSet<Affiliation> getAffiliations() {
        return null;
      }

      @Override
      public String getOrganization() {
        return null;
      }

      @Override
      public String getPersistentID() {
        return persistentId;
      }
    };
  }

  private User createMockUser() {
    return new User() {
      @Nullable
      @Override
      public String getDisplayName() {
        return null;
      }

      @Nullable
      @Override
      public String getPersistentId() {
        return null;
      }

      @Override
      public String getId() {
        return null;
      }
    };
  }

}
