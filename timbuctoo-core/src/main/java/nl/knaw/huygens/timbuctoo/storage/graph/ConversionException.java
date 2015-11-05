package nl.knaw.huygens.timbuctoo.storage.graph;

import nl.knaw.huygens.timbuctoo.storage.StorageException;

public class ConversionException extends StorageException {

  public ConversionException(Exception cause) {
    super(cause);
  }

  public ConversionException() {}

  public ConversionException(String message) {
    super(message);
  }

  /**
   * 
   */
  private static final long serialVersionUID = 1L;

}
