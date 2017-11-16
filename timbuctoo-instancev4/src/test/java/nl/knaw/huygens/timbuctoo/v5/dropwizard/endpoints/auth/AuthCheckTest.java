package nl.knaw.huygens.timbuctoo.v5.dropwizard.endpoints.auth;

import com.google.common.collect.Sets;
import nl.knaw.huygens.timbuctoo.v5.security.dto.User;
import nl.knaw.huygens.timbuctoo.v5.dataset.dto.PromotedDataSet;
import nl.knaw.huygens.timbuctoo.v5.security.PermissionFetcher;
import nl.knaw.huygens.timbuctoo.v5.security.UserValidator;
import nl.knaw.huygens.timbuctoo.v5.security.dto.Permission;
import org.junit.Test;

import javax.ws.rs.core.Response;
import java.util.Optional;
import java.util.Set;

import static javax.ws.rs.core.Response.Status.FORBIDDEN;
import static javax.ws.rs.core.Response.Status.UNAUTHORIZED;
import static nl.knaw.huygens.timbuctoo.v5.dropwizard.endpoints.auth.AuthCheck.checkAdminAccess;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

public class AuthCheckTest {

  private static Set<Permission> permissionsForNonAdmin() {
    return Sets.newHashSet(Permission.WRITE, Permission.READ);
  }

  private static Set<Permission> permissionsForAdmin() {
    return Sets.newHashSet(Permission.WRITE, Permission.ADMIN, Permission.READ);
  }

  @Test
  public void checkAdminAccessReturnsNullIfTheUserHasAdminPermissionsForTheDataSet() throws Exception {
    User notOwner = User.create(null, "user");
    UserValidator userValidator = mock(UserValidator.class);
    given(userValidator.getUserFromAccessToken(anyString())).willReturn(Optional.of(notOwner));
    PermissionFetcher permissionFetcher = mock(PermissionFetcher.class);
    given(permissionFetcher.getPermissions(anyString(), any(PromotedDataSet.class))).willReturn(permissionsForAdmin());
    Response response = checkAdminAccess(
      permissionFetcher,
      userValidator,
      "auth",
      PromotedDataSet.promotedDataSet("ownerid", "datasetid", "http://ex.org", "http://example.org/prefix/",
        false, false)
    );

    assertThat(response.getStatus(), is(200));
  }

  @Test
  public void checkAdminAccessReturnsAnUnauthorizedResponseIfTheUserIsUnknown() throws Exception {
    UserValidator userValidator = mock(UserValidator.class);
    given(userValidator.getUserFromAccessToken(anyString())).willReturn(Optional.empty());
    Response response = checkAdminAccess(
      null,
      userValidator,
      "auth",
      PromotedDataSet.promotedDataSet("ownerid", "datasetid", "http://ex.org",
        "http://example.org/prefix/", false, false)
    );

    assertThat(response.getStatus(), is(UNAUTHORIZED.getStatusCode()));
  }

  @Test
  public void checkAdminAccessReturnsAForbiddenResponseIfTheUserIsNotAnAdminForTheDataSet() throws Exception {
    User notOwner = User.create(null, "user");
    UserValidator userValidator = mock(UserValidator.class);
    given(userValidator.getUserFromAccessToken(anyString())).willReturn(Optional.of(notOwner));
    PermissionFetcher permissionFetcher = mock(PermissionFetcher.class);

    given(permissionFetcher.getPermissions(anyString(), any(PromotedDataSet.class)))
      .willReturn(permissionsForNonAdmin());
    Response response = checkAdminAccess(
      permissionFetcher,
      userValidator,
      "auth",
      PromotedDataSet.promotedDataSet("ownerid", "datasetid", "http://ex.org",
        "http://example.org/prefix/", false, false)
    );

    assertThat(response.getStatus(), is(FORBIDDEN.getStatusCode()));
  }

  @Test
  public void checkAdminAccessReturnsNullIfTheUserIsAnAdminForTheDataSet() throws Exception {
    User notOwner = User.create(null, "user");
    UserValidator userValidator = mock(UserValidator.class);
    given(userValidator.getUserFromAccessToken(anyString())).willReturn(Optional.of(notOwner));
    PermissionFetcher permissionFetcher = mock(PermissionFetcher.class);
    given(permissionFetcher.getPermissions(anyString(), any(PromotedDataSet.class))).willReturn(permissionsForAdmin());
    Response response = checkAdminAccess(
      permissionFetcher,
      userValidator,
      "auth",
      PromotedDataSet.promotedDataSet("ownerid", "datasetid", "http://ex.org",
        "http://example.org/prefix/", false, false)
    );

    assertThat(response.getStatus(), is(200));
  }

}
