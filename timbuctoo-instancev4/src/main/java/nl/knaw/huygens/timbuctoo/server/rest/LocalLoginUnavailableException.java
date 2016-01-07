package nl.knaw.huygens.timbuctoo.server.rest;

public class LocalLoginUnavailableException extends Exception{

  public LocalLoginUnavailableException(String message) {
    super(message);
  }
}
