package nl.knaw.huygens.timbuctoo.security;

public class UserCreationException extends Exception {

  public UserCreationException(Exception exception) {
    super(exception);
  }

  public UserCreationException(String message) {
    super(message);
  }
}
