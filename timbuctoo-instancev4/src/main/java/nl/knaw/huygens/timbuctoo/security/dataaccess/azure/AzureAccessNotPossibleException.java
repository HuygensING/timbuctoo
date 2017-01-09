package nl.knaw.huygens.timbuctoo.security.dataaccess.azure;

import nl.knaw.huygens.timbuctoo.security.dataaccess.AccessNotPossibleException;

public class AzureAccessNotPossibleException extends AccessNotPossibleException {
  public AzureAccessNotPossibleException(String message) {
    super(message);
  }
}
