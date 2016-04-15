package nl.knaw.huygens.timbuctoo.security;

import java.util.Optional;

public class VreAuthorizationCollection {
  public VreAuthorization addAuthorizationFor(String vreId, String userId) throws AuthorizationUnavailableException {
    throw new UnsupportedOperationException("Not implemented yet");
  }

  public Optional<VreAuthorization> authorizationFor(String vreId, String userId)
    throws AuthorizationUnavailableException {
    throw new UnsupportedOperationException("Not implemented yet");
  }
}
