package nl.knaw.huygens.timbuctoo.security.dropwizard;

import com.fasterxml.jackson.annotation.JsonProperty;
import nl.knaw.huygens.timbuctoo.security.FederatedAuthConfiguration;
import nl.knaw.huygens.timbuctoo.security.SecurityFactory;
import nl.knaw.huygens.timbuctoo.security.dataaccess.AccessFactory;
import nl.knaw.huygens.timbuctoo.util.TimeoutFactory;
import nl.knaw.huygens.timbuctoo.v5.security.SecurityFactoryConfiguration;
import org.apache.http.impl.client.CloseableHttpClient;


public class OldStyleSecurityFactoryConfiguration implements SecurityFactoryConfiguration {
  @JsonProperty
  private AccessFactory localAuthentication;

  @JsonProperty
  private String algorithm = "SHA-256";

  @JsonProperty
  private TimeoutFactory autoLogoutTimeout;

  @JsonProperty
  private FederatedAuthConfiguration federatedAuthentication;

  @Override
  public SecurityFactory createNewSecurityFactory(CloseableHttpClient httpCaller) {
    return new SecurityFactory(localAuthentication, algorithm, autoLogoutTimeout, federatedAuthentication,
      new HttpCaller(httpCaller));
  }
}
