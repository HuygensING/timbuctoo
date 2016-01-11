package nl.knaw.huygens.timbuctoo.server.rest;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;

@Path("/v2.1/search/wwpersons")
public class FacetedSearchV2_1Endpoint {

  @POST
  public Response post() {
    URI uri = UriBuilder.fromUri("http://localhost:8080").build();
    return Response.created(uri).build();
  }
}
