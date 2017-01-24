package nl.knaw.huygens.timbuctoo.solr;

import nl.knaw.huygens.timbuctoo.server.TimbuctooConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;

public class GenericSolrIndexNotifier implements SolrIndexNotifier {
  public static final Logger LOG = LoggerFactory.getLogger(GenericSolrIndexNotifier.class);

  private final String solrIndexingUrl;
  private final boolean isEnabled;

  public GenericSolrIndexNotifier(TimbuctooConfiguration configuration) {
    this.isEnabled = configuration.getSolrIndexTriggerEnabled();
    this.solrIndexingUrl = configuration.getSolrIndexingUrl();
  }

  @Override
  public void startIndexingForVre(String vreName) throws IOException {

    // Failsafe which logs an error message
    if (!isEnabled) {
      LOG.error("Invalid state: SolrIndexNotifier.startIndexingForVre invoked while not enabled via config");
      return;
    }

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

  @Override
  public boolean isEnabled() {
    return isEnabled;
  }
}
