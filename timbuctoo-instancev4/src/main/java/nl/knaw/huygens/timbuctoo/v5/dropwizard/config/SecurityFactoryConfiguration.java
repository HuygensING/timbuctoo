package nl.knaw.huygens.timbuctoo.v5.dropwizard.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import nl.knaw.huygens.timbuctoo.v5.security.SecurityFactory;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "@class")
public interface SecurityFactoryConfiguration {
  SecurityFactory createNewSecurityFactory();
}
