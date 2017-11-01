package nl.knaw.huygens.timbuctoo.server.security;

import nl.knaw.huygens.timbuctoo.security.LoggedInUsers;
import nl.knaw.huygens.timbuctoo.security.dto.User;
import nl.knaw.huygens.timbuctoo.server.endpoints.v2.bulkupload.RawCollection;
import nl.knaw.huygens.timbuctoo.v5.security.PermissionFetcher;
import nl.knaw.huygens.timbuctoo.v5.security.dto.Permission;
import nl.knaw.huygens.timbuctoo.v5.security.exceptions.PermissionFetchingException;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response;
import java.util.Optional;

import static nl.knaw.huygens.timbuctoo.server.security.UserPermissionChecker.UserPermission.ALLOWED_TO_WRITE;
import static nl.knaw.huygens.timbuctoo.server.security.UserPermissionChecker.UserPermission.NO_PERMISSION;
import static nl.knaw.huygens.timbuctoo.server.security.UserPermissionChecker.UserPermission.UNKNOWN_USER;

public class UserPermissionChecker {
  private final LoggedInUsers loggedInUsers;
  private final PermissionFetcher permissionFetcher;

  public UserPermissionChecker(LoggedInUsers loggedInUsers, PermissionFetcher permissionFetcher) {
    this.loggedInUsers = loggedInUsers;
    this.permissionFetcher = permissionFetcher;
  }

  public UserPermission check(String vreName, String authorizationHeader) {
    Optional<User> user = loggedInUsers.userFor(authorizationHeader);

    if (!user.isPresent()) {
      return UNKNOWN_USER;
    }

    try {
      if (permissionFetcher.getPermissions(user.get().getId(), vreName).contains(Permission.WRITE)) {
        return ALLOWED_TO_WRITE;
      } else {
        return NO_PERMISSION;
      }
    } catch (PermissionFetchingException e) {
      LoggerFactory.getLogger(RawCollection.class).error("Authorization cannot be read.", e);
      return UNKNOWN_USER;
    }
  }

  public Optional<User> getUserFor(String authorizationHeader) {
    return loggedInUsers.userFor(authorizationHeader);
  }

  public Optional<Response> checkPermissionWithResponse(String vreName, String authorizationHeader) {
    UserPermission permission = check(vreName, authorizationHeader);
    switch (permission) {
      case UNKNOWN_USER:
        return Optional.of(Response.status(Response.Status.UNAUTHORIZED).build());
      case NO_PERMISSION:
        return Optional.of(Response.status(Response.Status.FORBIDDEN).build());
      case ALLOWED_TO_WRITE:
        break;
      default:
        return Optional.of(Response.status(Response.Status.UNAUTHORIZED).build());
    }
    return Optional.empty();
  }

  public enum UserPermission {
    UNKNOWN_USER,
    NO_PERMISSION,
    ALLOWED_TO_WRITE
  }
}
