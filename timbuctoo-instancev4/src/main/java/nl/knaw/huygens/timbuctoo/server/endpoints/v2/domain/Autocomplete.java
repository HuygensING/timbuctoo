package nl.knaw.huygens.timbuctoo.server.endpoints.v2.domain;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.ImmutableMap;
import nl.knaw.huygens.timbuctoo.crud.InvalidCollectionException;
import nl.knaw.huygens.timbuctoo.crud.TinkerpopJsonCrudService;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;

import static nl.knaw.huygens.timbuctoo.util.JsonBuilder.jsn;
import static nl.knaw.huygens.timbuctoo.util.JsonBuilder.jsnO;

@Path("/v2.1/domain/{collection}/autocomplete")
@Produces(MediaType.APPLICATION_JSON)
public class Autocomplete {

  public static URI makeUrl(String collectionName, String token) {
    return UriBuilder.fromResource(Autocomplete.class)
      .queryParam("query", "*" + token + "*")
      .buildFromMap(ImmutableMap.of(
        "collection", collectionName
      ));
  }

  private final TinkerpopJsonCrudService crudService;

  public Autocomplete(TinkerpopJsonCrudService crudService) {
    this.crudService = crudService;
  }

  @GET
  public Response get(@PathParam("collection") String collectionName, @QueryParam("query") String query) {
    try {
      JsonNode result = crudService.autoComplete(collectionName, query);
      return Response.ok(result).build();
    } catch (InvalidCollectionException e) {
      return Response.status(Response.Status.NOT_FOUND).entity(jsnO("message", jsn(e.getMessage()))).build();
    }
  }
}
