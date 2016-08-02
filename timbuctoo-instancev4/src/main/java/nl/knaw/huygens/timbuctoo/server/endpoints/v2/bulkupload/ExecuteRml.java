package nl.knaw.huygens.timbuctoo.server.endpoints.v2.bulkupload;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

@Path("/v2.1/bulk-upload/{vre}/rml/execute")
public class ExecuteRml {
  @POST
  public Response post() {
    return Response.status(501).entity("Yet to be implemented").build();
  }
}
