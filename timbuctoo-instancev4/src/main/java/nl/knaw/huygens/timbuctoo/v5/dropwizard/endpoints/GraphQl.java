package nl.knaw.huygens.timbuctoo.v5.dropwizard.endpoints;

import com.google.common.collect.ImmutableMap;
import com.sleepycat.je.DatabaseException;
import nl.knaw.huygens.timbuctoo.server.UriHelper;
import nl.knaw.huygens.timbuctoo.v5.dataset.exceptions.RdfProcessingFailedException;
import nl.knaw.huygens.timbuctoo.v5.graphql.GraphQlService;
import nl.knaw.huygens.timbuctoo.v5.graphql.exceptions.GraphQlFailedException;
import nl.knaw.huygens.timbuctoo.v5.graphql.exceptions.GraphQlProcessingException;
import nl.knaw.huygens.timbuctoo.v5.serializable.SerializableResult;

import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.Optional;

import static javax.ws.rs.core.Response.ok;

@Path("/v5/{userId}/{dataSet}/graphql")
public class GraphQl {
  private final GraphQlService graphQlService;
  private final UriHelper uriHelper;
  private final ErrorResponseHelper errorResponseHelper;

  public GraphQl(GraphQlService service, UriHelper uriHelper, ErrorResponseHelper errorResponseHelper)
    throws DatabaseException, RdfProcessingFailedException {
    graphQlService = service;
    this.uriHelper = uriHelper;
    this.errorResponseHelper = errorResponseHelper;
  }

  public URI makeUrl(String userId, String dataSetId) {
    return uriHelper.fromResourceUri(UriBuilder.fromResource(this.getClass())
      .buildFromMap(ImmutableMap.of(
        "userId", userId,
        "dataSet", dataSetId
      )));
  }

  @POST
  public Response post(String query, @HeaderParam("accept") String acceptHeader, @PathParam("userId") String userId,
                       @PathParam("dataSet") String dataSet) {
    if (hasSpecifiedAcceptHeader(acceptHeader)) {
      return Response
        .status(400)
        .entity("Please specify a mimetype in the accept header. For example: application/ld+json")
        .build();
    }
    return executeGraphQlQuery(query, userId, dataSet);
  }

  public boolean hasSpecifiedAcceptHeader(@HeaderParam("accept") String acceptHeader) {
    return acceptHeader == null || acceptHeader.isEmpty() || "*/*".equals(acceptHeader);
  }


  @GET
  public Response get(@HeaderParam("accept") String acceptHeader,
                      @QueryParam("query") String query,
                      @PathParam("userId") String userId,
                      @PathParam("dataSet") String dataSet
  ) {
    if (hasSpecifiedAcceptHeader(acceptHeader)) {
      return Response
        .status(400)
        .entity("Please specify a mimetype in the accept header. For example: application/ld+json")
        .build();
    }
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
      final Optional<SerializableResult> result = graphQlService.executeQuery(userId, dataSet, query);
      if (result.isPresent()) {
        return ok(result.get()).build();
      } else {
        return errorResponseHelper.dataSetNotFound(userId, dataSet);
      }
    } catch (GraphQlProcessingException | GraphQlFailedException e) {
      e.printStackTrace();
      return Response.status(500).entity(e.getMessage()).build();
    }
  }

}
