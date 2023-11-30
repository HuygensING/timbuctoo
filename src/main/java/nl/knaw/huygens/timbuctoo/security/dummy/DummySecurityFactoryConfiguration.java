package nl.knaw.huygens.timbuctoo.security.dummy;

import com.fasterxml.jackson.annotation.JsonProperty;
import nl.knaw.huygens.timbuctoo.dropwizard.config.SecurityFactoryConfiguration;
import nl.knaw.huygens.timbuctoo.security.SecurityFactory;
import nl.knaw.huygens.timbuctoo.security.dataaccess.AccessFactory;

import javax.validation.Valid;

public class DummySecurityFactoryConfiguration implements SecurityFactoryConfiguration {
  @Valid
  @JsonProperty
  private AccessFactory accessFactory;

  @Override
  public SecurityFactory createNewSecurityFactory() {
    return new DummySecurityFactory(accessFactory);
  }
}
