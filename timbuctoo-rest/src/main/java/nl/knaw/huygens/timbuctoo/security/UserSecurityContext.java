package nl.knaw.huygens.timbuctoo.security;

/*
 * #%L
 * Timbuctoo REST api
 * =======
 * Copyright (C) 2012 - 2013 Huygens ING
 * =======
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import java.security.Principal;

import javax.ws.rs.core.SecurityContext;

import nl.knaw.huygens.timbuctoo.model.User;

public class UserSecurityContext implements SecurityContext {

  private Principal principal;
  private User user;
  private boolean secure;
  private String authenticationScheme;

  public UserSecurityContext(Principal principal, User user) {
    this.principal = principal;
    this.user = user;
  }

  @Override
  public Principal getUserPrincipal() {
    return principal;
  }

  @Override
  public boolean isUserInRole(String role) {
    if (user.getRoles() != null) {
      return user.getRoles().contains(role);
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
