package nl.knaw.huygens.concordion.extensions;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

public class HttpResult {
  private final int status;
  private final MultivaluedMap<String, Object> headers;
  private final String body;

  public HttpResult(Response callResult) {
    this.status = callResult.getStatus();
    this.headers = callResult.getHeaders();
    this.body = callResult.readEntity(String.class);
  }

  public int getStatus() {
    return status;
  }

  public MultivaluedMap<String, Object> getHeaders() {
    return headers;
  }

  public String getBody() {
    return body;
  }
}
