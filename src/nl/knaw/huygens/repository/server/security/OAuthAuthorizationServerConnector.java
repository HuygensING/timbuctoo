package nl.knaw.huygens.repository.server.security;

import javax.ws.rs.core.SecurityContext;

public interface OAuthAuthorizationServerConnector {
  public SecurityContext authenticate(String oAuthUserKey);
}
