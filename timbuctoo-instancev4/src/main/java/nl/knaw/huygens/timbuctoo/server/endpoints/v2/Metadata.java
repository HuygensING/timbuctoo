package nl.knaw.huygens.timbuctoo.server.endpoints.v4;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.ImmutableMap;
import nl.knaw.huygens.timbuctoo.model.properties.JsonMetadata;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;

@Path("/v2.1/metadata/{vre}")
@Produces(MediaType.APPLICATION_JSON)
public class Metadata {

  private final JsonMetadata jsonMetadata;

  public static URI makeUrl(String vreName) {
    return UriBuilder.fromResource(Metadata.class)
      .buildFromMap(ImmutableMap.of(
        "vre", vreName
      ));
  }

  public Metadata(JsonMetadata jsonMetadata) {
    this.jsonMetadata = jsonMetadata;
  }

  @GET
  public Response get(@PathParam("vre") String vreName) {
    ObjectNode result = jsonMetadata.getForVre(vreName);
    return Response.ok(result).build();
  }

}
