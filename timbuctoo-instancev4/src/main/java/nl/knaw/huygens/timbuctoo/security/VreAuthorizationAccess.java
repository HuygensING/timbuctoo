package nl.knaw.huygens.timbuctoo.security;

import java.util.Optional;

public interface VreAuthorizationAccess {
  VreAuthorization getOrCreateAuthorization(String vreId, String userId, String userRole)
    throws AuthorizationUnavailableException;

  Optional<VreAuthorization> getAuthorization(String vreId, String userId)
      throws AuthorizationUnavailableException;
}
