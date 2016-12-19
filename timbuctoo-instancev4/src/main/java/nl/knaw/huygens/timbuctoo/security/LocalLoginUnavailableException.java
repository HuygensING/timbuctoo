package nl.knaw.huygens.timbuctoo.security;

public class LocalLoginUnavailableException extends Exception {

  //These exceptions might be shown to the client.
  //therefore you should log a full exception and then throw one with only limited information
  public LocalLoginUnavailableException(String message) {
    super(message);
  }
}
