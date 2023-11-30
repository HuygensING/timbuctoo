package nl.knaw.huygens.timbuctoo.security.exceptions;

public class UserValidationException extends Exception {
  public UserValidationException(Throwable throwable) {
    super(throwable);
  }
}
