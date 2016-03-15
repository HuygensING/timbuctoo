package nl.knaw.huygens.timbuctoo.rest.resources;

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

import static nl.knaw.huygens.timbuctoo.security.UserRoles.USER_ROLE;
import nl.knaw.huygens.timbuctoo.config.Paths;

import org.junit.Test;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.ClientResponse.Status;

public class SiteMapResourceTest extends WebServiceTestSetup {

  @Test
  public void testGetSitemapV1() {
    setupUserWithRoles(VRE_ID, USER_ID, USER_ROLE);

    ClientResponse response = apiResource();
    verifyResponseStatus(response, Status.OK);
  }

  protected ClientResponse apiResource() {
    return resource().path(getAPIVersion()).path(Paths.SYSTEM_PREFIX).path("api") //
        .get(ClientResponse.class);
  }

}
