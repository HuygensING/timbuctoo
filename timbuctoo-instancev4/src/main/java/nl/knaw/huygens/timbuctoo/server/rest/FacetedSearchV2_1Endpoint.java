package nl.knaw.huygens.timbuctoo.server.rest;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.UUID;

@Path("/v2.1/search")
public class FacetedSearchV2_1Endpoint {

  @POST
  @Path("wwpersons")
  public Response post() {
    UUID uuid = UUID.randomUUID();
    URI uri = UriBuilder.fromResource(FacetedSearchV2_1Endpoint.class).path("{id}").build(new Object[]{uuid});
    return Response.created(uri).build();
  }

  @GET
  @Path("{id: [a-f0-9\\-]+}")
  public Response get() {
    return Response.ok("{}").build();
  }
}
