package nl.knaw.huygens.timbuctoo.server.endpoints.v2.domain;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.ImmutableMap;
import nl.knaw.huygens.timbuctoo.crud.InvalidCollectionException;
import nl.knaw.huygens.timbuctoo.crud.TinkerpopJsonCrudService;
import nl.knaw.huygens.timbuctoo.search.AutocompleteService;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.Optional;

import static nl.knaw.huygens.timbuctoo.util.JsonBuilder.jsn;
import static nl.knaw.huygens.timbuctoo.util.JsonBuilder.jsnO;

@Path("/v2.1/domain/{collection}/autocomplete")
@Produces(MediaType.APPLICATION_JSON)
public class Autocomplete {

  public static URI makeUrl(String collectionName) {
    return UriBuilder.fromResource(Autocomplete.class)
      .buildFromMap(ImmutableMap.of(
        "collection", collectionName
      ));
  }

  public static URI makeUrl(String collectionName, Optional<String> token, Optional<String> type) {
    URI uri = makeUrl(collectionName);

    if (type.isPresent()) {
      uri = UriBuilder.fromUri(uri)
        .path(Autocomplete.class, "getWithPath")
        .buildFromMap(ImmutableMap.of("type", type.get()));
    }
    if (token.isPresent()) {
      uri = UriBuilder.fromUri(uri).queryParam("query", "*" + token.get() + "*").build();
    }
    return uri;
  }

  private final TinkerpopJsonCrudService crudService;
  private final AutocompleteService autoCompleteService;


  public Autocomplete(TinkerpopJsonCrudService crudService, AutocompleteService autocompleteService) {
    this.crudService = crudService;
    this.autoCompleteService = autocompleteService;
  }

  @GET
  @Path("/")
  public Response get(@PathParam("collection") String collectionName, @QueryParam("query") Optional<String> query,
                      @QueryParam("type") Optional<String> type) {

    if (collectionName.equals("wwkeywords")) {
      try {
        JsonNode result = crudService.autoComplete(collectionName, query, type);
        return Response.ok(result).build();
      } catch (InvalidCollectionException e) {

        return Response.status(Response.Status.NOT_FOUND).entity(jsnO("message", jsn(e.getMessage()))).build();
      }
    } else {
      JsonNode result = autoCompleteService.search(collectionName, query, type);
      return Response.ok(result).build();
    }
  }

  @GET
  @Path("/{type}")
  public Response getWithPath(@PathParam("collection") String collectionName,
                              @QueryParam("query") Optional<String> query, @PathParam("type") Optional<String> type) {
    try {
      JsonNode result = crudService.autoComplete(collectionName, query, type);
      return Response.ok(result).build();
    } catch (InvalidCollectionException e) {
      return Response.status(Response.Status.NOT_FOUND).entity(jsnO("message", jsn(e.getMessage()))).build();
    }
  }

}
