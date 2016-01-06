package nl.knaw.huygens.timbuctoo.server.rest;

import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/v2.1/authenticate")
@Produces(MediaType.APPLICATION_JSON)
public class AuthenticationV2_1EndPoint {

  private final LoggedInUserStore loggedInUserStore;

  public AuthenticationV2_1EndPoint(LoggedInUserStore loggedInUserStore) {
    this.loggedInUserStore = loggedInUserStore;
  }

  @POST
  public Response authenticate(@HeaderParam(HttpHeaders.AUTHORIZATION) String encodedAuthString) {
    try {
      BasicAuthorizationHeaderParser.Credentials credentials = BasicAuthorizationHeaderParser
        .authenticate(encodedAuthString);

      String token = loggedInUserStore.userTokenFor(credentials.getUsername(), credentials.getPassword());

      return Response.noContent().header("X_AUTH_TOKEN", token).build();
    } catch (IllegalArgumentException e) {
      return Response
        .status(Response.Status.UNAUTHORIZED)
        .header(HttpHeaders.WWW_AUTHENTICATE, "Basic realm=\"timbuctoo\"")
        .build();
    }
  }
}
