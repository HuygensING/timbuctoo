package nl.knaw.huygens.repository.util;

public class MarginalScholarshipException extends RuntimeException {

  private static final long serialVersionUID = -5175017807899393836L;

  public MarginalScholarshipException(Throwable cause) {
    super(cause);
  }

  public MarginalScholarshipException() {
    super();
  }

  public MarginalScholarshipException(String msg) {
    super(msg);
  }

}
