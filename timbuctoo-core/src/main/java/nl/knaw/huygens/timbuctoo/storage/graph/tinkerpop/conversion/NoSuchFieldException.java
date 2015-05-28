package nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.conversion;

class NoSuchFieldException extends RuntimeException {
  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  public NoSuchFieldException(Class<?> type, String fieldName) {
    super(String.format("\"%s\" has no field with name \"%s\"", type, fieldName));
  }
}
