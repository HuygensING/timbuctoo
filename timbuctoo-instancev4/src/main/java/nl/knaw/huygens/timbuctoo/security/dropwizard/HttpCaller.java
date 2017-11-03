package nl.knaw.huygens.timbuctoo.security.dropwizard;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.LinkedListMultimap;
import nl.knaw.huygens.security.client.ActualResult;
import nl.knaw.huygens.security.client.ActualResultWithBody;
import nl.knaw.huygens.security.client.HttpRequest;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpOptions;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.methods.HttpTrace;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

public class HttpCaller implements nl.knaw.huygens.security.client.HttpCaller {

  private final HttpClient client;
  private final ObjectMapper mapper;

  public HttpCaller(HttpClient httpClient) {
    this.client = httpClient;
    this.mapper = new ObjectMapper();
  }

  @Override
  public ActualResult call(HttpRequest value) throws IOException {
    HttpResponse response = makeRequest(value);

    LinkedListMultimap<String, String> headers = LinkedListMultimap.create();
    for (Header header : response.getAllHeaders()) {
      headers.put(header.getName(), header.getValue());
    }

    return new ActualResult(
      response.getStatusLine().getStatusCode(),
      response.getStatusLine().getReasonPhrase(),
      headers
    );
  }

  @Override
  public <T> ActualResultWithBody<T> call(HttpRequest value, Class<? extends T> clazz) throws IOException {
    HttpResponse response = makeRequest(value);

    InputStream content = response.getEntity().getContent();
    T body = mapper.readValue(content, clazz);

    LinkedListMultimap<String, String> headers = LinkedListMultimap.create();
    for (Header header : response.getAllHeaders()) {
      headers.put(header.getName(), header.getValue());
    }

    return new ActualResultWithBody<>(
      response.getStatusLine().getStatusCode(),
      response.getStatusLine().getReasonPhrase(),
      headers,
      body
    );
  }

  private HttpResponse makeRequest(HttpRequest value) throws IOException {
    HttpRequestBase request;

    switch (value.method) {
      case "POST":
        request = new HttpPost(value.getUrl());
        break;
      case "GET":
        request = new HttpGet(value.getUrl());
        break;
      case "PUT":
        request = new HttpPut(value.getUrl());
        break;
      case "DELETE":
        request = new HttpDelete(value.getUrl());
        break;
      case "OPTIONS":
        request = new HttpOptions(value.getUrl());
        break;
      case "HEAD":
        request = new HttpHead(value.getUrl());
        break;
      case "TRACE":
        request = new HttpTrace(value.getUrl());
        break;
      default:
        throw new IllegalArgumentException("HTTP method not supported");
    }

    for (Map.Entry<String, String> header : value.headers.entries()) {
      request.setHeader(header.getKey(), header.getValue());
    }

    return client.execute(request);
  }

}
