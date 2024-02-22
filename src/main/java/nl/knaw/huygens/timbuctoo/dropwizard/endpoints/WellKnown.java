package nl.knaw.huygens.timbuctoo.dropwizard.endpoints;

import nl.knaw.huygens.timbuctoo.datastores.rssource.RsDocumentBuilder;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

@Path(".well-known")
public class WellKnown {
  @Path("resourcesync")
  @GET
  public Response resourceSync() {
    // Permanent redirect
    return Response.seeOther(UriBuilder.fromPath("/resourcesync")
                                       .path(RsDocumentBuilder.SOURCE_DESCRIPTION_PATH)
                                       .build()
    ).build();
  }
}
