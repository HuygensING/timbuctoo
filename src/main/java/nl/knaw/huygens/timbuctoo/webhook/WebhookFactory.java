package nl.knaw.huygens.timbuctoo.webhook;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import io.dropwizard.client.HttpClientBuilder;
import io.dropwizard.client.HttpClientConfiguration;
import io.dropwizard.core.setup.Environment;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class WebhookFactory {
  private List<String> dataSetUpdatedUrls;

  @Valid
  @NotNull
  @JsonProperty("httpClient")
  private HttpClientConfiguration httpClientConfig = new HttpClientConfiguration();

  @JsonSetter("dataSetUpdatedUrls")
  public void setDataSetUpdatedUrls(List<String> dataSetUpdatedUrls) {
    this.dataSetUpdatedUrls = dataSetUpdatedUrls.stream()
      .filter(Objects::nonNull)
      .collect(Collectors.toList());
  }

  public Webhooks getWebHook(Environment environment) {
    if (dataSetUpdatedUrls != null && !dataSetUpdatedUrls.isEmpty()) {
      final CloseableHttpClient httpClient = new HttpClientBuilder(environment)
        .using(httpClientConfig)
        .build("webhook-client");

      return new CallingWebhooks(dataSetUpdatedUrls, httpClient);
    } else {
      return new NoOpWebhooks();
    }
  }
}
