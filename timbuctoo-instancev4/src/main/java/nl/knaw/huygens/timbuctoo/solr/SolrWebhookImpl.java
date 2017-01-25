package nl.knaw.huygens.timbuctoo.solr;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.SocketTimeoutException;

public class SolrWebhookImpl implements SolrWebhook {
  public static final Logger LOG = LoggerFactory.getLogger(SolrWebhookImpl.class);

  private final String solrIndexingUrl;
  private final HttpClient httpClient;

  SolrWebhookImpl(String url, HttpClient httpClient) {
    this.solrIndexingUrl = url;
    this.httpClient = httpClient;
  }

  @Override
  public void startIndexingForVre(String vreName) throws IOException {

    try {
      final String url = String.format("%s/%s", solrIndexingUrl, vreName);
      httpClient.execute(new HttpPost(url));
    } catch (SocketTimeoutException e) {
      // Setting a low read timeout will terminate the connection prematurely, causing a SocketTimeoutException:
      // This is expected, because writing to an HTTP connection without waiting for a response does not conform
      // to the HTTP specification
      if (LOG.isDebugEnabled()) {
        LOG.debug("Caught expected SocketTimeoutException caused by 200ms read timeout", e);
      }
    }
  }
}
