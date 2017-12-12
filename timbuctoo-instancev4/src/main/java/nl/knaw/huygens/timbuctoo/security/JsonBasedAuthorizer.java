package nl.knaw.huygens.timbuctoo.security;

import nl.knaw.huygens.timbuctoo.security.dto.Authorization;
import nl.knaw.huygens.timbuctoo.security.dataaccess.VreAuthorizationAccess;
import nl.knaw.huygens.timbuctoo.v5.security.dto.User;
import nl.knaw.huygens.timbuctoo.security.dto.VreAuthorization;
import nl.knaw.huygens.timbuctoo.v5.security.exceptions.AuthorizationCreationException;
import nl.knaw.huygens.timbuctoo.security.exceptions.AuthorizationException;
import nl.knaw.huygens.timbuctoo.v5.security.exceptions.AuthorizationUnavailableException;

import java.util.Optional;

import static nl.knaw.huygens.timbuctoo.security.dto.UserRoles.ADMIN_ROLE;
import static nl.knaw.huygens.timbuctoo.security.dto.UserRoles.UNVERIFIED_USER_ROLE;

public class JsonBasedAuthorizer implements Authorizer, VreAuthorizationCrud {

  private VreAuthorizationAccess authorizationAccess;

  public JsonBasedAuthorizer(VreAuthorizationAccess authorizationAccess) {
    this.authorizationAccess = authorizationAccess;
  }

  @Override
  public Authorization  authorizationFor(String vreId, String userId) throws AuthorizationUnavailableException {
    //FIXME: based on a cursory glance at the code it seems that this get and the following if are redundant
    Optional<VreAuthorization> vreAuthorization = authorizationAccess.getAuthorization(vreId, userId);

    if (vreAuthorization.isPresent()) {
      return vreAuthorization.get();
    }

    return authorizationAccess.getOrCreateAuthorization(vreId, userId, UNVERIFIED_USER_ROLE);
  }

  @Override
  public Optional<VreAuthorization> getAuthorization(String vreId, String userId)
    throws AuthorizationUnavailableException {
    return authorizationAccess.getAuthorization(vreId, userId);
  }


  @Override
  public void createAuthorization(String vreId, String userId, String vreRole) throws AuthorizationCreationException {
    try {
      authorizationAccess.getOrCreateAuthorization(vreId, userId, vreRole);
    } catch (AuthorizationUnavailableException e) {
      throw new AuthorizationCreationException(e);
    }
  }


  @Override
  public void deleteVreAuthorizations(String vreId, User user)
    throws AuthorizationException, AuthorizationUnavailableException {
    Optional<VreAuthorization> authorization = authorizationAccess.getAuthorization(vreId, user.getId());

    if (!authorization.isPresent() || !authorization.get().getRoles().contains(ADMIN_ROLE)) {
      throw new AuthorizationException(String.format(
        "User with id '%s' is not allowed to remove the authorizations of vre with id '%s",
        user,
        vreId
      ));
    }

    authorizationAccess.deleteVreAuthorizations(vreId);
  }
}
