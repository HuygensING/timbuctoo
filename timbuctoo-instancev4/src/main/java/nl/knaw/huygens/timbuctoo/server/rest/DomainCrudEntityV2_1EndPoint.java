package nl.knaw.huygens.timbuctoo.server.rest;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.ImmutableMap;
import io.dropwizard.jersey.params.UUIDParam;
import nl.knaw.huygens.timbuctoo.crud.InvalidCollectionException;
import nl.knaw.huygens.timbuctoo.crud.TinkerpopJsonCrudService;

import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.UUID;

@Path("/v2.1/domain/{collection}/{id}")
@Produces(MediaType.APPLICATION_JSON)
public class DomainCrudEntityV2_1EndPoint {

  public static URI makeUrl(String collectionName, UUID id) {
    return UriBuilder.fromResource(DomainCrudCollectionV2_1EndPoint.class)
      .buildFromMap(ImmutableMap.of(
        "collection", collectionName,
        "id", id
      ));
  }

  public static URI makeUrl(String collectionName, UUID id, int rev) {
    return UriBuilder.fromResource(DomainCrudCollectionV2_1EndPoint.class)
      .queryParam("rev", rev)
      .buildFromMap(ImmutableMap.of(
        "collection", collectionName,
        "id", id
      ));
  }

  private final TinkerpopJsonCrudService crudService;

  public DomainCrudEntityV2_1EndPoint(TinkerpopJsonCrudService crudService) {
    this.crudService = crudService;
  }

  @GET
  public Response get(@PathParam("collection") String collectionName, @PathParam("id") UUIDParam id) {
    //try {
    JsonNode result = crudService.get(collectionName, id.get());
    return Response.ok(result).build();
    //} catch (InvalidCollectionException e) {
    //  return Response.status(Response.Status.NOT_FOUND).build();
    //} catch (NotFoundException e) {
    //  return Response.status(Response.Status.NOT_FOUND).build();
    //}
  }
}
