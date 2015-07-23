package nl.knaw.huygens.timbuctoo.index;

public class RawSearchUnavailableException extends Exception {
  public RawSearchUnavailableException(String name) {
    super(String.format("Index with name \"%s\" does not support raw search", name));
  }

  private static final long serialVersionUID = 1L;

}
