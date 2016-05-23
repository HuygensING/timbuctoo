package nl.knaw.huygens.timbuctoo.server.endpoints.v2.domain;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.ImmutableMap;
import nl.knaw.huygens.timbuctoo.crud.InvalidCollectionException;
import nl.knaw.huygens.timbuctoo.crud.NotFoundException;
import nl.knaw.huygens.timbuctoo.crud.TinkerpopJsonCrudService;
import nl.knaw.huygens.timbuctoo.security.AuthorizationException;
import nl.knaw.huygens.timbuctoo.security.LoggedInUserStore;
import nl.knaw.huygens.timbuctoo.security.User;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import java.io.IOException;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static nl.knaw.huygens.timbuctoo.util.JsonBuilder.jsn;
import static nl.knaw.huygens.timbuctoo.util.JsonBuilder.jsnO;

@Path("/v2.1/domain/{collection}")
@Produces(MediaType.APPLICATION_JSON)
public class Index {

  public static URI makeUrl(String collectionName) {
    return UriBuilder.fromResource(Index.class)
                     .buildFromMap(ImmutableMap.of(
                       "collection", collectionName
                     ));
  }

  private final TinkerpopJsonCrudService crudService;
  private final LoggedInUserStore loggedInUserStore;

  public Index(TinkerpopJsonCrudService crudService, LoggedInUserStore loggedInUserStore) {
    this.crudService = crudService;
    this.loggedInUserStore = loggedInUserStore;
  }

  @POST
  public Response createNew(
    @PathParam("collection") String collectionName,
    @HeaderParam("Authorization") String authHeader,
    ObjectNode body
  ) throws URISyntaxException {
    Optional<User> user = loggedInUserStore.userFor(authHeader);
    if (!user.isPresent()) {
      return Response.status(Response.Status.UNAUTHORIZED).build();
    } else {
      try {
        UUID id = crudService.create(collectionName, body, user.get().getId());
        return Response.created(SingleEntity.makeUrl(collectionName, id)).build();
      } catch (InvalidCollectionException e) {
        return Response.status(Response.Status.NOT_FOUND).entity(jsnO("message", jsn(e.getMessage()))).build();
      } catch (IOException e) {
        return Response.status(Response.Status.BAD_REQUEST).entity(jsnO("message", jsn(e.getMessage()))).build();
      } catch (AuthorizationException e) {
        return Response.status(Response.Status.FORBIDDEN).entity(jsnO("message", jsn(e.getMessage()))).build();
      }
    }
  }

  @GET
  public Response list(@PathParam("collection") String collectionName,
                       @QueryParam("rows") @DefaultValue("200") int rows) {

    try {
      List<ObjectNode> jsonNodes = crudService.fetchCollection(collectionName, rows);
      return Response.ok(jsonNodes).build();
    } catch (InvalidCollectionException e) {
      return Response.status(Response.Status.NOT_FOUND).entity(jsnO("message", jsn(e.getMessage()))).build();
    }
  }
}
