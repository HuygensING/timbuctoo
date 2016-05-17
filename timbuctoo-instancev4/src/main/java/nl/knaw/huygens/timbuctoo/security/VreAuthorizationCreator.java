package nl.knaw.huygens.timbuctoo.security;

public interface VreAuthorizationCreator {
  void createAuthorization(String vreId, String userId, String vreRole) throws AuthorizationCreationException;
}
