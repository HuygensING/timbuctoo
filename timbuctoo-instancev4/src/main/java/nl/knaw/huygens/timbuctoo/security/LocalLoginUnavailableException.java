package nl.knaw.huygens.timbuctoo.security;

public class LocalLoginUnavailableException extends Exception{

  public LocalLoginUnavailableException(String message) {
    super(message);
  }
}
