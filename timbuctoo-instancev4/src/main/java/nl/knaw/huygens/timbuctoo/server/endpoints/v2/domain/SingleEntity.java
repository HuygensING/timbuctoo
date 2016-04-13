package nl.knaw.huygens.timbuctoo.server.endpoints.v2.domain;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.ImmutableMap;
import io.dropwizard.jersey.params.UUIDParam;
import nl.knaw.huygens.timbuctoo.crud.AlreadyUpdatedException;
import nl.knaw.huygens.timbuctoo.crud.InvalidCollectionException;
import nl.knaw.huygens.timbuctoo.crud.NotFoundException;
import nl.knaw.huygens.timbuctoo.crud.TinkerpopJsonCrudService;
import nl.knaw.huygens.timbuctoo.security.AuthorizationException;
import nl.knaw.huygens.timbuctoo.security.LoggedInUserStore;
import nl.knaw.huygens.timbuctoo.security.User;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.net.URI;
import java.util.Optional;
import java.util.UUID;

import static nl.knaw.huygens.timbuctoo.util.JsonBuilder.jsn;
import static nl.knaw.huygens.timbuctoo.util.JsonBuilder.jsnO;

@Path("/v2.1/domain/{collection}/{id}")
@Produces(MediaType.APPLICATION_JSON)
public class SingleEntity {

  public static URI makeUrl(String collectionName, UUID id) {
    return UriBuilder.fromResource(SingleEntity.class)
      .buildFromMap(ImmutableMap.of(
        "collection", collectionName,
        "id", id
      ));
  }

  public static URI makeUrl(String collectionName, UUID id, Integer rev) {
    if (rev == null) {
      return makeUrl(collectionName, id);
    } else {
      return UriBuilder.fromResource(SingleEntity.class)
        .queryParam("rev", rev)
        .buildFromMap(ImmutableMap.of(
          "collection", collectionName,
          "id", id
        ));
    }
  }

  private final TinkerpopJsonCrudService crudService;
  private final LoggedInUserStore loggedInUserStore;

  public SingleEntity(TinkerpopJsonCrudService crudService, LoggedInUserStore loggedInUserStore) {
    this.crudService = crudService;
    this.loggedInUserStore = loggedInUserStore;
  }

  @GET
  public Response get(@PathParam("collection") String collectionName, @PathParam("id") UUIDParam id) {
    try {
      JsonNode result = crudService.get(collectionName, id.get());
      return Response.ok(result).build();
    } catch (InvalidCollectionException e) {
      return Response.status(Response.Status.NOT_FOUND).entity(jsnO("message", jsn(e.getMessage()))).build();
    } catch (NotFoundException e) {
      return Response.status(Response.Status.NOT_FOUND).entity(jsnO("message", jsn("not found"))).build();
    }
  }

  //FIXME disallow edits from users of a different VRE
  @PUT
  public Response put(@PathParam("collection") String collectionName, @HeaderParam("Authorization") String authHeader,
                      @PathParam("id") UUIDParam id, ObjectNode body) {
    Optional<User> user = loggedInUserStore.userFor(authHeader);
    if (!user.isPresent()) {
      return Response.status(Response.Status.UNAUTHORIZED).build();
    } else {
      try {
        crudService.replace(collectionName, id.get(), body, user.get().getId());
        JsonNode jsonNode = crudService.get(collectionName, id.get());
        return Response.ok(jsonNode).build();
      } catch (InvalidCollectionException e) {
        return Response.status(Response.Status.NOT_FOUND).entity(jsnO("message", jsn(e.getMessage()))).build();
      } catch (NotFoundException e) {
        return Response.status(Response.Status.NOT_FOUND).entity(jsnO("message", jsn("not found"))).build();
      } catch (IOException e) {
        return Response.status(Response.Status.BAD_REQUEST).entity(jsnO("message", jsn(e.getMessage()))).build();
      } catch (AlreadyUpdatedException e) {
        return Response
          .status(Response.Status.EXPECTATION_FAILED)
          .entity(jsnO("message", jsn("Entry was already updated")))
          .build();
      }
    }
  }

  //FIXME disallow deletes from users of a different VRE
  @DELETE
  public Response delete(@PathParam("collection") String collectionName,
                         @HeaderParam("Authorization") String authHeader,
                         @PathParam("id") UUIDParam id) {
    Optional<User> user = loggedInUserStore.userFor(authHeader);
    if (!user.isPresent()) {
      return Response.status(Response.Status.UNAUTHORIZED).build();
    } else {
      try {
        crudService.delete(collectionName, id.get(), user.get().getId());
        return Response.noContent().build();
      } catch (InvalidCollectionException e) {
        return Response.status(Response.Status.NOT_FOUND).entity(jsnO("message", jsn(e.getMessage()))).build();
      } catch (NotFoundException e) {
        return Response.status(Response.Status.NOT_FOUND).entity(jsnO("message", jsn("not found"))).build();
      } catch (AuthorizationException e) {
        return Response.status(Response.Status.FORBIDDEN).entity(jsnO("message", jsn(e.getMessage()))).build();
      }
    }
  }

}
