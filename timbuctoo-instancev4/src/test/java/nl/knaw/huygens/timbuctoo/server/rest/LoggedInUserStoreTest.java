package nl.knaw.huygens.timbuctoo.server.rest;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isEmptyString;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.core.Is.is;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

public class LoggedInUserStoreTest {

  private LoggedInUserStore userStoreWithUserA;
  private LoggedInUserStore userStoreWithUserAandB;

  @Before
  public void setUp() throws Exception {
    JsonBasedAuthenticator jsonBasedAuthenticator = mock(JsonBasedAuthenticator.class);
    given(jsonBasedAuthenticator.authenticate("a", "b")).willReturn("pid");
    userStoreWithUserA = new LoggedInUserStore(jsonBasedAuthenticator);

    jsonBasedAuthenticator = mock(JsonBasedAuthenticator.class);
    given(jsonBasedAuthenticator.authenticate("a", "b")).willReturn("pid");
    given(jsonBasedAuthenticator.authenticate("c", "d")).willReturn("otherPid");
    userStoreWithUserAandB = new LoggedInUserStore(jsonBasedAuthenticator);

  }

  @Test
  public void canStoreAUserAndReturnsAToken() {
    LoggedInUserStore instance = userStoreWithUserA;

    String token = instance.userTokenFor("a", "b");

    assertThat(token, not(isEmptyString()));
  }

  @Test
  public void canRetrieveAStoredUser() {
    LoggedInUserStore instance = userStoreWithUserA;
    String token = instance.userTokenFor("a", "b");

    User user = instance.userFor(token);

    assertThat(user, is(not(nullValue())));
  }

  @Test
  public void willReturnAUniqueTokenForEachUser() {
    LoggedInUserStore instance = userStoreWithUserAandB;

    String tokenA = instance.userTokenFor("a", "b");
    String tokenB = instance.userTokenFor("c", "d");

    assertThat(tokenA, is(not(tokenB)));
  }

  //FIXME: same token same user?

  @Test
  public void willReturnTheSameUserForATokenEachTime() {
    LoggedInUserStore instance = userStoreWithUserA;
    String token = instance.userTokenFor("a", "b");

    User user = instance.userFor(token);
    User sameUser = instance.userFor(token);

    assertThat(user, is(sameUser));
  }

  @Test
  public void willReturnTheUserBelongingToTheToken() {
    LoggedInUserStore instance = userStoreWithUserAandB;
    String tokenA = instance.userTokenFor("a", "b");
    String tokenB = instance.userTokenFor("c", "b");

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
  public void returnsNoTokenIfTheUserIsUnknown() {
    LoggedInUserStore instance = userStoreWithUserA;

    String token = instance.userTokenFor("unknownUser", "");

    assertThat(token, is(nullValue()));
  }
}
