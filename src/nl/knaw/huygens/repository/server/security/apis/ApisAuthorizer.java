package nl.knaw.huygens.repository.server.security.apis;

import java.security.Principal;
import java.util.Collection;

import javax.ws.rs.core.SecurityContext;

public class ApisAuthorizer implements SecurityContext {
  private AuthenticatedPrincipal principal;
  private Collection<String> roles;

  public ApisAuthorizer() {

  }

  public ApisAuthorizer(AuthenticatedPrincipal principal) {
    this.setPrincipal(principal);
  }

  @Override
  public Principal getUserPrincipal() {
    return this.getPrincipal();
  }

  public AuthenticatedPrincipal getPrincipal() {
    return principal;
  }

  public void setPrincipal(AuthenticatedPrincipal principal) {
    if (principal == null) {
      throw new IllegalArgumentException("principal cannot be null");
    }

    this.principal = principal;
    this.roles = principal.getRoles();
  }

  @Override
  public boolean isUserInRole(String role) {
    boolean isUserInRole = false;

    if (this.roles != null) {
      isUserInRole = roles.contains(role);
    }

    return isUserInRole;
  }

  @Override
  public boolean isSecure() {
    // TODO Auto-generated method stub
    return true;
  }

  @Override
  public String getAuthenticationScheme() {
    // TODO Auto-generated method stub
    return null;
  }

}
