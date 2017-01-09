package nl.knaw.huygens.timbuctoo.security;

import nl.knaw.huygens.timbuctoo.core.dto.dataset.Collection;
import nl.knaw.huygens.timbuctoo.crud.Authorization;
import nl.knaw.huygens.timbuctoo.security.dto.VreAuthorization;
import nl.knaw.huygens.timbuctoo.security.exceptions.AuthorizationCreationException;
import nl.knaw.huygens.timbuctoo.security.exceptions.AuthorizationUnavailableException;

import java.util.Optional;

import static nl.knaw.huygens.timbuctoo.security.UserRoles.UNVERIFIED_USER_ROLE;

public class JsonBasedAuthorizer implements Authorizer, VreAuthorizationCreator {

  private VreAuthorizationAccess authorizationAccess;

  public JsonBasedAuthorizer(VreAuthorizationAccess authorizationAccess) {
    this.authorizationAccess = authorizationAccess;
  }

  @Override
  public Authorization authorizationFor(Collection collection, String userId) throws AuthorizationUnavailableException {
    return authorizationFor(collection.getVreName(), userId);
  }

  @Override
  public Authorization authorizationFor(String vreId, String userId) throws AuthorizationUnavailableException {
    //FIXME: based on a cursory glance at the code it seems that this get and the following if are redundant
    Optional<VreAuthorization> vreAuthorization = authorizationAccess.getAuthorization(vreId, userId);

    if (vreAuthorization.isPresent()) {
      return vreAuthorization.get();
    }

    return authorizationAccess.getOrCreateAuthorization(vreId, userId, UNVERIFIED_USER_ROLE);
  }

  @Override
  public void createAuthorization(String vreId, String userId, String vreRole) throws AuthorizationCreationException {
    try {
      authorizationAccess.getOrCreateAuthorization(vreId, userId, vreRole);
    } catch (AuthorizationUnavailableException e) {
      throw new AuthorizationCreationException(e);
    }
  }
}
