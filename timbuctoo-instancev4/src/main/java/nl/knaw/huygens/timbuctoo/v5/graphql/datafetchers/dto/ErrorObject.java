package nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.dto;

import com.fasterxml.jackson.databind.node.ObjectNode;

import static nl.knaw.huygens.timbuctoo.util.JsonBuilder.jsnO;

public class ErrorObject {
  private final String dateStamp;
  private final String file;
  private final String method;
  private final String message;
  private final String error;

  private ErrorObject(String dateStamp, String file, String method, String message, String error) {
    this.dateStamp = dateStamp;
    this.file = file;
    this.method = method;
    this.message = message;
    this.error = error;
  }


  public static ErrorObject parse(String errorString) {
    String dateStamp = null;
    String file = null;
    String method = null;
    String message = null;
    String error = null;
    final String[] parts = errorString.split("; ");
    final ObjectNode object = jsnO();
    for (String part : parts) {
      if (!part.contains(": ")) {
        dateStamp = part.trim();
      } else {
        final String name = part.substring(0, part.indexOf(":"));
        final String value = part.substring(part.indexOf(": ") + 1).trim();
        switch (name) {
          case "file":
            file = value;
            break;
          case "method":
            method = value;
            break;
          case "message":
            message = value;
            break;
          case "error":
            error = value;
            break;
          default:
            throw new RuntimeException("This should not happen");
        }
      }
    }

    return new ErrorObject(dateStamp, file, method, message, error);
  }

  public String getDateStamp() {
    return dateStamp;
  }

  public String getFile() {
    return file;
  }

  public String getMethod() {
    return method;
  }

  public String getMessage() {
    return message;
  }

  public String getError() {
    return error;
  }
}
