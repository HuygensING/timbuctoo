package nl.knaw.huygens.timbuctoo.solr;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;

public class SolrWebhookImpl implements SolrWebhook {
  public static final Logger LOG = LoggerFactory.getLogger(SolrWebhookImpl.class);

  private final String solrIndexingUrl;

  SolrWebhookImpl(String url) {
    this.solrIndexingUrl = url;
  }

  @Override
  public void startIndexingForVre(String vreName) throws IOException {

    try {
      final URL url = new URL(String.format("%s/%s", solrIndexingUrl, vreName));

      // Configure the connection to a minimal read timeout
      final URLConnection connection = url.openConnection();
      connection.setReadTimeout(100);
      connection.setDoOutput(true);

      // Write minimal HTTP POST data to trigger indexing
      final OutputStream outputStream = connection.getOutputStream();
      outputStream.write(String.format("POST %s HTTP/1.1\n", url.getPath()).getBytes());
      outputStream.write("Accept: *.*\n\n".getBytes());
      outputStream.flush();
      outputStream.close();

      // Obtain input stream to enforce valid read/write HTTP situation
      connection.getInputStream().close();

      LOG.info("Solr indexing triggered for {}", vreName);
    } catch (SocketTimeoutException e) {
      // Setting a low read timeout will terminate the connection prematurely, causing a SocketTimeoutException:
      // This is expected, because writing to an HTTP connection without waiting for a response does not conform
      // to the HTTP specification
      if (LOG.isDebugEnabled()) {
        LOG.debug("Caught expected SocketTimeoutException caused by 100ms read timeout", e);
      }
    }
  }
}
