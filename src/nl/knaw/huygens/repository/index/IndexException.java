package nl.knaw.huygens.repository.index;

public class IndexException extends Exception {

  private static final long serialVersionUID = 1L;

  public IndexException() {
    super();
  }

  public IndexException(String message) {
    super(message);
  }

  public IndexException(Throwable cause) {
    super(cause);
  }

  public IndexException(String message, Throwable cause) {
    super(message, cause);
  }

}
