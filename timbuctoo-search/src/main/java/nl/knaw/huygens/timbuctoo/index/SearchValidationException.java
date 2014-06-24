package nl.knaw.huygens.timbuctoo.index;

/**
 * A wrapper for the validation exceptions thrown when searching.
 */
public class SearchValidationException extends Exception {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  public SearchValidationException(Exception e) {
    super(e);
  }

}
