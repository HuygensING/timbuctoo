package nl.knaw.huygens.repository.modules;

import java.util.Map;

import com.google.common.collect.Maps;
import com.sun.jersey.api.core.PackagesResourceConfig;
import com.sun.jersey.guice.JerseyServletModule;
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer;
import com.sun.jersey.spi.container.servlet.ServletContainer;

public class RepositoryServletModule extends JerseyServletModule {
  @Override
  protected void configureServlets() {
    Map<String, String> params = Maps.newHashMap();
    params.put(PackagesResourceConfig.PROPERTY_PACKAGES, "nl.knaw.huygens.repository.resources;com.fasterxml.jackson.jaxrs.json;nl.knaw.huygens.repository.providers");
    params.put(PackagesResourceConfig.PROPERTY_CONTAINER_REQUEST_FILTERS, "com.sun.jersey.api.container.filter.LoggingFilter");
    params.put(ServletContainer.PROPERTY_WEB_PAGE_CONTENT_REGEX, "/static.*");
    params.put(ServletContainer.FEATURE_FILTER_FORWARD_ON_404, "true");
    filter("/*").through(GuiceContainer.class, params);
  }
}