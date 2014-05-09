package nl.knaw.huygens.timbuctoo.rest;

import java.util.IllegalFormatException;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

/**
 * A web application exception that adds a response body
 * with details about the error that occurred.
 */
public class TimbuctooException extends WebApplicationException {

  private static final long serialVersionUID = 1L;

  public TimbuctooException(Status status, String format, Object... args) {
    super(response(status, format, args));
  }

  public TimbuctooException(Status status) {
    super(response(status, ""));
  }

  private static Response response(Status status, String format, Object... args) {
    Error error = new Error(status, format, args);
    return Response.status(status).entity(error).type(MediaType.APPLICATION_JSON).build();
  }

  public static class Error {
    private final int statusCode;
    private String message;

    public Error(Status status, String format, Object... args) {
      statusCode = status.getStatusCode();
      try {
        message = String.format(format, args);
      } catch (IllegalFormatException e) {
        // best effort...
        message = format;
      }
    }

    public int getStatusCode() {
      return statusCode;
    }

    public String getMessage() {
      return message;
    }
  }

}
