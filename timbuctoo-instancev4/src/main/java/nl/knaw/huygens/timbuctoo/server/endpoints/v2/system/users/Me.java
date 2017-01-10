package nl.knaw.huygens.timbuctoo.server.endpoints.v2.system.users;


import nl.knaw.huygens.timbuctoo.security.LoggedInUsers;
import nl.knaw.huygens.timbuctoo.security.dto.User;

import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Optional;

@Path("/v2.1/system/users/me")
@Produces(MediaType.APPLICATION_JSON)
public class Me {
  private final LoggedInUsers loggedInUsers;

  public Me(LoggedInUsers loggedInUsers) {
    this.loggedInUsers = loggedInUsers;
  }

  @GET
  public Response get(@HeaderParam("Authorization") String authHeader) {
    Optional<User> user = loggedInUsers.userFor(authHeader);
    if (user.isPresent()) {
      return Response.ok().entity(user.get()).build();

    } else {
      return Response.status(Response.Status.UNAUTHORIZED).build();
    }
  }
}
