package nl.knaw.huygens.timbuctoo.security.dataaccess;

public class AccessNotPossibleException extends Exception {
  public AccessNotPossibleException(String message) {
    super(message);
  }
}
