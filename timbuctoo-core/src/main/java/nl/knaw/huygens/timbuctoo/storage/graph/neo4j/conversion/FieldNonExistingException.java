package nl.knaw.huygens.timbuctoo.storage.graph.neo4j.conversion;

class FieldNonExistingException extends RuntimeException {
  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  public FieldNonExistingException(Class<?> type, String fieldName) {
    super(String.format("\"%s\" has no field with name \"%s\"", type, fieldName));
  }
}
