package nl.knaw.huygens.timbuctoo.server.security;

import nl.knaw.huygens.timbuctoo.security.AuthorizationUnavailableException;
import nl.knaw.huygens.timbuctoo.security.Authorizer;
import nl.knaw.huygens.timbuctoo.security.LoggedInUserStore;
import nl.knaw.huygens.timbuctoo.security.User;
import nl.knaw.huygens.timbuctoo.server.security.UserPermissionChecker.UserPermission;
import org.junit.Before;
import org.junit.Test;

import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;

public class UserPermissionCheckerTest {

  public static final String VRE_NAME = "vreName";
  public static final String AUTHORIZATION_HEADER = "authorizationHeader";
  private LoggedInUserStore loggedInUserStore;
  private Authorizer authorizer;
  private UserPermissionChecker instance;

  @Before
  public void setUp() throws Exception {
    loggedInUserStore = mock(LoggedInUserStore.class);
    authorizer = mock(Authorizer.class);
    instance = new UserPermissionChecker(loggedInUserStore, authorizer);
  }

  @Test
  public void checkReturnsUnknownUserWhenTheUserCannotBeAuthenticated() {
    given(loggedInUserStore.userFor(anyString())).willReturn(Optional.empty());

    UserPermission permission = instance.check(VRE_NAME, AUTHORIZATION_HEADER);

    assertThat(permission, is(UserPermission.UNKNOWN_USER));
  }

  @Test
  public void checkReturnsAllowedToWriteWhenTheUserIsAuthorizedForTheVre() throws Exception {
    given(loggedInUserStore.userFor(anyString())).willReturn(Optional.of(new User("displayName")));
    given(authorizer.authorizationFor(anyString(), anyString())).willReturn(() -> true);

    UserPermission permission = instance.check(VRE_NAME, AUTHORIZATION_HEADER);

    assertThat(permission, is(UserPermission.ALLOWED_TO_WRITE));
  }

  @Test
  public void checkReturnsNoPermissionWhenTheUserIsNotAuthorized() throws Exception {
    given(loggedInUserStore.userFor(anyString())).willReturn(Optional.of(new User("displayName")));
    given(authorizer.authorizationFor(anyString(), anyString())).willReturn(() -> false);

    UserPermission permission = instance.check(VRE_NAME, AUTHORIZATION_HEADER);

    assertThat(permission, is(UserPermission.NO_PERMISSION));
  }

  @Test
  public void checkReturnsUnknownUserWhenTheUserCannotBeAuthorized() throws Exception {
    given(loggedInUserStore.userFor(anyString())).willReturn(Optional.of(new User("displayName")));
    given(authorizer.authorizationFor(anyString(), anyString())).willThrow(new AuthorizationUnavailableException());

    UserPermission permission = instance.check(VRE_NAME, AUTHORIZATION_HEADER);

    assertThat(permission, is(UserPermission.UNKNOWN_USER));
  }


}
