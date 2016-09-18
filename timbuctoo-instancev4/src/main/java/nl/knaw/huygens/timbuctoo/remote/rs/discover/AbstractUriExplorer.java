package nl.knaw.huygens.timbuctoo.remote.rs.discover;


import nl.knaw.huygens.timbuctoo.remote.rs.xml.ResourceSyncContext;
import nl.knaw.huygens.timbuctoo.remote.rs.xml.RsBuilder;
import nl.knaw.huygens.timbuctoo.remote.rs.xml.RsRoot;
import nl.knaw.huygens.timbuctoo.util.LambdaExceptionUtil.Function_WithExceptions;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;

import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.Charset;

/**
 * Explore a Resourcesync source.
 *
 * <q>
 * ResourceSync provides three ways for a Destination to discover whether and how a Source supports ResourceSync.
 * <ul>
 *  <li>6.3.2 ResourceSync Well-Known URI</li>
 *  <li>6.3.3 Links</li>
 *  <li>6.3.4 robots.txt</li>
 * </ul>
 * </q>
 *
 * @see <a href="http://www.openarchives.org/rs/1.0/resourcesync#Discovery">
 *   http://www.openarchives.org/rs/1.0/resourcesync#Discovery</a>
 */
public abstract class AbstractUriExplorer {

  static String getCharset(HttpResponse response) {
    ContentType contentType = ContentType.getOrDefault(response.getEntity());
    Charset charset = contentType.getCharset();
    return charset == null ? "UTF-8" : charset.name();
  }

  private final CloseableHttpClient httpClient;
  private final ResourceSyncContext rsContext;

  private URI currentUri;

  public AbstractUriExplorer(CloseableHttpClient httpClient, ResourceSyncContext rsContext) {
    this.httpClient = httpClient;
    this.rsContext = rsContext;
  }

  public abstract Result<?> explore(URI uri, ResultIndex index);

  protected CloseableHttpClient getHttpClient() {
    return httpClient;
  }

  protected ResourceSyncContext getRsContext() {
    return rsContext;
  }

  protected URI getCurrentUri() {
    return currentUri;
  }

  public <T> Result<T> execute(URI uri, Function_WithExceptions<HttpResponse, T, ?> func) {
    currentUri = uri;
    Result<T> result = new Result<T>(uri);
    HttpGet request = new HttpGet(uri);

    try (CloseableHttpResponse response = httpClient.execute(request)) {
      int statusCode = response.getStatusLine().getStatusCode();
      result.setStatusCode(statusCode);
      if (!Response.Status.Family.SUCCESSFUL.equals(Response.Status.Family.familyOf(statusCode))) {
        result.setError(new RemoteException(statusCode, response.getStatusLine().getReasonPhrase(), uri));
      } else {
        result.accept(func.apply(response));
      }
    } catch (Exception e) {
      result.setError(e);
    }
    return result;
  }

  Function_WithExceptions<HttpResponse, RsRoot, Exception> rsConverter = (response) -> {
    InputStream inStream = response.getEntity().getContent();
    return new RsBuilder(this.getRsContext()).setInputStream(inStream).build().orElse(null);
  };

}
