package nl.knaw.huygens.timbuctoo.security;

import nl.knaw.huygens.timbuctoo.security.exceptions.AuthorizationCreationException;

public interface VreAuthorizationCreator {
  void createAuthorization(String vreId, String userId, String vreRole) throws AuthorizationCreationException;
}
