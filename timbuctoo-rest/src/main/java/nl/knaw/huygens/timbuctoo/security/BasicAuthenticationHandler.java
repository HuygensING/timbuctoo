package nl.knaw.huygens.timbuctoo.security;

import nl.knaw.huygens.security.client.UnauthorizedException;

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

    String persistentId = localAuthenticator.authenticate(normalizedAuthString);
    String authorizationToken = localLoggedInUsers.add(persistentId);

    return authorizationToken;
  }

  private String normalize(String authenticationString) {
    if (!StringUtils.startsWithAny(authenticationString, "basic", "Basic")) {
      throw new IllegalArgumentException("Authentication is not basic.");
    }

    return authenticationString.replaceAll("^[bB]asic ", "");
  }
}
