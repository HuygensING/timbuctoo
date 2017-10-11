package nl.knaw.huygens.timbuctoo.v5.dropwizard.endpoints.auth;

import nl.knaw.huygens.timbuctoo.security.Authorizer;
import nl.knaw.huygens.timbuctoo.security.LoggedInUsers;
import nl.knaw.huygens.timbuctoo.security.dto.Authorization;
import nl.knaw.huygens.timbuctoo.security.dto.User;
import nl.knaw.huygens.timbuctoo.v5.dataset.dto.PromotedDataSet;
import org.junit.Test;

import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Optional;

import static javax.ws.rs.core.Response.Status.FORBIDDEN;
import static javax.ws.rs.core.Response.Status.UNAUTHORIZED;
import static nl.knaw.huygens.timbuctoo.v5.dropwizard.endpoints.auth.AuthCheck.checkAdminAccess;
import static nl.knaw.huygens.timbuctoo.v5.dropwizard.endpoints.auth.AuthCheck.checkWriteAccess;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

public class AuthCheckTest {

  @Test
  public void checkWriteAccessRerturnNullIfTheDataSetDoesNotExistAndTheUserIdIsTheSameAsTheDataSetOwnerId() {
    String ownerId = "ownerid";
    User owner = User.create(null, ownerId);
    LoggedInUsers loggedInUsers = mock(LoggedInUsers.class);
    given(loggedInUsers.userFor(anyString())).willReturn(Optional.of(owner));

    Response response = checkWriteAccess(
      ownerId,
      "dataSet",
      (user, dataSet) -> Optional.empty(),
      null,
      loggedInUsers,
      "auth"
    );

    assertThat(response, is(nullValue()));
  }

  @Test
  public void checkWriteAccessRerturnForbiddenIfTheDataSetDoesNotExistAndTheUserIdDiffersFromTheDataSetOwnerId() {
    User notOwner = User.create(null, "user");
    LoggedInUsers loggedInUsers = mock(LoggedInUsers.class);
    given(loggedInUsers.userFor(anyString())).willReturn(Optional.of(notOwner));

    Response response = checkWriteAccess(
      "ownerid",
      "dataSet",
      (user, dataSet) -> Optional.empty(),
      null,
      loggedInUsers,
      "auth"
    );

    assertThat(response.getStatus(), is(FORBIDDEN.getStatusCode()));
  }

  @Test
  public void checkAdminAccessReturnsNullIfTheUserHasAdminPermissionsForTheDataSet() throws Exception {
    User notOwner = User.create(null, "user");
    LoggedInUsers loggedInUsers = mock(LoggedInUsers.class);
    given(loggedInUsers.userFor(anyString())).willReturn(Optional.of(notOwner));
    Authorizer authorizer = mock(Authorizer.class);
    given(authorizer.authorizationFor(anyString(), anyString())).willReturn(authorizationForAdmin());
    Response response = checkAdminAccess(
      authorizer,
      loggedInUsers,
      "auth",
      PromotedDataSet.promotedDataSet("ownerid", "datasetid", "http://ex.org", false)
    );

    assertThat(response.getStatus(), is(200));
  }

  @Test
  public void checkAdminAccessReturnsAnUnauthorizedResponseIfTheUserIsUnknown() throws Exception {
    LoggedInUsers loggedInUsers = mock(LoggedInUsers.class);
    given(loggedInUsers.userFor(anyString())).willReturn(Optional.empty());
    Response response = checkAdminAccess(
      null,
      loggedInUsers,
      "auth",
      PromotedDataSet.promotedDataSet("ownerid", "datasetid", "http://ex.org", false)
    );

    assertThat(response.getStatus(), is(UNAUTHORIZED.getStatusCode()));
  }

  @Test
  public void checkAdminAccessReturnsAForbiddenResponseIfTheUserIsNotAnAdminForTheDataSet() throws Exception {
    User notOwner = User.create(null, "user");
    LoggedInUsers loggedInUsers = mock(LoggedInUsers.class);
    given(loggedInUsers.userFor(anyString())).willReturn(Optional.of(notOwner));
    Authorizer authorizer = mock(Authorizer.class);
    given(authorizer.authorizationFor(anyString(), anyString())).willReturn(authorizationForNonAdmin());
    Response response = checkAdminAccess(
      authorizer,
      loggedInUsers,
      "auth",
      PromotedDataSet.promotedDataSet("ownerid", "datasetid", "http://ex.org", false)
    );

    assertThat(response.getStatus(), is(FORBIDDEN.getStatusCode()));
  }

  @Test
  public void checkAdminAccessReturnsNullIfTheUserIsAnAdminForTheDataSet() throws Exception {
    User notOwner = User.create(null, "user");
    LoggedInUsers loggedInUsers = mock(LoggedInUsers.class);
    given(loggedInUsers.userFor(anyString())).willReturn(Optional.of(notOwner));
    Authorizer authorizer = mock(Authorizer.class);
    given(authorizer.authorizationFor(anyString(), anyString())).willReturn(authorizationForAdmin());
    Response response = checkAdminAccess(
      authorizer,
      loggedInUsers,
      "auth",
      PromotedDataSet.promotedDataSet("ownerid", "datasetid", "http://ex.org", false)
    );

    assertThat(response.getStatus(), is(200));
  }

  private static Authorization authorizationForNonAdmin() {
    return auhtorization(false);
  }

  private static Authorization authorizationForAdmin() {
    return auhtorization(true);
  }

  private static Authorization auhtorization(boolean isAdmin) {
    return new Authorization() {
      @Override
      public List<String> getRoles() {
        throw new UnsupportedOperationException("Not implemented");
      }

      @Override
      public boolean isAllowedToWrite() {
        throw new UnsupportedOperationException("Not implemented");
      }

      @Override
      public boolean hasAdminAccess() {
        return isAdmin;
      }
    };
  }
}
