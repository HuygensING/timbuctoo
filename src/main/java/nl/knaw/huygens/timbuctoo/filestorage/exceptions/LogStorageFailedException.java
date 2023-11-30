package nl.knaw.huygens.timbuctoo.filestorage.exceptions;

public class LogStorageFailedException extends Exception {
  public LogStorageFailedException(Throwable cause) {
    super(cause);
  }

  public LogStorageFailedException(String message) {
    super(message);
  }
}
