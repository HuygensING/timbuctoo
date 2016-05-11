package nl.knaw.huygens.timbuctoo.security;

public class AuthorizationCreationException extends Exception {
  public AuthorizationCreationException(String message) {
    super(message);
  }

  public AuthorizationCreationException(AuthorizationUnavailableException cause) {
    super(cause);
  }
}
