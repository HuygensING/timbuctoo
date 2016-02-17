package nl.knaw.huygens.timbuctoo.server.rest;

import io.dropwizard.jersey.params.UUIDParam;
import nl.knaw.huygens.timbuctoo.search.SearchDescription;
import nl.knaw.huygens.timbuctoo.search.SearchResult;
import nl.knaw.huygens.timbuctoo.search.SearchStore;
import nl.knaw.huygens.timbuctoo.search.description.SearchDescriptionFactory;
import nl.knaw.huygens.timbuctoo.server.GraphWrapper;
import nl.knaw.huygens.timbuctoo.server.SearchConfig;
import nl.knaw.huygens.timbuctoo.server.rest.search.SearchRequestV2_1;
import nl.knaw.huygens.timbuctoo.server.rest.search.SearchResponseV2_1Factory;

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

  private final SearchResponseV2_1Factory searchResponseFactory;
  private final SearchDescriptionFactory searchDescriptionFactory;
  private final GraphWrapper graphWrapper;
  private SearchStore searchStore;

  public FacetedSearchV2_1Endpoint(SearchConfig searchConfig, GraphWrapper graphWrapper) {
    this.searchStore = new SearchStore(searchConfig.getSearchResultAvailabilityTimeout());
    this.graphWrapper = graphWrapper;
    this.searchResponseFactory = new SearchResponseV2_1Factory(searchConfig);
    searchDescriptionFactory = new SearchDescriptionFactory();
  }

  @POST
  @Path("{entityName: [a-z]+}s")
  public Response post(@PathParam("entityName") String entityName, SearchRequestV2_1 searchRequest) {

    Optional<SearchDescription> description = getDescription(entityName);

    if (description.isPresent()) {
      /*
       * The previous implementation of the faceted search v2.1 did validate the search request. For reasons of time
       * this feature is omitted, until proven necessary. Execute will just ignore unknown facets, full text search
       * fields and sort fields.
       */
      UUID uuid = searchStore.add(description.get().execute(graphWrapper, searchRequest));

      URI uri = createUri(uuid);

      return Response.created(uri).build();
    }

    return Response.status(Response.Status.BAD_REQUEST).entity(new InvalidCollectionMessage(entityName)).build();
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
    Optional<SearchResult> searchResult = searchStore.getSearchResult(id.get());
    if (searchResult.isPresent()) {
      return Response.ok(searchResponseFactory.createResponse(searchResult.get(), rows, start)).build();
    }

    return Response
      .status(Response.Status.NOT_FOUND)
      .entity(new NotFoundMessage(id))
      .build();
  }

  private Optional<SearchDescription> getDescription(String entityName) {
    return searchDescriptionFactory.create(entityName);
  }

  private class NotFoundMessage {
    public final String message;

    public NotFoundMessage(UUIDParam id) {
      message = String.format("No SearchResult with id '%s'", id.get());
    }
  }

  private class InvalidCollectionMessage {
    public final String message;

    public InvalidCollectionMessage(String entityName) {
      message = String.format("'%s' is not a valid collection", entityName);
    }
  }
}
