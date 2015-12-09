package nl.knaw.huygens.timbuctoo.server.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/v2.1/domain/wwpersons")
@Produces(MediaType.APPLICATION_JSON)
public class WWPersonCollectionV2_1EndPoint {
  @GET
  public Response getAll() {
    return Response.ok().entity("{}").build();
  }
}
