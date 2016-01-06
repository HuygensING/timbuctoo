package nl.knaw.huygens.timbuctoo.server.rest;


import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/v2.1/system/users/me")
@Produces(MediaType.APPLICATION_JSON)
public class UserV2_1Endpoint {
  private final LoggedInUserStore loggedInUserStore;

  public UserV2_1Endpoint(LoggedInUserStore loggedInUserStore) {
    this.loggedInUserStore = loggedInUserStore;
  }

  @GET
  public Response get(@HeaderParam("Authorization") String authHeader) {
    User user = loggedInUserStore.userFor(authHeader);
    if (user == null) {
      return Response.status(Response.Status.UNAUTHORIZED).build();
    } else {
      return Response.ok().entity(user).build();
    }
  }
}
