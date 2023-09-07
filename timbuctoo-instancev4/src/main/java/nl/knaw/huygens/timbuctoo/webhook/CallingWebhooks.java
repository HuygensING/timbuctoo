package nl.knaw.huygens.timbuctoo.webhook;

import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.List;

import static nl.knaw.huygens.timbuctoo.util.JsonBuilder.jsn;
import static nl.knaw.huygens.timbuctoo.util.JsonBuilder.jsnO;

public class CallingWebhooks implements Webhooks {
  public static final Logger LOG = LoggerFactory.getLogger(CallingWebhooks.class);

  private final List<String> urls;
  private final CloseableHttpClient httpClient;

  CallingWebhooks(List<String> urls, CloseableHttpClient httpClient) {
    this.urls = urls;
    this.httpClient = httpClient;
  }

  @Override
  public void dataSetUpdated(String dataSetId) throws IOException {
    for (String url : urls) {
      try {
        HttpPost request = new HttpPost(url);
        request.setEntity(new StringEntity(jsnO(
            "dataSetId", jsn(dataSetId)
        ).toString(), ContentType.APPLICATION_JSON));
        LOG.info("Calling " + url + " to signal update");
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
}
