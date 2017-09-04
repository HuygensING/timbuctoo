package nl.knaw.huygens.timbuctoo.security;

import com.fasterxml.jackson.annotation.JsonProperty;
import nl.knaw.huygens.security.client.AuthenticationHandler;
import nl.knaw.huygens.security.client.HttpCaller;
import nl.knaw.huygens.security.client.HuygensAuthenticationHandler;
import nl.knaw.huygens.security.client.UnauthorizedException;
import nl.knaw.huygens.security.client.model.HuygensSecurityInformation;

import javax.validation.constraints.NotNull;

public class FederatedAuthConfiguration {

  @JsonProperty
  private String authenticationServerUrl;

  @JsonProperty
  private String authenticationCredentials;

  @JsonProperty
  @NotNull
  private Boolean enabled;

  public AuthenticationHandler makeHandler(HttpCaller httpCaller) {
    if (enabled) {
      if (authenticationServerUrl.equals("DUMMY")) {
        return sessionId -> {
          HuygensSecurityInformation information = new HuygensSecurityInformation();
          information.setPersistentID(authenticationCredentials == null ? "123456789" : authenticationCredentials);
          information.setDisplayName("TEST");
          return information;
        };
      } else {
        return new HuygensAuthenticationHandler(
          httpCaller,
          authenticationServerUrl,
          authenticationCredentials
        );
      }
    } else {
      return sessionId -> {
        throw new UnauthorizedException("No federated authentication configured");
      };
    }
  }
}
