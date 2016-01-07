package nl.knaw.huygens.timbuctoo.server.rest;

public class InvalidAuthorizationHeaderException extends Exception {
  InvalidAuthorizationHeaderException(String message) {
    super(message);
  }

}
