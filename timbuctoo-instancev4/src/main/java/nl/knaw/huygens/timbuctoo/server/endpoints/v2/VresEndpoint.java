package nl.knaw.huygens.timbuctoo.server.endpoints.v2;

import com.fasterxml.jackson.databind.node.ArrayNode;
import nl.knaw.huygens.timbuctoo.model.properties.JsonMetadata;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/v2.1/system/vres")
@Produces(MediaType.APPLICATION_JSON)
public class VresEndpoint {

  private final JsonMetadata jsonMetadata;

  public VresEndpoint(JsonMetadata jsonMetadata) {
    this.jsonMetadata = jsonMetadata;
  }


  @GET
  public Response get() {
    ArrayNode result = jsonMetadata.listVres();
    return Response.ok(result).build();
  }


}
