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

import static nl.knaw.huygens.timbuctoo.config.Paths.VERSION_PATH_OPTIONAL;

import javax.inject.Inject;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

import nl.knaw.huygens.security.client.UnauthorizedException;
import nl.knaw.huygens.timbuctoo.annotations.APIDesc;
import nl.knaw.huygens.timbuctoo.rest.TimbuctooException;
import nl.knaw.huygens.timbuctoo.rest.util.CustomHeaders;
import nl.knaw.huygens.timbuctoo.security.BasicAuthenticationHandler;

@Path(VERSION_PATH_OPTIONAL + "authenticate")
public class AuthenticationResource {

  private final BasicAuthenticationHandler authenticationHandler;

  @Inject
  public AuthenticationResource(BasicAuthenticationHandler authenticationHandler) {
    this.authenticationHandler = authenticationHandler;
  }

  @APIDesc("Expects an Authorization header with a Basic authentication information.")
  @POST
  public Response authenticate(@HeaderParam(HttpHeaders.AUTHORIZATION) String authorization) {
    try {
      String token = authenticationHandler.authenticate(authorization);

      return Response.noContent().header(CustomHeaders.TOKEN_HEADER, token).build();
    } catch (IllegalArgumentException ex) {
      throw new TimbuctooException(Response.Status.BAD_REQUEST, "%s", ex.getMessage());
    } catch (UnauthorizedException ex) {
      throw new TimbuctooException(Response.Status.UNAUTHORIZED, "%s", ex.getMessage());
    }
  }
}
