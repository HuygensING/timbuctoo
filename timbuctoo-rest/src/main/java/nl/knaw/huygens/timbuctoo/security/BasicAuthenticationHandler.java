package nl.knaw.huygens.timbuctoo.security;

import nl.knaw.huygens.security.client.UnauthorizedException;

public class BasicAuthenticationHandler {

  /**
   * Creates the authentication token if the user is known.
   * @param authenticationString a base64 encoded string that contains user name and pass word.
   * @return the authentication token is the user is known.
   * @throws IllegalArgumentException when {@code authenticationString} is not {@code HTTP_BASIC}.
   * @throws UnAuthorizedException when the user name and password combination is unknown.
   */
  public String authenticate(String authenticationString) throws UnauthorizedException {

    return null;
  }

}
