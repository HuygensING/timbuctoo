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

import nl.knaw.huygens.security.client.UnauthorizedException;
import nl.knaw.huygens.timbuctoo.model.Login;

import org.apache.commons.lang3.StringUtils;

import com.google.inject.Inject;

public class BasicAuthenticationHandler {

  private final LocalAuthenticator localAuthenticator;
  private final LocalLoggedInUsers localLoggedInUsers;

  @Inject
  public BasicAuthenticationHandler(LocalAuthenticator localAuthenticator, LocalLoggedInUsers localLoggedInUsers) {
    this.localAuthenticator = localAuthenticator;
    this.localLoggedInUsers = localLoggedInUsers;
  }

  /**
   * Creates the authentication token if the user is known.
   * @param authenticationString a base64 encoded string that contains user name and pass word.
   * @return the authentication token if the user is known.
   * @throws IllegalArgumentException when {@code authenticationString} is not {@code HTTP_BASIC}.
   * @throws UnAuthorizedException when the user name and password combination is unknown.
   */
  public String authenticate(String authenticationString) throws UnauthorizedException {
    String normalizedAuthString = normalize(authenticationString);

    Login login = localAuthenticator.authenticate(normalizedAuthString);
    String authorizationToken = localLoggedInUsers.add(login);

    return authorizationToken;
  }

  private String normalize(String authenticationString) {
    if (!StringUtils.startsWithAny(authenticationString, "basic", "Basic")) {
      throw new IllegalArgumentException("Authentication is not basic.");
    }

    return authenticationString.replaceAll("^[bB]asic ", "");
  }
}
