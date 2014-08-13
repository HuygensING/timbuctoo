package nl.knaw.huygens.timbuctoo.search;

public class InvalidRangeFacetException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  public InvalidRangeFacetException(String message) {
    super(message);
  }
}
