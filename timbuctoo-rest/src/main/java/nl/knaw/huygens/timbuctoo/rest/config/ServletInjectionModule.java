package nl.knaw.huygens.timbuctoo.rest.config;

import java.util.Map;

import nl.knaw.huygens.security.client.filters.SecurityResourceFilterFactory;
import nl.knaw.huygens.timbuctoo.rest.CORSFilter;
import nl.knaw.huygens.timbuctoo.rest.filters.UserResourceFilterFactory;
import nl.knaw.huygens.timbuctoo.rest.filters.VREAuthorizationFilterFactory;

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
    params.put(PackagesResourceConfig.PROPERTY_PACKAGES, "nl.knaw.huygens.timbuctoo.rest.resources;com.fasterxml.jackson.jaxrs.json;nl.knaw.huygens.timbuctoo.rest.providers");
    params.put(ResourceConfig.PROPERTY_CONTAINER_REQUEST_FILTERS, ServletInjectionModelHelper.getClassNamesString(LoggingFilter.class));
    params.put(ResourceConfig.PROPERTY_RESOURCE_FILTER_FACTORIES, ServletInjectionModelHelper.getClassNamesString(SecurityResourceFilterFactory.class, VREAuthorizationFilterFactory.class,
        UserResourceFilterFactory.class, RolesAllowedResourceFilterFactory.class));
    params.put(ResourceConfig.PROPERTY_CONTAINER_RESPONSE_FILTERS, ServletInjectionModelHelper.getClassNamesString(LoggingFilter.class, CORSFilter.class));
    params.put(ServletContainer.PROPERTY_WEB_PAGE_CONTENT_REGEX, "/static.*");
    filter("/*").through(GuiceContainer.class, params);
  }

}
