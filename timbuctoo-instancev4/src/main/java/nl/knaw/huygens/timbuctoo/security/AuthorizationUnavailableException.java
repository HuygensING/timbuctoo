package nl.knaw.huygens.timbuctoo.security;

public class AuthorizationUnavailableException extends Exception {
  public AuthorizationUnavailableException(String message) {
    super(message);
  }

  public AuthorizationUnavailableException() {
    super();
  }
}
