package nl.knaw.huygens.timbuctoo.server.endpoints.system.users;

import nl.knaw.huygens.timbuctoo.security.dto.User;
import nl.knaw.huygens.timbuctoo.security.UserValidator;
import nl.knaw.huygens.timbuctoo.security.exceptions.UserValidationException;

import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Optional;

@Path("/system/users/me")
@Produces(MediaType.APPLICATION_JSON)
public class Me {
  private final UserValidator userValidator;

  public Me(UserValidator userValidator) {
    this.userValidator = userValidator;
  }

  @GET
  public Response get(@HeaderParam("Authorization") String authHeader) {
    Optional<User> user;
    try {
      user = userValidator.getUserFromAccessToken(authHeader);
    } catch (UserValidationException e) {
      user = Optional.empty();
    }
    if (user.isPresent()) {
      return Response.ok().entity(user.get()).build();

    } else {
      return Response.status(Response.Status.UNAUTHORIZED).build();
    }
  }
}
