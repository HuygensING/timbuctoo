package nl.knaw.huygens.timbuctoo.server.endpoints;

import nl.knaw.huygens.timbuctoo.server.UriHelper;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;

@Path("/")
public class RootEndpoint {

  private final UriHelper uriHelper;

  public RootEndpoint(UriHelper uriHelper) {
    this.uriHelper = uriHelper;
  }

  @GET
  @Produces(MediaType.TEXT_HTML)
  public Response getHomepage() {
    return Response.temporaryRedirect(uriHelper.fromResourceUri(URI.create("/static/upload/"))).build();
  }
}
