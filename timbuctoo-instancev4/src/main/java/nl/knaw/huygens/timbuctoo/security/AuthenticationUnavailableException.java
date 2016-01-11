package nl.knaw.huygens.timbuctoo.security;

public class AuthenticationUnavailableException extends Exception {

  public AuthenticationUnavailableException(String message) {
    super(message);
  }
}
