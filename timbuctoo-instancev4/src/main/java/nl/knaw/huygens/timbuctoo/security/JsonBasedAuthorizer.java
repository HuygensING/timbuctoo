package nl.knaw.huygens.timbuctoo.security;

import nl.knaw.huygens.timbuctoo.crud.Authorization;
import nl.knaw.huygens.timbuctoo.database.dto.dataset.Collection;

import java.nio.file.Path;
import java.util.Optional;

import static nl.knaw.huygens.timbuctoo.security.UserRoles.UNVERIFIED_USER_ROLE;

public class JsonBasedAuthorizer implements Authorizer, VreAuthorizationCreator {

  private VreAuthorizationCollection authorizationCollection;

  public JsonBasedAuthorizer(Path pathToAuthorizations) {
    this.authorizationCollection = new VreAuthorizationCollection(pathToAuthorizations);
  }

  public JsonBasedAuthorizer(VreAuthorizationCollection authorizationCollection) {
    this.authorizationCollection = authorizationCollection;
  }

  @Override
  public Authorization authorizationFor(Collection collection, String userId) throws AuthorizationUnavailableException {
    return authorizationFor(collection.getVre().getVreName(), userId);
  }

  @Override
  public Authorization authorizationFor(String vreId, String userId) throws AuthorizationUnavailableException {
    Optional<VreAuthorization> vreAuthorization = authorizationCollection.authorizationFor(vreId, userId);

    if (vreAuthorization.isPresent()) {
      return vreAuthorization.get();
    }

    return authorizationCollection.addAuthorizationFor(vreId, userId, UNVERIFIED_USER_ROLE);
  }

  @Override
  public void createAuthorization(String vreId, String userId, String vreRole) throws AuthorizationCreationException {
    try {
      authorizationCollection.addAuthorizationFor(vreId, userId, vreRole);
    } catch (AuthorizationUnavailableException e) {
      throw new AuthorizationCreationException(e);
    }
  }
}
