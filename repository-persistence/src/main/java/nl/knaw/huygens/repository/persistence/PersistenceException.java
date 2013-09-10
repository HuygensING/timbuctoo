package nl.knaw.huygens.repository.persistence;

/**
 * Signals that a persistence error has occurred.
 */
public class PersistenceException extends Exception {

  private static final long serialVersionUID = 1L;

  public PersistenceException(String message) {
    super(message);
  }

  public PersistenceException(String message, Throwable cause) {
    super(message, cause);
  }

  public PersistenceException(Throwable cause) {
    super(cause);
  }

}
