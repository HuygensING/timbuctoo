package nl.knaw.huygens.timbuctoo.rest.filters;

/*
 * #%L
 * Timbuctoo REST api
 * =======
 * Copyright (C) 2012 - 2015 Huygens ING
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

import com.sun.jersey.spi.container.ContainerRequest;
import nl.knaw.huygens.timbuctoo.security.UserSecurityContext;
import nl.knaw.huygens.timbuctoo.security.VREAuthorizationHandler;
import org.junit.Test;
import org.mockito.Mockito;

import javax.ws.rs.WebApplicationException;

import static org.mockito.Mockito.mock;

public class UserResourceFilterTest {

  @Test(expected = WebApplicationException.class)
  public void filterThrowsAWebApplicationExceptionWithStatusUnauthorizedIfThereIsNoUserInTheSercurityContext() {
    // setup
    UserResourceFilterFactory.UserResourceFilter instance = new UserResourceFilterFactory.UserResourceFilter(mock(VREAuthorizationHandler.class));
    UserSecurityContext securityContext = mock(UserSecurityContext.class);
    ContainerRequest request = mock(ContainerRequest.class);
    Mockito.when(request.getSecurityContext()).thenReturn(securityContext);

    // action
    instance.filter(request);
  }
}
