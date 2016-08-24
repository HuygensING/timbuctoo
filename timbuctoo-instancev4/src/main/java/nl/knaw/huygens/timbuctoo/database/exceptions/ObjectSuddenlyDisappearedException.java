package nl.knaw.huygens.timbuctoo.database.exceptions;

public class ObjectSuddenlyDisappearedException extends RuntimeException {
  public ObjectSuddenlyDisappearedException(String message) {
    super(message);
  }
}
