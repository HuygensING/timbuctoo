package nl.knaw.huygens.concordion.extensions;

import java.util.AbstractMap;
import java.util.List;

public class HttpRequest {
  public final String method;
  public final String url;
  public final List<AbstractMap.SimpleEntry<String, String>> headers;
  public final String body;
  public final String server;

  public HttpRequest(String method, String url, List<AbstractMap.SimpleEntry<String, String>> headers, String body, String server) {
    this.method = method;
    this.url = url;
    this.headers = headers;
    this.body = body;
    this.server = server;
  }
}
