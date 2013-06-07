package nl.knaw.huygens.repository.config;

import java.util.Map;

import nl.knaw.huygens.repository.server.security.RolesPartiallyAllowedResourceFilterFactory;
import nl.knaw.huygens.repository.server.security.apis.ApisAuthorizationResourceFilterFactory;
import nl.knaw.huygens.repository.server.security.apis.SecurityContextCreatorResourceFilterFactory;

import com.google.common.collect.Maps;
import com.sun.jersey.api.container.filter.LoggingFilter;
import com.sun.jersey.api.container.filter.RolesAllowedResourceFilterFactory;
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
    params.put(ResourceConfig.PROPERTY_CONTAINER_REQUEST_FILTERS, getClassNamesString(LoggingFilter.class));
    params.put(
        ResourceConfig.PROPERTY_RESOURCE_FILTER_FACTORIES,
        getClassNamesString(ApisAuthorizationResourceFilterFactory.class, SecurityContextCreatorResourceFilterFactory.class, RolesAllowedResourceFilterFactory.class,
            RolesPartiallyAllowedResourceFilterFactory.class));
    params.put(ResourceConfig.PROPERTY_CONTAINER_RESPONSE_FILTERS, getClassNamesString(LoggingFilter.class));
    params.put(ServletContainer.PROPERTY_WEB_PAGE_CONTENT_REGEX, "/static.*");
    filter("/*").through(GuiceContainer.class, params);
  }

  private String getClassNamesString(Class<?>... classes) {
    StringBuilder sb = new StringBuilder();
    boolean first = true;
    for (Class<?> cls : classes) {
      if (!first) {
        sb.append(";");
      }
      sb.append(cls.getName());
      first = false;
    }
    return sb.toString();
  }
}
