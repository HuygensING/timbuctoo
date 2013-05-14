package nl.knaw.huygens.repository.config;

import java.util.Map;

import com.google.common.collect.Maps;
import com.sun.jersey.api.core.PackagesResourceConfig;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.guice.JerseyServletModule;
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer;
import com.sun.jersey.spi.container.servlet.ServletContainer;

public class ServletInjectionModule extends JerseyServletModule {

  @Override
  protected void configureServlets() {
    Map<String, String> params = Maps.newHashMap();
    params.put(PackagesResourceConfig.PROPERTY_PACKAGES, "nl.knaw.huygens.repository.resources;com.fasterxml.jackson.jaxrs.json;nl.knaw.huygens.repository.providers");
    params.put(ResourceConfig.PROPERTY_CONTAINER_REQUEST_FILTERS, "com.sun.jersey.api.container.filter.LoggingFilter");
    params.put(ResourceConfig.PROPERTY_RESOURCE_FILTER_FACTORIES,
        "nl.knaw.huygens.repository.server.security.AnnotatedSecurityFilterFactory;com.sun.jersey.api.container.filter.RolesAllowedResourceFilterFactory");
    params.put(ResourceConfig.PROPERTY_CONTAINER_RESPONSE_FILTERS, "com.sun.jersey.api.container.filter.LoggingFilter");
    params.put(ServletContainer.PROPERTY_WEB_PAGE_CONTENT_REGEX, "/static.*");
    filter("/*").through(GuiceContainer.class, params);
  }

}
