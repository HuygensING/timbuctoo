package nl.knaw.huygens.timbuctoo.storage.neo4j;

public class PropertyNotIndexedException extends RuntimeException {
  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  private PropertyNotIndexedException(String message) {
    super(message);
  }

  public static PropertyNotIndexedException propertyHasNoIndex(Class<?> type, String propertyName) {
    return new PropertyNotIndexedException(String.format("\"%s\" of \"%s\" cannot be searched.", propertyName, type));
  }
}
