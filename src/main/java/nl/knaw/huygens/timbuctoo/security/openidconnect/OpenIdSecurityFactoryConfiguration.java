package nl.knaw.huygens.timbuctoo.security.openidconnect;

import com.fasterxml.jackson.annotation.JsonProperty;
import nl.knaw.huygens.timbuctoo.dropwizard.config.SecurityFactoryConfiguration;
import nl.knaw.huygens.timbuctoo.security.dataaccess.AccessFactory;
import nl.knaw.huygens.timbuctoo.util.TimeoutFactory;
import nl.knaw.huygens.timbuctoo.security.SecurityFactory;

import javax.validation.Valid;

public class OpenIdSecurityFactoryConfiguration implements SecurityFactoryConfiguration {
  @Valid
  @JsonProperty
  private OpenIdClient openIdClient;
  @Valid
  @JsonProperty
  private TimeoutFactory autoLogoutTimeout;
  @Valid
  @JsonProperty
  private AccessFactory accessFactory;

  @Override
  public SecurityFactory createNewSecurityFactory() {
    return new OpenIdConnectSecurityFactory(autoLogoutTimeout.createTimeout(), accessFactory, openIdClient);
  }
}
