package nl.knaw.huygens.timbuctoo.v5.datastores.schema;

public class SchemaUpdateException extends Exception {
  public SchemaUpdateException(Exception exception) {
    super(exception);
  }
}
