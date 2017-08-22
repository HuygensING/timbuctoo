package nl.knaw.huygens.timbuctoo.v5.dropwizard.endpoints.auth;

import nl.knaw.huygens.timbuctoo.security.Authorizer;
import nl.knaw.huygens.timbuctoo.security.LoggedInUsers;
import nl.knaw.huygens.timbuctoo.security.dto.Authorization;
import nl.knaw.huygens.timbuctoo.security.dto.User;
import nl.knaw.huygens.timbuctoo.security.exceptions.AuthorizationUnavailableException;
import nl.knaw.huygens.timbuctoo.v5.dataset.dto.DataSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;

public class AuthCheck {
  private static final Logger LOG = LoggerFactory.getLogger(AuthCheck.class);

  public static Response checkWriteAccess(DataSet dataSet, Optional<User> user, Authorizer authorizer) {
    if (!user.isPresent()) {
      return Response.status(Response.Status.UNAUTHORIZED).build();
    }
    String currentUserId = user.get().getPersistentId();
    try {
      final Authorization authorization = getAuthorization(
        authorizer,
        dataSet.getOwnerId(),
        dataSet.getDataSetId(),
        currentUserId
      );
      if (!authorization.isAllowedToWrite()) {
        return Response.status(Response.Status.FORBIDDEN).build();
      }
    } catch (AuthorizationUnavailableException e) {
      LOG.error("Authorization unavailable", e);
      //The dataset should already exist, so this is a weird error
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
    return null;
  }

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
        if (!getAuthorization(authorizer, dataSetOwnerId, dataSetId, currentUserId).isAllowedToWrite()) {
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

  private static Authorization getAuthorization(Authorizer authorizer, String dataSetOwnerId, String dataSetId,
                                                String currentUserId) throws AuthorizationUnavailableException {
    return authorizer.authorizationFor(dataSetOwnerId + "_" + dataSetId, currentUserId);
  }

  public static Response checkAdminAccess(BiFunction<String, String, Boolean> dataSetExists,
                                          Authorizer authorizer, LoggedInUsers loggedInUsers, String authHeader,
                                          String dataSetOwnerId, String dataSetId) {

    if (!dataSetExists.apply(dataSetOwnerId, dataSetId)) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }

    Optional<User> user = loggedInUsers.userFor(authHeader);
    if (!user.isPresent()) {
      return Response.status(Response.Status.UNAUTHORIZED).build();
    }
    String currentUserId = user.get().getPersistentId();

    try {
      if (!getAuthorization(authorizer, dataSetOwnerId, dataSetId, currentUserId).hasAdminAccess()) {
        return Response.status(Response.Status.FORBIDDEN).build();
      }
    } catch (AuthorizationUnavailableException e) {
      return Response.status(Response.Status.FORBIDDEN).build();
    }

    return null;

  }
}
