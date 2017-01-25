package nl.knaw.huygens.timbuctoo.solr;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.client.HttpClientBuilder;
import io.dropwizard.client.HttpClientConfiguration;
import io.dropwizard.setup.Environment;
import io.dropwizard.util.Duration;
import org.apache.http.client.HttpClient;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public class WebhookFactory {

  @JsonProperty
  private String vreAdded;

  @Valid
  @NotNull
  @JsonProperty("httpClient")
  private HttpClientConfiguration httpClientConfig = new HttpClientConfiguration();

  public Webhooks getWebHook(Environment environment) {
    if (vreAdded != null) {
      httpClientConfig.setConnectionRequestTimeout(Duration.milliseconds(200));
      final HttpClient httpClient = new HttpClientBuilder(environment)
        .using(httpClientConfig)
        .build("solr-webhook-client");

      return new CallingWebhooks(vreAdded, httpClient);
    } else {
      return new NoOpWebhooks();
    }
  }
}
