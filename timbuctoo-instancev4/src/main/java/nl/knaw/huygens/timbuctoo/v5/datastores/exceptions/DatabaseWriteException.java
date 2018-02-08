package nl.knaw.huygens.timbuctoo.v5.datastores.exceptions;

public class DatabaseWriteException extends Exception {
  public DatabaseWriteException(Exception cause) {
    super(cause);
  }
}
