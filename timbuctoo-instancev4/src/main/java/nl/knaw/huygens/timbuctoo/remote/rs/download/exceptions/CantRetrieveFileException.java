package nl.knaw.huygens.timbuctoo.remote.rs.download.exceptions;

public class CantRetrieveFileException extends Exception {
  public CantRetrieveFileException(int statusCode, String message) {
    super("Error " + Integer.toString(statusCode) + " Can't retrieve specified file. " + message);
  }
}
