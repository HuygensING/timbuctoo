package nl.knaw.huygens.timbuctoo.storage.neo4j;

public class PropertyNotSearchableException extends RuntimeException {
  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  private PropertyNotSearchableException(String message) {
    super(message);
  }

  public static PropertyNotSearchableException propertyHasNoIndex(Class<?> type, String propertyName) {
    return new PropertyNotSearchableException(String.format("\"%s\" of \"%s\" cannot be searched.", propertyName, type));
  }
}
