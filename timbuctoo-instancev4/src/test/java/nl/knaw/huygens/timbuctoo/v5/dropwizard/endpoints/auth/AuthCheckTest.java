package nl.knaw.huygens.timbuctoo.v5.dropwizard.endpoints.auth;

import nl.knaw.huygens.timbuctoo.security.LoggedInUsers;
import nl.knaw.huygens.timbuctoo.security.dto.User;
import org.junit.Test;

import javax.ws.rs.core.Response;
import java.util.Optional;

import static javax.ws.rs.core.Response.Status.FORBIDDEN;
import static nl.knaw.huygens.timbuctoo.v5.dropwizard.endpoints.auth.AuthCheck.checkWriteAccess;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

public class AuthCheckTest {

  @Test
  public void checkWriteAccessRerturnNullIfTheDataSetDoesNotExistAndTheUserIdIsTheSameAsTheDataSetOwnerId() {
    String ownerId = "ownerId";
    User owner = User.create(null, ownerId);
    LoggedInUsers loggedInUsers = mock(LoggedInUsers.class);
    given(loggedInUsers.userFor(anyString())).willReturn(Optional.of(owner));

    Response response = checkWriteAccess((user, dataSet) -> false, null, loggedInUsers, "auth", ownerId, "dataSet");

    assertThat(response, is(nullValue()));
  }

  @Test
  public void checkWriteAccessRerturnForbiddenIfTheDataSetDoesNotExistAndTheUserIdDiffersFromTheDataSetOwnerId() {
    User notOwner = User.create(null, "user");
    LoggedInUsers loggedInUsers = mock(LoggedInUsers.class);
    given(loggedInUsers.userFor(anyString())).willReturn(Optional.of(notOwner));

    Response response = checkWriteAccess((user, dataSet) -> false, null, loggedInUsers, "auth", "ownerId", "dataSet");

    assertThat(response, is(notNullValue()));
    assertThat(response.getStatus(), is(FORBIDDEN.getStatusCode()));
  }

}
