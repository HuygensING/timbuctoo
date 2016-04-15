package nl.knaw.huygens.timbuctoo.security;

import nl.knaw.huygens.timbuctoo.crud.Authorization;
import nl.knaw.huygens.timbuctoo.model.vre.Collection;

import java.util.Optional;

public class JsonBasedAuthorizer implements Authorizer {

  private VreAuthorizationCollection authorizationCollection;

  public JsonBasedAuthorizer(VreAuthorizationCollection authorizationCollection) {
    this.authorizationCollection = authorizationCollection;
  }

  @Override
  public Authorization authorizationFor(Collection collection, String userId)
    throws AuthorizationUnavailableException {
    Optional<VreAuthorization> vreAuthorization =
      authorizationCollection.authorizationFor(collection.getVre().getVreName(), userId);

    if (vreAuthorization.isPresent()) {
      return vreAuthorization.get();
    }

    return
      authorizationCollection.addAuthorizationFor(collection.getVre().getVreName(), userId);

  }
}
