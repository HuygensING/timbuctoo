package nl.knaw.huygens.timbuctoo.server.security;

import com.google.common.collect.Sets;
import nl.knaw.huygens.timbuctoo.security.dto.Authorization;
import nl.knaw.huygens.timbuctoo.v5.security.dto.User;
import nl.knaw.huygens.timbuctoo.server.security.UserPermissionChecker.UserPermission;
import nl.knaw.huygens.timbuctoo.v5.security.PermissionFetcher;
import nl.knaw.huygens.timbuctoo.v5.security.UserValidator;
import nl.knaw.huygens.timbuctoo.v5.security.dto.Permission;
import nl.knaw.huygens.timbuctoo.v5.security.exceptions.PermissionFetchingException;
import nl.knaw.huygens.timbuctoo.v5.security.exceptions.UserValidationException;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;

public class UserPermissionCheckerTest {

  public static final String VRE_NAME = "vreName";
  public static final String AUTHORIZATION_HEADER = "authorizationHeader";
  public static final boolean ALLOWED_TO_WRITE = true;
  public static final boolean NOT_ALLOWED_TO_WRITE = false;
  private UserValidator userValidator;
  private PermissionFetcher permissionFetcher;
  private UserPermissionChecker instance;

  @Before
  public void setUp() throws Exception {
    userValidator = mock(UserValidator.class);
    permissionFetcher = mock(PermissionFetcher.class);
    instance = new UserPermissionChecker(userValidator, permissionFetcher);
  }

  @Test
  public void checkReturnsUnknownUserWhenTheUserCannotBeAuthenticated() throws Exception {
    given(userValidator.getUserFromAccessToken(anyString())).willReturn(Optional.empty());

    UserPermission permission = instance.check(VRE_NAME, AUTHORIZATION_HEADER);

    assertThat(permission, is(UserPermission.UNKNOWN_USER));
  }

  @Test
  public void checkReturnsAllowedToWriteWhenTheUserIsAuthorizedForTheVre() throws Exception {
    given(userValidator.getUserFromAccessToken(anyString())).willReturn(Optional.of(User.create("displayName", "")));
    given(permissionFetcher.getPermissions(any(),any())).willReturn(Sets.newHashSet(Permission.READ,
      Permission.WRITE));

    UserPermission permission = instance.check(VRE_NAME, AUTHORIZATION_HEADER);

    assertThat(permission, is(UserPermission.ALLOWED_TO_WRITE));
  }

  @Test
  public void checkReturnsNoPermissionWhenTheUserIsNotAuthorized() throws Exception {
    given(userValidator.getUserFromAccessToken(anyString()))
      .willReturn(Optional.of(User.create("displayName", "")));
    given(permissionFetcher.getPermissions(any(),any())).willReturn(Sets.newHashSet(Permission.READ));

    UserPermission permission = instance.check(VRE_NAME, AUTHORIZATION_HEADER);

    assertThat(permission, is(UserPermission.NO_PERMISSION));
  }

  private Authorization authorization(boolean isAllowedToWrite) {
    return new Authorization() {
      @Override
      public List<String> getRoles() {
        throw new UnsupportedOperationException("Not implemented yet");
      }

      @Override
      public boolean isAllowedToWrite() {
        return isAllowedToWrite;
      }

      @Override
      public boolean hasAdminAccess() {
        throw new UnsupportedOperationException("Not implemented");
      }
    };
  }

  @Test
  public void checkReturnsUnknownUserWhenTheUserCannotBeAuthorized() throws Exception {
    given(userValidator.getUserFromAccessToken(anyString()))
      .willReturn(Optional.of(User.create("displayName", "")));
    given(permissionFetcher.getPermissions(any(),any())).willThrow(new PermissionFetchingException(""));

    UserPermission permission = instance.check(VRE_NAME, AUTHORIZATION_HEADER);

    assertThat(permission, is(UserPermission.UNKNOWN_USER));
  }


}
