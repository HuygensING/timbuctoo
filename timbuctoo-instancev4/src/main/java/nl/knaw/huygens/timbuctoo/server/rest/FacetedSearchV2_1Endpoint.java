package nl.knaw.huygens.timbuctoo.server.rest;

import io.dropwizard.jersey.params.UUIDParam;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.Optional;
import java.util.UUID;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Path("/v2.1/search")
@Produces(APPLICATION_JSON)
public class FacetedSearchV2_1Endpoint {

  private Searcher searcher;
  private final SearchResponseV2_1Factory searchResponseV2_1Factory;

  public FacetedSearchV2_1Endpoint(Searcher searcher) {
    this.searcher = searcher;
    searchResponseV2_1Factory = new SearchResponseV2_1Factory();
  }

  @POST
  @Path("wwpersons")
  public Response post(SearchRequestV2_1 searchRequest) {
    UUID uuid = searcher.search(getDescription().createQuery(searchRequest));

    URI uri = createUri(uuid);

    return Response.created(uri).build();
  }

  private URI createUri(UUID uuid) {
    return UriBuilder.fromResource(FacetedSearchV2_1Endpoint.class).path("{id}").build(uuid);
  }

  @GET
  @Path("{id}")
  /*
   * Use UUIDParam instead of UUID, because we want to be explicit to the user of this API the request is not
   * supported. When UUID is used Jersery returns a '404 Not Found' if the request contains a malformed one. The
   * UUIDParam will return a '400 Bad Request' for malformed UUID's.
   * See: http://www.dropwizard.io/0.9.1/dropwizard-jersey/apidocs/io/dropwizard/jersey/params/AbstractParam.html
   * Or: http://codahale.com/what-makes-jersey-interesting-parameter-classes/
   */
  public Response get(@PathParam("id") UUIDParam id,
                      @QueryParam("rows") @DefaultValue("10") int rows,
                      @QueryParam("start") @DefaultValue("0") int start) {
    Optional<SearchResult> searchResult = searcher.getSearchResult(id.get());
    if (searchResult.isPresent()) {
      return Response.ok(searchResponseV2_1Factory.createResponse(searchResult.get(), rows, start)).build();
    }

    return Response
      .status(Response.Status.NOT_FOUND)
      .entity(new NotFoundMessage(id))
      .build();
  }

  private WwPersonSearchDescription getDescription() {
    return new WwPersonSearchDescription();
  }

  private class NotFoundMessage {
    public final String message;
    public final int statusCode = 404;

    public NotFoundMessage(UUIDParam id) {
      message = String.format("No SearchResult with id '%s'", id.get());
    }
  }
}
