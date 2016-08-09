package nl.knaw.huygens.timbuctoo.rml;

import java.util.Map;

public class Row {
  private final Map<String, Object> data;
  private final ErrorHandler errorHandler;

  public Row(Map<String, Object> data, ErrorHandler errorHandler) {
    this.data = data;
    this.errorHandler = errorHandler;
  }

  public Row(Map<String, Object> data) {
    this(data, new LoggingErrorHandler());
  }

  public Object get(String key) {
    return data.get(key);
  }

  public void handleLinkError(String childField, String parentCollection, String parentField) {
    errorHandler.handleLink(data, childField, parentCollection, parentField);
  }
}
