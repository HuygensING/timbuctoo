package nl.knaw.huygens.repository.server.security;

import java.security.Principal;

import javax.ws.rs.core.SecurityContext;

/**
 * A class used for development and testing purposes only. It will bypass the oauth security.
 * @author martijnm
 *
 */
public class NoSecurityOAuthAuthorizationServerConnector implements OAuthAuthorizationServerConnector {

  @Override
  public SecurityContext authenticate(String oAuthUserKey) {
    return new SecurityContext() {

      @Override
      public boolean isUserInRole(String role) {
        return true;
      }

      @Override
      public boolean isSecure() {
        return false;
      }

      @Override
      public Principal getUserPrincipal() {
        return null;
      }

      @Override
      public String getAuthenticationScheme() {
        return null;
      }
    };
  }

}
