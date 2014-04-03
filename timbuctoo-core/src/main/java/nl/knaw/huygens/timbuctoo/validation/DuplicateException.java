package nl.knaw.huygens.timbuctoo.validation;

public class DuplicateException extends ValidationException {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  private String duplicateId;

  public DuplicateException(String duplicateId) {
    super();
    this.duplicateId = duplicateId;
  }

  public String getDuplicateId() {
    return duplicateId;
  }

}
