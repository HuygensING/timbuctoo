package nl.knaw.huygens.timbuctoo.remote.rs.discover;


import nl.knaw.huygens.timbuctoo.util.LambdaExceptionUtil.Function_WithExceptions;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.slf4j.Logger;

import javax.ws.rs.core.Response;
import java.net.URI;
import java.nio.charset.Charset;

/**
 * Execute a request against a server.
 */
public abstract class AbstractUriExplorer {

  private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(AbstractUriExplorer.class);
  private final CloseableHttpClient httpClient;
  private URI currentUri;

  public AbstractUriExplorer(CloseableHttpClient httpClient) {
    this.httpClient = httpClient;
  }

  static String getCharset(HttpResponse response) {
    ContentType contentType = ContentType.getOrDefault(response.getEntity());
    Charset charset = contentType.getCharset();
    return charset == null ? "UTF-8" : charset.name();
  }

  public abstract Result<?> explore(URI uri, ResultIndex index, String authString);

  protected CloseableHttpClient getHttpClient() {
    return httpClient;
  }

  protected URI getCurrentUri() {
    return currentUri;
  }

  public <T> Result<T> execute(URI uri, Function_WithExceptions<HttpResponse, T, ?> func, String authString) {
    currentUri = uri;
    Result<T> result = new Result<T>(uri);
    HttpGet request = new HttpGet(uri);
    if (authString != null) {
      request.addHeader("Authorization", authString);
    }
    try (CloseableHttpResponse response = httpClient.execute(request)) {
      int statusCode = response.getStatusLine().getStatusCode();
      result.setStatusCode(statusCode);
      if (!Response.Status.Family.SUCCESSFUL.equals(Response.Status.Family.familyOf(statusCode))) {
        result.addError(new RemoteException(statusCode, response.getStatusLine().getReasonPhrase(), uri));
      } else {
        result.accept(func.apply(response));
      }
    } catch (Exception e) {
      result.addError(e);
    }
    return result;
  }

}
