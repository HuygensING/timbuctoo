package nl.knaw.huygens.timbuctoo.rml;

import java.util.Map;

public class ThrowingErrorHandler implements ErrorHandler {
  @Override
  public void linkError(Map<String, Object> rowData, String childField, String parentCollection, String parentField) {
    throw new RuntimeException("Linking failed!");
  }

  @Override
  public void valueGenerateFailed(String key, String message) {
    throw new RuntimeException(key + ": " + message);
  }
}
