package nl.knaw.huygens.timbuctoo.search;

public class NoSuchFacetException extends Exception {

  private static final long serialVersionUID = 1L;

  public NoSuchFacetException(String message) {
    super(message);
  }

}
