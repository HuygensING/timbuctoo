package nl.knaw.huygens.timbuctoo.experimental.womenwriters;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.ImmutableMap;
import io.dropwizard.jersey.params.UUIDParam;
import nl.knaw.huygens.timbuctoo.crud.CrudServiceFactory;
import nl.knaw.huygens.timbuctoo.crud.InvalidCollectionException;
import nl.knaw.huygens.timbuctoo.crud.NotFoundException;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.UUID;

import static nl.knaw.huygens.timbuctoo.util.JsonBuilder.jsn;
import static nl.knaw.huygens.timbuctoo.util.JsonBuilder.jsnO;

@Path("/v2.1/womenwritersdomain/{collection}/{id}")
@Produces(MediaType.APPLICATION_JSON)
public class WomenWritersEntityGet {

  public static URI makeUrl(String collectionName, UUID id) {
    return UriBuilder.fromResource(WomenWritersEntityGet.class)
      .buildFromMap(ImmutableMap.of(
        "collection", collectionName,
        "id", id
      ));
  }

  private final CrudServiceFactory crudServiceFactory;

  public WomenWritersEntityGet(CrudServiceFactory crudServiceFactory) {
    this.crudServiceFactory = crudServiceFactory;
  }

  @GET
  public Response get(@PathParam("collection") String collectionName, @PathParam("id") UUIDParam id,
                      @QueryParam("rev") Integer rev) {
    try {
      JsonNode result = crudServiceFactory.newWomenWritersJsonCrudService().get(collectionName, id.get(), rev);
      return Response.ok(result).build();
    } catch (InvalidCollectionException e) {
      return Response.status(Response.Status.NOT_FOUND).entity(jsnO("message", jsn(e.getMessage()))).build();
    } catch (NotFoundException e) {
      return Response.status(Response.Status.NOT_FOUND).entity(jsnO("message", jsn("not found"))).build();
    }
  }
}
