package nl.knaw.huygens.timbuctoo.v5.dropwizard.endpoints;

import com.sleepycat.je.DatabaseException;
import nl.knaw.huygens.timbuctoo.v5.graphql.GraphQlService;
import nl.knaw.huygens.timbuctoo.v5.graphql.exceptions.GraphQlFailedException;
import nl.knaw.huygens.timbuctoo.v5.graphql.exceptions.GraphQlProcessingException;
import nl.knaw.huygens.timbuctoo.v5.dataset.exceptions.RdfProcessingFailedException;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

import static javax.ws.rs.core.Response.ok;

@Path("/v5/{userId}/{dataSet}/graphql")
public class GraphQl {
  private final GraphQlService graphQlService;

  public GraphQl(GraphQlService service) throws DatabaseException, RdfProcessingFailedException {
    graphQlService = service;
  }

  @POST
  public Response get(String query, @PathParam("userId") String userId, @PathParam("dataSet") String dataSet) {
    try {
      return ok(graphQlService.executeQuery(userId, dataSet, query)).build();
    } catch (GraphQlProcessingException | GraphQlFailedException e) {
      e.printStackTrace();
      return Response.status(500).entity(e.getMessage()).build();
    }
  }

}
