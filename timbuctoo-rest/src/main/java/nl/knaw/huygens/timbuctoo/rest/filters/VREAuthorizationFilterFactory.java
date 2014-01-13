package nl.knaw.huygens.timbuctoo.rest.filters;

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

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response.Status;

import nl.knaw.huygens.security.client.filters.AbstractRolesAllowedResourceFilterFactory;
import nl.knaw.huygens.security.client.filters.BypassFilter;
import nl.knaw.huygens.timbuctoo.rest.util.CustomHeaders;
import nl.knaw.huygens.timbuctoo.vre.VREManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.sun.jersey.api.model.AbstractMethod;
import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerRequestFilter;
import com.sun.jersey.spi.container.ContainerResponseFilter;
import com.sun.jersey.spi.container.ResourceFilter;

public class VREAuthorizationFilterFactory extends AbstractRolesAllowedResourceFilterFactory {

  private final VREManager vreManager;

  @Inject
  public VREAuthorizationFilterFactory(VREManager vreManager) {
    this.vreManager = vreManager;
  }

  @Override
  protected ResourceFilter createResourceFilter(AbstractMethod am) {
    return new VREAuthorizationResourceFilter(this.vreManager);
  }

  @Override
  protected ResourceFilter createNoSecurityResourceFilter() {
    return new BypassFilter();
  }

  /**
   * This class filters the requests to see if a VRE id is attached to the request, and if the id is valid.
   */
  protected static class VREAuthorizationResourceFilter implements ResourceFilter, ContainerRequestFilter {
    private static final Logger LOG = LoggerFactory.getLogger(VREAuthorizationResourceFilter.class);
    private final VREManager vreManager;

    public VREAuthorizationResourceFilter(VREManager vreManager) {
      this.vreManager = vreManager;
    }

    @Override
    public ContainerRequest filter(ContainerRequest request) {

      // Get the VRE.
      String vreId = request.getHeaderValue(CustomHeaders.VRE_ID_KEY);
      if (vreId == null) {
        LOG.error("No VRE id was send with the request.");
        throw new WebApplicationException(Status.UNAUTHORIZED);
      }

      if (!vreManager.doesVREExist(vreId)) {
        LOG.error("VRE with id {} is not known.", vreId);
        throw new WebApplicationException(Status.FORBIDDEN);
      }

      return request;
    }

    @Override
    public ContainerRequestFilter getRequestFilter() {
      return this;
    }

    @Override
    public ContainerResponseFilter getResponseFilter() {
      return null;
    }

  }

}
