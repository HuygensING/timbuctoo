package nl.knaw.huygens.repository.rest.security;

import java.security.Principal;
import java.util.List;

import javax.ws.rs.core.SecurityContext;

import nl.knaw.huygens.repository.model.User;

public class UserSecurityContext implements SecurityContext {
  private Principal principal;
  private User user;
  private List<String> roles;
  private boolean secure;
  private String authenticationScheme;

  public UserSecurityContext(Principal principal, User user) {
    this.principal = principal;
    this.user = user;
    this.roles = user.getRoles();
  }

  @Override
  public Principal getUserPrincipal() {
    return principal;
  }

  @Override
  public boolean isUserInRole(String role) {
    if (roles != null) {
      return roles.contains(role);
    }
    return false;
  }

  @Override
  public boolean isSecure() {
    return secure;
  }

  public void setSecure(boolean secure) {
    this.secure = secure;
  }

  @Override
  public String getAuthenticationScheme() {
    return authenticationScheme;
  }

  public void setAuthenticationScheme(String authenticationScheme) {
    this.authenticationScheme = authenticationScheme;
  }

  public User getUser() {
    return user;
  }

}
