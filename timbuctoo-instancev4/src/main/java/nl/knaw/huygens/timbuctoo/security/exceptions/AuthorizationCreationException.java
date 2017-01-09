package nl.knaw.huygens.timbuctoo.security.exceptions;

public class AuthorizationCreationException extends Exception {
  public AuthorizationCreationException(String message) {
    super(message);
  }

  public AuthorizationCreationException(AuthorizationUnavailableException cause) {
    super(cause);
  }
}
