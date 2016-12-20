package nl.knaw.huygens.timbuctoo.security.dataaccess.azure;

public class AzureAccessNotPossibleException extends Exception {
  public AzureAccessNotPossibleException(String message) {
    super(message);
  }
}
