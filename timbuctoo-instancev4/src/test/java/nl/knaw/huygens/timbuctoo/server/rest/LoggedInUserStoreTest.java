package nl.knaw.huygens.timbuctoo.server.rest;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Optional;

import static nl.knaw.huygens.timbuctoo.server.rest.OptionalPresentMatcher.present;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isEmptyString;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.core.Is.is;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;

public class LoggedInUserStoreTest {

  private LoggedInUserStore userStoreWithUserA;
  private LoggedInUserStore userStoreWithUserAandB;

  @Before
  public void setUp() throws Exception {
    JsonBasedAuthenticator jsonBasedAuthenticator = mock(JsonBasedAuthenticator.class);
    // set default value
    given(jsonBasedAuthenticator.authenticate(anyString(), anyString())).willReturn(Optional.empty());
    given(jsonBasedAuthenticator.authenticate("a", "b")).willReturn(Optional.of("pid"));
    userStoreWithUserA = new LoggedInUserStore(jsonBasedAuthenticator);

    jsonBasedAuthenticator = mock(JsonBasedAuthenticator.class);

    given(jsonBasedAuthenticator.authenticate(anyString(), anyString())).willReturn(Optional.empty());
    given(jsonBasedAuthenticator.authenticate("a", "b")).willReturn(Optional.of("pid"));
    given(jsonBasedAuthenticator.authenticate("c", "d")).willReturn(Optional.of("otherPid"));
    userStoreWithUserAandB = new LoggedInUserStore(jsonBasedAuthenticator);

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

    User user = instance.userFor(token);

    assertThat(user, is(not(nullValue())));
  }

  @Test
  public void willReturnAUniqueTokenForEachUser() throws LocalLoginUnavailableException {
    LoggedInUserStore instance = userStoreWithUserAandB;

    String tokenA = instance.userTokenFor("a", "b").get();
    String tokenB = instance.userTokenFor("c", "d").get();

    assertThat(tokenA, is(not(tokenB)));
  }

  //FIXME: same token same user?

  @Test
  public void willReturnTheSameUserForATokenEachTime() throws LocalLoginUnavailableException {
    LoggedInUserStore instance = userStoreWithUserA;
    String token = instance.userTokenFor("a", "b").get();

    User user = instance.userFor(token);
    User sameUser = instance.userFor(token);

    assertThat(user, is(sameUser));
  }

  @Test
  public void willReturnTheUserBelongingToTheToken() throws LocalLoginUnavailableException {
    LoggedInUserStore instance = userStoreWithUserAandB;
    String tokenA = instance.userTokenFor("a", "b").get();
    String tokenB = instance.userTokenFor("c", "d").get();

    User userA = instance.userFor(tokenA);
    User userB = instance.userFor(tokenB);

    assertThat(userA, is(not(userB)));
  }

  @Test
  public void returnsNullForABogusToken() {
    LoggedInUserStore instance = userStoreWithUserA;
    String bogusToken = "bogus";

    User user = instance.userFor(bogusToken);

    assertThat(user, is(nullValue()));
  }

  @Test
  public void returnsNoTokenIfTheUserIsUnknown() throws LocalLoginUnavailableException {
    LoggedInUserStore instance = userStoreWithUserA;

    Optional<String> token = instance.userTokenFor("unknownUser", "");

    assertThat(token, is(not(present())));
  }

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Test
  public void throwsLocalLoginUnavailableExceptionWhenTheUserCouldNotBeAuthenticatedLocallyDueToASystemError()
    throws LocalLoginUnavailableException {
    JsonBasedAuthenticator authenticator = mock(JsonBasedAuthenticator.class);
    LoggedInUserStore instance = new LoggedInUserStore(authenticator);
    given(authenticator.authenticate(anyString(), anyString())).willThrow(new LocalLoginUnavailableException(""));

    expectedException.expect(LocalLoginUnavailableException.class);

    instance.userTokenFor("", "");
  }

}
