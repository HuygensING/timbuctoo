package nl.knaw.huygens.concordion.extensions;

import javax.ws.rs.core.MultivaluedMap;

public class HttpRequest {
  public final String method;
  public final String url;
  public final MultivaluedMap<String, Object> headers;

  public HttpRequest(String method, String url, MultivaluedMap<String, Object> headers) {
    this.method = method;
    this.url = url;
    this.headers = headers;
  }
}
