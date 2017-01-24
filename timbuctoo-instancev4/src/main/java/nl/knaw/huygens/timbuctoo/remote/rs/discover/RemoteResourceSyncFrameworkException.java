package nl.knaw.huygens.timbuctoo.remote.rs.discover;

/**
 * Signals that a remote source does not comply to the Resourcesync Framework specification.
 *
 * @see <a href="http://www.openarchives.org/rs/1.0/resourcesync">
 *   http://www.openarchives.org/rs/1.0/resourcesync</a>
 */
public class RemoteResourceSyncFrameworkException extends Exception {

  public RemoteResourceSyncFrameworkException() {
  }

  public RemoteResourceSyncFrameworkException(String messaqge) {
    super(messaqge);
  }

  public RemoteResourceSyncFrameworkException(String message, Throwable cause) {
    super(message, cause);
  }

  public RemoteResourceSyncFrameworkException(Throwable cause) {
    super(cause);
  }

}
