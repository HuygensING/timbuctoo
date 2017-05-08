package nl.knaw.huygens.timbuctoo.v5.dropwizard.endpoints;

import com.sleepycat.je.DatabaseException;
import nl.knaw.huygens.timbuctoo.v5.graphql.GraphQlService;
import nl.knaw.huygens.timbuctoo.v5.graphql.exceptions.GraphQlFailedException;
import nl.knaw.huygens.timbuctoo.v5.graphql.exceptions.GraphQlProcessingException;
import nl.knaw.huygens.timbuctoo.v5.logprocessing.exceptions.LogProcessingFailedException;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

import static javax.ws.rs.core.Response.ok;

@Path("/v4/{dataSet}/graphql")
public class GraphQl {
  private final GraphQlService graphQlService;

  public GraphQl(GraphQlService service) throws DatabaseException, LogProcessingFailedException {
    graphQlService = service;
  }

  @POST
  public Response get(String query, @PathParam("dataSet") String dataSet) {
    try {
      return ok(graphQlService.executeQuery(dataSet, query)).build();
    } catch (GraphQlProcessingException | GraphQlFailedException e) {
      e.printStackTrace();
      return Response.status(500).entity(e.getMessage()).build();
    }
  }

}
