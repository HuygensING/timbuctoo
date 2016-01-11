package nl.knaw.huygens.concordion.extensions;

import com.google.common.collect.Lists;

import java.util.AbstractMap;
import java.util.List;

public class HttpRequest {
  public final String method;
  public final String url;
  public final List<AbstractMap.SimpleEntry<String, String>> headers;
  public final String body;
  public final String server;
  public final List<AbstractMap.SimpleEntry<String, String>> queryParameters;

  public HttpRequest(
    String method,
    String url,
    List<AbstractMap.SimpleEntry<String, String>> headers,
    String body,
    String server,
    List<AbstractMap.SimpleEntry<String, String>> queryParameters
  ) {
    this.method = method;
    this.url = url;
    this.headers = headers;
    this.body = body;
    this.server = server;

    this.queryParameters = queryParameters;
  }

  public HttpRequest(String method, String url) {
    this.method = method;
    this.url = url;
    this.body = null;
    this.server = null;
    this.headers = Lists.newArrayList();
    this.queryParameters = Lists.newArrayList();
  }


  public HttpRequest withHeader(String key, String value) {
    headers.add(new AbstractMap.SimpleEntry<>(key, value));
    return this;
  }
}
