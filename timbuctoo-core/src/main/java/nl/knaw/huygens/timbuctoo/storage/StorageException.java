package nl.knaw.huygens.timbuctoo.storage;

import java.io.IOException;

public class StorageException extends IOException {

  private static final long serialVersionUID = 1L;

  public StorageException() {
    super();
  }

  public StorageException(String message) {
    super(message);
  }

  public StorageException(Throwable cause) {
    super(cause);
  }

  public StorageException(String message, Throwable cause) {
    super(message, cause);
  }

}
