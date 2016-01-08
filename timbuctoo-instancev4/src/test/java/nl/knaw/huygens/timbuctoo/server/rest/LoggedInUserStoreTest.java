package nl.knaw.huygens.timbuctoo.server.rest;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Optional;

import static java.util.concurrent.TimeUnit.SECONDS;
import static nl.knaw.huygens.timbuctoo.server.rest.OptionalPresentMatcher.present;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isEmptyString;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.core.Is.is;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;

public class LoggedInUserStoreTest {

  public static final Timeout ONE_SECOND_TIMEOUT = new Timeout(1, SECONDS);
  private LoggedInUserStore userStoreWithUserA;
  private LoggedInUserStore userStoreWithUserAAndB;

  @Before
  public void setUp() throws Exception {
    JsonBasedAuthenticator jsonBasedAuthenticator = mock(JsonBasedAuthenticator.class);
    // set default value
    given(jsonBasedAuthenticator.authenticate(anyString(), anyString())).willReturn(Optional.empty());
    given(jsonBasedAuthenticator.authenticate("a", "b")).willReturn(Optional.of("pid"));
    userStoreWithUserA = new LoggedInUserStore(jsonBasedAuthenticator, ONE_SECOND_TIMEOUT);

    jsonBasedAuthenticator = mock(JsonBasedAuthenticator.class);

    given(jsonBasedAuthenticator.authenticate(anyString(), anyString())).willReturn(Optional.empty());
    given(jsonBasedAuthenticator.authenticate("a", "b")).willReturn(Optional.of("pid"));
    given(jsonBasedAuthenticator.authenticate("c", "d")).willReturn(Optional.of("otherPid"));
    userStoreWithUserAAndB = new LoggedInUserStore(jsonBasedAuthenticator, ONE_SECOND_TIMEOUT);

  }

  @Test
  public void canStoreAUserAndReturnsAToken() throws LocalLoginUnavailableException {
    LoggedInUserStore instance = userStoreWithUserA;

    Optional<String> token = instance.userTokenFor("a", "b");

    assertThat(token, is(present()));
    assertThat(token.get(), not(isEmptyString()));
  }

  @Test
  public void canRetrieveAStoredUser() throws LocalLoginUnavailableException {
    LoggedInUserStore instance = userStoreWithUserA;
    String token = instance.userTokenFor("a", "b").get();

    Optional<User> user = instance.userFor(token);

    assertThat(user, is(present()));
  }

  @Test
  public void willReturnAUniqueTokenForEachUser() throws LocalLoginUnavailableException {
    LoggedInUserStore instance = userStoreWithUserAAndB;

    String tokenA = instance.userTokenFor("a", "b").get();
    String tokenB = instance.userTokenFor("c", "d").get();

    assertThat(tokenA, is(not(tokenB)));
  }

  //FIXME: same token same user?

  @Test
  public void willReturnTheSameUserForATokenEachTime() throws LocalLoginUnavailableException {
    LoggedInUserStore instance = userStoreWithUserA;
    String token = instance.userTokenFor("a", "b").get();

    User user = instance.userFor(token).get();
    User sameUser = instance.userFor(token).get();

    assertThat(user, is(sameUser));
  }

  @Test
  public void willReturnTheUserBelongingToTheToken() throws LocalLoginUnavailableException {
    LoggedInUserStore instance = userStoreWithUserAAndB;
    String tokenA = instance.userTokenFor("a", "b").get();
    String tokenB = instance.userTokenFor("c", "d").get();

    User userA = instance.userFor(tokenA).get();
    User userB = instance.userFor(tokenB).get();

    assertThat(userA, is(not(userB)));
  }

  @Test
  public void returnsAnEmptyOptionalForABogusToken() {
    LoggedInUserStore instance = userStoreWithUserA;
    String bogusToken = "bogus";

    Optional<User> user = instance.userFor(bogusToken);

    assertThat(user, is(not(present())));
  }

  @Test
  public void returnsNoTokenIfTheUserIsUnknown() throws LocalLoginUnavailableException {
    LoggedInUserStore instance = userStoreWithUserA;

    Optional<String> token = instance.userTokenFor("unknownUser", "");

    assertThat(token, is(not(present())));
  }

  @Test
  public void returnsAnEmptyOptionalIfTheUserIsRetrievedAfterATimeout() throws LocalLoginUnavailableException,
    InterruptedException {
    LoggedInUserStore instance = this.userStoreWithUserA;
    String token = instance.userTokenFor("a", "b").get();

    Thread.sleep(ONE_SECOND_TIMEOUT.toMiliSeconds() + 1);

    Optional<User> user = instance.userFor(token);

    assertThat(user, is(not(present())));
  }

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Test
  public void throwsLocalLoginUnavailableExceptionWhenTheUserCouldNotBeAuthenticatedLocallyDueToASystemError()
    throws LocalLoginUnavailableException {
    JsonBasedAuthenticator authenticator = mock(JsonBasedAuthenticator.class);
    LoggedInUserStore instance = new LoggedInUserStore(authenticator, ONE_SECOND_TIMEOUT);
    given(authenticator.authenticate(anyString(), anyString())).willThrow(new LocalLoginUnavailableException(""));

    expectedException.expect(LocalLoginUnavailableException.class);

    instance.userTokenFor("", "");
  }

}
