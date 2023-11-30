package nl.knaw.huygens.timbuctoo.dropwizard.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import nl.knaw.huygens.timbuctoo.security.SecurityFactory;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "@class")
public interface SecurityFactoryConfiguration {
  SecurityFactory createNewSecurityFactory();
}
