package nl.knaw.huygens.timbuctoo.storage.neo4j;

import nl.knaw.huygens.timbuctoo.storage.StorageException;

public class ConversionException extends StorageException {

  public ConversionException(Exception cause) {
    super(cause);
  }

  public ConversionException() {}

  /**
   * 
   */
  private static final long serialVersionUID = 1L;

}
