package nl.knaw.huygens.timbuctoo.server.endpoints.legacy;

import io.dropwizard.jersey.params.UUIDParam;
import nl.knaw.huygens.timbuctoo.server.UriHelper;
import nl.knaw.huygens.timbuctoo.server.endpoints.v2.domain.Index;
import nl.knaw.huygens.timbuctoo.server.endpoints.v2.domain.SingleEntity;

import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

public class Redirections {

  private final UriHelper uriHelper;

  public Redirections(UriHelper uriHelper) {
    this.uriHelper = uriHelper;
  }

  @Path("/domain/{collection}/{id}")
  public Response singleEntity(@PathParam("collection") String collectionName, @PathParam("id") UUIDParam id,
                               @QueryParam("rev") Integer rev) {
    return Response.status(301)
                   .location(uriHelper.fromResourceUri(SingleEntity.makeUrl(collectionName, id.get(), rev)))
                   .build();
  }

  @Path("/domain/{collection}")
  public Response index(@PathParam("collection") String collectionName) {
    return Response.status(301)
                   .location(uriHelper.fromResourceUri(Index.makeUrl(collectionName)))
                   .build();
  }
}
