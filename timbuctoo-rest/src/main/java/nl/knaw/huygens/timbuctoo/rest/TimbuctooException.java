package nl.knaw.huygens.timbuctoo.rest;

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

  public TimbuctooException(Status status) {
    this(status, "", "");
  }

  public TimbuctooException(Status status, String message) {
    this(status, message, "");
  }

  public TimbuctooException(Status status, String message, String moreInfo) {
    super(response(status, message, moreInfo));
  }

  private static Response response(Status status, String message, String moreInfo) {
    Error error = new Error(status, message, moreInfo);
    return Response.status(status).entity(error).type(MediaType.APPLICATION_JSON).build();
  }

  public static class Error {
    private final int statusCode;
    private final String message;
    private final String moreInfo;

    public Error(Status status, String message, String info) {
      statusCode = status.getStatusCode();
      this.message = message;
      this.moreInfo = info;
    }

    public int getStatusCode() {
      return statusCode;
    }

    public String getMessage() {
      return message;
    }

    public String getMoreInfo() {
      return moreInfo;
    }
  }

}
