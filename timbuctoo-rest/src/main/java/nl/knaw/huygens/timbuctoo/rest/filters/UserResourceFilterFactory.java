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

import static nl.knaw.huygens.timbuctoo.rest.util.CustomHeaders.VRE_ID_KEY;
import static nl.knaw.huygens.timbuctoo.rest.util.QueryParameters.USER_ID_KEY;

import java.util.Collections;
import java.util.List;

import javax.ws.rs.core.SecurityContext;

import nl.knaw.huygens.timbuctoo.model.User;
import nl.knaw.huygens.timbuctoo.model.VREAuthorization;
import nl.knaw.huygens.timbuctoo.security.UserSecurityContext;
import nl.knaw.huygens.timbuctoo.security.VREAuthorizationHandler;

import com.google.inject.Inject;
import com.sun.jersey.api.model.AbstractMethod;
import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerRequestFilter;
import com.sun.jersey.spi.container.ContainerResponseFilter;
import com.sun.jersey.spi.container.ResourceFilter;
import com.sun.jersey.spi.container.ResourceFilterFactory;

public class UserResourceFilterFactory implements ResourceFilterFactory {
  private final VREAuthorizationHandler vreAuthorizationHandler;

  @Inject
  public UserResourceFilterFactory(VREAuthorizationHandler vreAuthorizationHandler) {
    this.vreAuthorizationHandler = vreAuthorizationHandler;
  }

  @Override
  public List<ResourceFilter> create(AbstractMethod abstractMethod) {
    return Collections.<ResourceFilter> singletonList(new UserResourceFilter(vreAuthorizationHandler));
  }

  /**
   * A resource filter that sets the user and the VRE as query parameters, 
   * and retrieves the vreAuthorization of the user.
   */
  private static class UserResourceFilter implements ResourceFilter, ContainerRequestFilter {
    private final VREAuthorizationHandler vreAuthorizationHandler;

    public UserResourceFilter(VREAuthorizationHandler vreAuthorizationHandler) {
      this.vreAuthorizationHandler = vreAuthorizationHandler;
    }

    @Override
    public ContainerRequest filter(ContainerRequest request) {
      SecurityContext securityContext = request.getSecurityContext();

      if (securityContext instanceof UserSecurityContext) {
        User user = ((UserSecurityContext) securityContext).getUser();
        String userId = user.getId();
        String vreId = request.getHeaderValue(VRE_ID_KEY);

        // Set the user id and the current vre id.
        request.getQueryParameters().putSingle(USER_ID_KEY, userId);
        request.getQueryParameters().putSingle(VRE_ID_KEY, vreId);

        VREAuthorization vreAuthorization = vreAuthorizationHandler.getVREAuthorization(vreId, user);

        user.setVreAuthorization(vreAuthorization);
      }

      return request;
    }

    @Override
    public ContainerRequestFilter getRequestFilter() {
      return this;
    }

    @Override
    public ContainerResponseFilter getResponseFilter() {
      // ReponseFilter not supported
      return null;
    }

  }
}
