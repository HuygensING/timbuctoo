package nl.knaw.huygens.timbuctoo.rest.resources;

import javax.inject.Inject;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

import nl.knaw.huygens.security.client.UnauthorizedException;
import nl.knaw.huygens.timbuctoo.rest.util.CustomHeaders;
import nl.knaw.huygens.timbuctoo.security.BasicAuthenticationHandler;

@Path("authenticate")
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
      return Response.status(Response.Status.BAD_REQUEST).build();
    } catch (UnauthorizedException ex) {
      return Response.status(Response.Status.UNAUTHORIZED).build();
    }
  }
}
