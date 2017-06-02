package nl.knaw.huygens.timbuctoo.v5.dropwizard.endpoints.auth;

import nl.knaw.huygens.timbuctoo.security.Authorizer;
import nl.knaw.huygens.timbuctoo.security.LoggedInUsers;
import nl.knaw.huygens.timbuctoo.security.dto.User;
import nl.knaw.huygens.timbuctoo.security.exceptions.AuthorizationUnavailableException;

import javax.ws.rs.core.Response;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;

public class AuthCheck {
  public static Response checkWriteAccess(BiFunction<String, String, Boolean> dataSetExists,
                                          Authorizer authorizer, LoggedInUsers loggedInUsers, String authHeader,
                                          String dataSetOwnerId, String dataSetId) {
    if (dataSetOwnerId == null || dataSetOwnerId.isEmpty()) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }
    if (dataSetId == null || dataSetId.isEmpty()) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }

    Optional<User> user = loggedInUsers.userFor(authHeader);

    if (!user.isPresent()) {
      return Response.status(Response.Status.UNAUTHORIZED).build();
    }
    String currentUserId = user.get().getPersistentId();
    if (dataSetExists.apply(dataSetOwnerId, dataSetId)) {

      try {
        if (!authorizer.authorizationFor(dataSetOwnerId + "_" + dataSetId, currentUserId).isAllowedToWrite()) {
          return Response.status(Response.Status.FORBIDDEN).build();
        }
      } catch (AuthorizationUnavailableException e) {
        // The does not yet exist. It will be created by getOrCreate below. So check if the user is accessing a dataSet
        // under his or her own namespace
        if (!dataSetOwnerId.equals(currentUserId)) {
          return Response.status(Response.Status.FORBIDDEN).build();
        }
      }
    } else if (!Objects.equals(currentUserId, dataSetOwnerId)) {
      return Response.status(Response.Status.FORBIDDEN).build();
    }
    return null;
  }
}
