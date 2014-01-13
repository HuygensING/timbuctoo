package nl.knaw.huygens.timbuctoo.rest.config;

/*
 * #%L
 * Timbuctoo REST api
 * =======
 * Copyright (C) 2012 - 2014 Huygens ING
 * =======
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

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
