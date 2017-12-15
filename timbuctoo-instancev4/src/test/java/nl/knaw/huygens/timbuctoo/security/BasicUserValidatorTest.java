package nl.knaw.huygens.timbuctoo.security;

import nl.knaw.huygens.hamcrest.OptionalPresentMatcher;
import nl.knaw.huygens.security.client.AuthenticationHandler;
import nl.knaw.huygens.timbuctoo.v5.security.dto.User;
import org.junit.Before;
import org.junit.Test;

import java.util.Optional;

import static nl.knaw.huygens.timbuctoo.security.dto.UserStubs.anyUser;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
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
    given(loggedInUsers.userFor("validAccessToken")).willReturn(Optional.of(anyUser()));

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
  public void getUserFromUserIdReturnsEmptyWhenIdIsNull() throws Exception {
    Optional<User> user = basicUserValidator.getUserFromUserId(null);

    assertThat(user, is(Optional.empty()));
  }

  @Test
  public void getUserFromUserIdReturnsUserWhenIdIsValid() throws Exception {
    given(userStore.userForId("testUserId")).willReturn(Optional.of(anyUser()));

    Optional<User> user = basicUserValidator.getUserFromUserId("testUserId");

    assertThat(user, is(OptionalPresentMatcher.present()));
  }

}
