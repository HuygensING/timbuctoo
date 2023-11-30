package nl.knaw.huygens.timbuctoo.security.openidconnect;

public class OpenIdConnectException extends Exception {
  public OpenIdConnectException(String message) {
    super(message);
  }

  public OpenIdConnectException(String message, Throwable cause) {
    super(message, cause);
  }
}
