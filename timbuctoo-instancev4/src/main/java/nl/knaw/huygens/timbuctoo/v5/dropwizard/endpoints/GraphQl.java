package nl.knaw.huygens.timbuctoo.v5.dropwizard.endpoints;

import com.google.common.collect.ImmutableMap;
import com.sleepycat.je.DatabaseException;
import nl.knaw.huygens.timbuctoo.server.UriHelper;
import nl.knaw.huygens.timbuctoo.v5.dataset.exceptions.RdfProcessingFailedException;
import nl.knaw.huygens.timbuctoo.v5.graphql.GraphQlService;
import nl.knaw.huygens.timbuctoo.v5.graphql.exceptions.GraphQlFailedException;
import nl.knaw.huygens.timbuctoo.v5.graphql.exceptions.GraphQlProcessingException;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;

import static javax.ws.rs.core.Response.ok;

@Path("/v5/{userId}/{dataSet}/graphql")
public class GraphQl {
  private final GraphQlService graphQlService;
  private final UriHelper uriHelper;

  public GraphQl(GraphQlService service, UriHelper uriHelper) throws DatabaseException, RdfProcessingFailedException {
    graphQlService = service;
    this.uriHelper = uriHelper;
  }

  public URI makeUrl(String userId, String dataSetId) {
    return uriHelper.fromResourceUri(UriBuilder.fromResource(this.getClass())
      .buildFromMap(ImmutableMap.of(
        "userId", userId,
        "dataSet", dataSetId
      )));
  }

  @POST
  public Response post(String query, @PathParam("userId") String userId, @PathParam("dataSet") String dataSet) {
    return executeGraphQlQuery(query, userId, dataSet);
  }


  @GET
  public Response get(@QueryParam("query") String query,
                      @PathParam("userId") String userId,
                      @PathParam("dataSet") String dataSet
  ) {
    return executeGraphQlQuery(query, userId, dataSet);
  }

  @Path("csv")
  @GET
  @Produces("text/csv")
  public Response getCsv(@QueryParam("query") String query,
                         @PathParam("userId") String userId,
                         @PathParam("dataSet") String dataSet
  ) {
    return executeGraphQlQuery(query, userId, dataSet);
  }

  private Response executeGraphQlQuery(@QueryParam("query") String query, @PathParam("userId") String userId,
                                       @PathParam("dataSet") String dataSet) {
    try {
      return ok(graphQlService.executeQuery(userId, dataSet, query)).build();
    } catch (GraphQlProcessingException | GraphQlFailedException e) {
      e.printStackTrace();
      return Response.status(500).entity(e.getMessage()).build();
    }
  }


}
