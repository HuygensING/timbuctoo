package nl.knaw.huygens.timbuctoo.security;

/*
 * #%L
 * Timbuctoo REST api
 * =======
 * Copyright (C) 2012 - 2015 Huygens ING
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

import static nl.knaw.huygens.timbuctoo.security.LocalLoggedInUsers.LOCAL_SESSION_KEY_PREFIX;
import nl.knaw.huygens.security.client.AuthenticationHandler;
import nl.knaw.huygens.security.client.HuygensAuthenticationHandler;
import nl.knaw.huygens.security.client.UnauthorizedException;
import nl.knaw.huygens.security.client.model.SecurityInformation;

import org.apache.commons.lang3.StringUtils;

import com.google.inject.Inject;

public class TimbuctooAuthenticationHandler implements AuthenticationHandler {

  private final LocalLoggedInUsers localLoggedInUsers;
  // The AuthenticationHandler for the security server.
  private final HuygensAuthenticationHandler huygensAuthenticationHandler;

  @Inject
  public TimbuctooAuthenticationHandler(LocalLoggedInUsers localLoggedInUsers, HuygensAuthenticationHandler huygensAuthenticationHandler) {
    this.localLoggedInUsers = localLoggedInUsers;
    this.huygensAuthenticationHandler = huygensAuthenticationHandler;
  }

  @Override
  public SecurityInformation getSecurityInformation(String sessionId) throws UnauthorizedException {
    if (isLocalSessionToken(sessionId)) {
      return localLoggedInUsers.getSecurityInformation(sessionId);
    }

    return huygensAuthenticationHandler.getSecurityInformation(sessionId);
  }

  private boolean isLocalSessionToken(String sessionId) {
    return StringUtils.startsWith(sessionId, LOCAL_SESSION_KEY_PREFIX);
  }

}
