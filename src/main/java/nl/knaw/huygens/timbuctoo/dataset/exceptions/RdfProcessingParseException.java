package nl.knaw.huygens.timbuctoo.dataset.exceptions;

public abstract class RdfProcessingParseException extends Exception {
  protected RdfProcessingParseException(String message) {
    super(message);
  }
}
