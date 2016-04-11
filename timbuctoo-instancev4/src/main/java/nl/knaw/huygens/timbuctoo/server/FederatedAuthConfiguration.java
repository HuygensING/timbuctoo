package nl.knaw.huygens.timbuctoo.server;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.client.HttpClientBuilder;
import io.dropwizard.client.HttpClientConfiguration;
import io.dropwizard.setup.Environment;
import nl.knaw.huygens.security.client.AuthenticationHandler;
import nl.knaw.huygens.security.client.HuygensAuthenticationHandler;
import nl.knaw.huygens.security.client.UnauthorizedException;
import nl.knaw.huygens.timbuctoo.server.federatedauth.HttpCaller;
import org.apache.http.client.HttpClient;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public class FederatedAuthConfiguration {

  @JsonProperty
  private String authorizationServerUrl;

  @JsonProperty
  private String authoriationCredentials;

  @JsonProperty
  @NotNull
  private Boolean enabled;

  @Valid
  @NotNull
  @JsonProperty("httpClient")
  private HttpClientConfiguration httpClientConfig = new HttpClientConfiguration();


  public AuthenticationHandler makeHandler(Environment environment) {
    if (enabled) {
      final HttpClient httpClient = new HttpClientBuilder(environment)
        .using(httpClientConfig)
        .build("federated-auth-client");

      return new HuygensAuthenticationHandler(
        new HttpCaller(httpClient),
        authorizationServerUrl,
        authoriationCredentials
      );
    } else {
      return sessionId -> {
        throw new UnauthorizedException("No federated authentication configured");
      };
    }
  }
}
