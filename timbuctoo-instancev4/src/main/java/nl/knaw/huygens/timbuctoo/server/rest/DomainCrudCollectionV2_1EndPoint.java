package nl.knaw.huygens.timbuctoo.server.rest;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.ImmutableMap;
import nl.knaw.huygens.timbuctoo.crud.InvalidCollectionException;
import nl.knaw.huygens.timbuctoo.crud.TinkerpopJsonCrudService;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import java.io.IOException;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.UUID;

@Path("/v2.1/domain/{collection}")
@Produces(MediaType.APPLICATION_JSON)
public class DomainCrudCollectionV2_1EndPoint {

  public static URI makeUrl(String collectionName) {
    return UriBuilder.fromResource(DomainCrudCollectionV2_1EndPoint.class)
      .buildFromMap(ImmutableMap.of(
        "collection", collectionName
      ));
  }

  private final TinkerpopJsonCrudService crudService;

  public DomainCrudCollectionV2_1EndPoint(TinkerpopJsonCrudService crudService) {
    this.crudService = crudService;
  }

  @POST
  public Response createNew(@PathParam("collection") String collectionName, ObjectNode body) throws URISyntaxException {
    try {
      UUID id = crudService.create(
        collectionName,
        body,
        "",
        (idParam, rev) -> DomainCrudEntityV2_1EndPoint.makeUrl(collectionName, idParam, rev)
      );
      return Response.created(DomainCrudEntityV2_1EndPoint.makeUrl(collectionName, id)).build();
    } catch (InvalidCollectionException e) {
      return Response.status(Response.Status.NOT_FOUND).build();
    } catch (IOException e) {
      return Response.status(400).build();
    }
  }
}
