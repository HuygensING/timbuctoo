package nl.knaw.huygens.repository.util;

public class RepositoryException extends RuntimeException {

  private static final long serialVersionUID = -5175017807899393836L;

  public RepositoryException(Throwable cause) {
    super(cause);
  }

  public RepositoryException() {
    super();
  }

  public RepositoryException(String msg) {
    super(msg);
  }

}
