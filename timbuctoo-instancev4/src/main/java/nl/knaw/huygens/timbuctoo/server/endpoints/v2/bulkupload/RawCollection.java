package nl.knaw.huygens.timbuctoo.server.endpoints.v2.bulkupload;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

@Path("/v2.1/bulk-upload/{vre}/raw-collections/{collection}")
public class RawCollection {
  @GET
  public Response get() {
    return Response.status(501).entity("Yet to be implemented").build();
  }
}
