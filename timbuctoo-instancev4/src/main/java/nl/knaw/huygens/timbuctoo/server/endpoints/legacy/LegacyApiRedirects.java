package nl.knaw.huygens.timbuctoo.server.endpoints.legacy;

import io.dropwizard.jersey.params.UUIDParam;
import nl.knaw.huygens.timbuctoo.server.UriHelper;
import nl.knaw.huygens.timbuctoo.server.endpoints.v2.domain.Index;
import nl.knaw.huygens.timbuctoo.server.endpoints.v2.domain.SingleEntity;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

/**
 * This class redirects from API endpoints that are no longer supported, but whose urls are still cached by google
 * or the handle server to their current counterparts.
 */
public class LegacyApiRedirects {

  private final UriHelper uriHelper;

  public LegacyApiRedirects(UriHelper uriHelper) {
    this.uriHelper = uriHelper;
  }

  @Path("/domain/{collection}/{id}")
  @GET
  public Response singleEntity(@PathParam("collection") String collectionName, @PathParam("id") UUIDParam id,
                               @QueryParam("rev") Integer rev) {
    return Response.status(301)
                   .location(uriHelper.fromResourceUri(SingleEntity.makeUrl(collectionName, id.get(), rev)))
                   .build();
  }

  @Path("/domain/{collection}")
  @GET
  public Response index(@PathParam("collection") String collectionName) {
    return Response.status(301)
                   .location(uriHelper.fromResourceUri(Index.makeUrl(collectionName)))
                   .build();
  }
}
