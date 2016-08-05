package nl.knaw.huygens.timbuctoo.server.security;

import nl.knaw.huygens.timbuctoo.crud.Authorization;
import nl.knaw.huygens.timbuctoo.security.AuthorizationUnavailableException;
import nl.knaw.huygens.timbuctoo.security.Authorizer;
import nl.knaw.huygens.timbuctoo.security.LoggedInUserStore;
import nl.knaw.huygens.timbuctoo.security.User;
import nl.knaw.huygens.timbuctoo.server.endpoints.v2.bulkupload.RawCollection;
import org.slf4j.LoggerFactory;

import java.util.Optional;

import static nl.knaw.huygens.timbuctoo.server.security.UserPermissionChecker.UserPermission.ALLOWED_TO_WRITE;
import static nl.knaw.huygens.timbuctoo.server.security.UserPermissionChecker.UserPermission.NO_PERMISSION;
import static nl.knaw.huygens.timbuctoo.server.security.UserPermissionChecker.UserPermission.UNKNOWN_USER;

public class UserPermissionChecker {
  private final LoggedInUserStore loggedInUserStore;
  private final Authorizer authorizer;

  public UserPermissionChecker(LoggedInUserStore loggedInUserStore, Authorizer authorizer) {
    this.loggedInUserStore = loggedInUserStore;
    this.authorizer = authorizer;
  }

  public UserPermission check(String vreName, String authorizationHeader) {
    Optional<User> user = loggedInUserStore.userFor(authorizationHeader);

    if (!user.isPresent()) {
      return UNKNOWN_USER;
    }

    try {
      Authorization authorization = authorizer.authorizationFor(vreName, user.get().getId());
      if (authorization.isAllowedToWrite()) {
        return ALLOWED_TO_WRITE;
      } else {
        return NO_PERMISSION;
      }

    } catch (AuthorizationUnavailableException e) {
      LoggerFactory.getLogger(RawCollection.class).error("Authorization cannot be read.", e);
      return UNKNOWN_USER;
    }
  }

  public enum UserPermission {
    UNKNOWN_USER,
    NO_PERMISSION,
    ALLOWED_TO_WRITE
  }
}
