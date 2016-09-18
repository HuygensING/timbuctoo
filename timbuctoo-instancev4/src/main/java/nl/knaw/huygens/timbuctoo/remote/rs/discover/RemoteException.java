package nl.knaw.huygens.timbuctoo.remote.rs.discover;


import java.net.URI;

public class RemoteException extends Exception {

  private int statusCode;
  private URI uri;

  public RemoteException() {
  }

  public RemoteException(String message) {
    super(message);
  }

  public RemoteException(int statusCode, String message) {
    super(statusCode + " " + message);
    this.statusCode = statusCode;
  }

  public RemoteException(int statusCode, String message, URI uri) {
    super(statusCode + " " + message + ": " + uri.toString());
    this.statusCode = statusCode;
    this.uri = uri;
  }

  public int getStatusCode() {
    return statusCode;
  }

  public URI getUri() {
    return uri;
  }
}
