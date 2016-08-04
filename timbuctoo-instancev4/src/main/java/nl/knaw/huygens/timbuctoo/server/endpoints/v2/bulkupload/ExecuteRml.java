package nl.knaw.huygens.timbuctoo.server.endpoints.v2.bulkupload;

import nl.knaw.huygens.timbuctoo.server.UriHelper;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;

@Path("/v2.1/bulk-upload/{vre}/rml/execute")
public class ExecuteRml {
  private final UriHelper uriHelper;

  public ExecuteRml(UriHelper uriHelper) {
    this.uriHelper = uriHelper;
  }

  @POST
  public Response post() {
    return Response.status(501).entity("Yet to be implemented").build();
  }

  public URI makeUri(String vreName) {
    URI resourceUri = UriBuilder.fromResource(ExecuteRml.class).resolveTemplate("vre", vreName).build();

    return uriHelper.fromResourceUri(resourceUri);
  }
}
