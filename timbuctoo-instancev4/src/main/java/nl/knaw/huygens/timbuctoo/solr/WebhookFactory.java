package nl.knaw.huygens.timbuctoo.solr;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.client.HttpClientBuilder;
import io.dropwizard.client.HttpClientConfiguration;
import io.dropwizard.core.setup.Environment;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public class WebhookFactory {

  @JsonProperty
  private String vreAdded;

  @JsonProperty
  private String dataSetUpdated;

  @Valid
  @NotNull
  @JsonProperty("httpClient")
  private HttpClientConfiguration httpClientConfig = new HttpClientConfiguration();

  public Webhooks getWebHook(Environment environment) {
    if (vreAdded != null || dataSetUpdated != null) {
      final CloseableHttpClient httpClient = new HttpClientBuilder(environment)
        .using(httpClientConfig)
        .build("solr-webhook-client");

      return new CallingWebhooks(vreAdded, dataSetUpdated, httpClient);
    } else {
      return new NoOpWebhooks();
    }
  }
}
