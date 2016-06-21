package nl.knaw.huygens.timbuctoo.server.endpoints;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;

@Path("/")
public class RootEndpoint {

  @GET
  @Produces(MediaType.TEXT_HTML)
  public Response getHomepage() {
    return Response.temporaryRedirect(URI.create("/static/intro/")).build();
  }
}
