package nl.knaw.huygens.timbuctoo.v5.security.dummy;

import com.fasterxml.jackson.annotation.JsonProperty;
import nl.knaw.huygens.timbuctoo.security.dataaccess.AccessFactory;
import nl.knaw.huygens.timbuctoo.v5.dropwizard.config.SecurityFactoryConfiguration;
import nl.knaw.huygens.timbuctoo.v5.security.SecurityFactory;
import org.apache.http.impl.client.CloseableHttpClient;

import javax.validation.Valid;

public class DummySecurityFactoryConfiguration implements SecurityFactoryConfiguration {
  @Valid
  @JsonProperty
  private AccessFactory accessFactory;

  @Override
  public SecurityFactory createNewSecurityFactory(CloseableHttpClient httpCaller) {
    return new DummySecurityFactory(accessFactory);
  }
}
