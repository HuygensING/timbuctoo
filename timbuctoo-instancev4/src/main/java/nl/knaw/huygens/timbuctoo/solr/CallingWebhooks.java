package nl.knaw.huygens.timbuctoo.solr;

import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.SocketTimeoutException;

import static nl.knaw.huygens.timbuctoo.util.JsonBuilder.jsn;
import static nl.knaw.huygens.timbuctoo.util.JsonBuilder.jsnO;

public class CallingWebhooks implements Webhooks {
  public static final Logger LOG = LoggerFactory.getLogger(CallingWebhooks.class);

  private final String solrIndexingUrl;
  private final String indexerUrl;
  private final CloseableHttpClient httpClient;

  CallingWebhooks(String solrUrl, String indexerUrl, CloseableHttpClient httpClient) {
    this.solrIndexingUrl = solrUrl;
    this.indexerUrl = indexerUrl;
    this.httpClient = httpClient;
  }

  @Override
  public void startIndexingForVre(String vreName) throws IOException {
    if (solrIndexingUrl == null || solrIndexingUrl.isEmpty()) {
      return;
    }
    try {
      HttpPost request = new HttpPost(solrIndexingUrl);
      //FIXME sending only the datasetName requires the client to construct the urls by hand
      //it would be better to send the full vre metadata, but then the metadata needs to contain proper urls first
      request.setEntity(new StringEntity(jsnO(
        "datasetName", jsn(vreName)
      ).toString(), ContentType.APPLICATION_JSON));
      httpClient.execute(request, res -> null);
    } catch (SocketTimeoutException e) {
      // Setting a low read timeout will terminate the connection prematurely, causing a SocketTimeoutException:
      // This is expected, because writing to an HTTP connection without waiting for a response does not conform
      // to the HTTP specification
      if (LOG.isDebugEnabled()) {
        LOG.debug("Caught expected SocketTimeoutException caused by 200ms read timeout", e);
      }
    }
  }

  @Override
  public void dataSetUpdated(String dataSetId) throws IOException {
    if (indexerUrl == null || indexerUrl.isEmpty()) {
      return;
    }
    try {
      HttpPost request = new HttpPost(indexerUrl);
      request.setEntity(new StringEntity(jsnO(
        "dataSetId", jsn(dataSetId)
      ).toString(), ContentType.APPLICATION_JSON));
      LOG.info("Calling " + indexerUrl + " to signal update");
      httpClient.execute(request, response -> null);
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
