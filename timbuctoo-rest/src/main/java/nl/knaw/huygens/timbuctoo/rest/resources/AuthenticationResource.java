package nl.knaw.huygens.timbuctoo.rest.resources;

import static nl.knaw.huygens.timbuctoo.config.Paths.VERSION_PATH_OPTIONAL;

import javax.inject.Inject;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

import nl.knaw.huygens.security.client.UnauthorizedException;
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
