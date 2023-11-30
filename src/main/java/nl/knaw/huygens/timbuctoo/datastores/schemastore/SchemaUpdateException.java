package nl.knaw.huygens.timbuctoo.datastores.schemastore;

public class SchemaUpdateException extends Exception {
  public SchemaUpdateException(Exception exception) {
    super(exception);
  }
}
