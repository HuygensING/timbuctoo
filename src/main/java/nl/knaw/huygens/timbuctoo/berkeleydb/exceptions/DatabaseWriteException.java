package nl.knaw.huygens.timbuctoo.berkeleydb.exceptions;

public class DatabaseWriteException extends Exception {
  public DatabaseWriteException(Exception cause) {
    super(cause);
  }
}
