package nl.knaw.huygens.timbuctoo.solr;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.client.HttpClientBuilder;
import io.dropwizard.client.HttpClientConfiguration;
import io.dropwizard.setup.Environment;
import io.dropwizard.util.Duration;
import org.apache.http.client.HttpClient;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.Optional;

public class SolrWebhookFactory {

  @JsonProperty
  private boolean isEnabled;

  @JsonProperty
  private String url;

  @Valid
  @NotNull
  @JsonProperty("httpClient")
  private HttpClientConfiguration httpClientConfig = new HttpClientConfiguration();

  public Optional<SolrWebhook> getWebHook(Environment environment) {
    if (isEnabled) {
      httpClientConfig.setConnectionRequestTimeout(Duration.milliseconds(200));
      final HttpClient httpClient = new HttpClientBuilder(environment)
        .using(httpClientConfig)
        .build("solr-webhook-client");

      return Optional.of(new SolrWebhookImpl(url, httpClient));
    } else {
      return Optional.empty();
    }
  }
}
