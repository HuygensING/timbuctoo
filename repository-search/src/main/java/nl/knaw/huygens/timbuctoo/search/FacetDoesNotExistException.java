package nl.knaw.huygens.timbuctoo.search;

public class FacetDoesNotExistException extends Exception {

  private static final long serialVersionUID = 1L;

  public FacetDoesNotExistException(String message) {
    super(message);
  }

}
