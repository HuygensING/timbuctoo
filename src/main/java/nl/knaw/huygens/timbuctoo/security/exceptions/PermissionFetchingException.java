package nl.knaw.huygens.timbuctoo.security.exceptions;

public class PermissionFetchingException extends Exception {
  public PermissionFetchingException(Throwable throwable) {
    super(throwable);
  }

  public PermissionFetchingException(String message) {
    super(message);
  }
}
