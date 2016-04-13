package nl.knaw.huygens.timbuctoo.security;

public class AuthorizationException extends Exception {
  public AuthorizationException(String message) {
    super(message);
  }
}
