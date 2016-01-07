package nl.knaw.huygens.timbuctoo.server.rest;

public class InvalidAuthorizationHeaderException extends Exception {
  private InvalidAuthorizationHeaderException(String message) {
    super(message);
  }

  public static InvalidAuthorizationHeaderException notBasicAuthorizationHeader() {
    return new InvalidAuthorizationHeaderException("Header must start with the word 'Basic' followed by 1 space.");
  }

  public static InvalidAuthorizationHeaderException invalidBasicAuthValue(String decodedAuth) {
    String message = String.format(
      "The username and password should be seperated by a ':' but that character was not found in '%s'.",
      decodedAuth);
    return new InvalidAuthorizationHeaderException(message);
  }

  /**
   * Adds the message of the exception argument to the InvalidAuthorizationHeaderException.
   */
  public static InvalidAuthorizationHeaderException wrap(Exception exception) {
    return new InvalidAuthorizationHeaderException(exception.getMessage());
  }
}
