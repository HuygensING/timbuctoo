package nl.knaw.huygens.timbuctoo.v5.logprocessing.exceptions;

public class LogStorageFailedException extends Exception {
  private LogStorageFailedException(Throwable cause) {
    super(cause);
  }
}
