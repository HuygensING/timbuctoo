package nl.knaw.huygens.timbuctoo.server;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.client.HttpClientBuilder;
import io.dropwizard.client.HttpClientConfiguration;
import io.dropwizard.setup.Environment;
import nl.knaw.huygens.timbuctoo.security.SecurityFactory;
import nl.knaw.huygens.timbuctoo.server.federatedauth.HttpCaller;

public class HttpClientSecurityFactory extends SecurityFactory {

  @JsonIgnore
  private HttpCaller httpClient;
  @JsonIgnore
  private Environment environment;
  private HttpClientConfiguration httpClientConfig = new HttpClientConfiguration();

  @Override
  protected HttpCaller getHttpCaller() {
    if (httpClient == null) {
      httpClient = new HttpCaller(new HttpClientBuilder(environment)
        .using(httpClientConfig)
        .build("federated-auth-client")
      );
    }
    return httpClient;
  }

  public void setEnvironment(Environment environment) {
    this.environment = environment;
  }

  @JsonProperty
  public void setHttpClientConfig(HttpClientConfiguration httpClientConfig) {
    this.httpClientConfig = httpClientConfig;
  }
}
