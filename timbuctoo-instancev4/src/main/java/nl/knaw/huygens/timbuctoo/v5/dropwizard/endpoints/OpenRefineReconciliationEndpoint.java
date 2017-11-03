package nl.knaw.huygens.timbuctoo.v5.dropwizard.endpoints;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import nl.knaw.huygens.timbuctoo.v5.datastores.resourcesync.ResourceSyncException;
import nl.knaw.huygens.timbuctoo.v5.openrefine.Query;
import nl.knaw.huygens.timbuctoo.v5.openrefine.QueryResults;
import nl.knaw.huygens.timbuctoo.v5.openrefine.ReconciliationQueryExecuter;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.Map;

@Path("v5/openrefinereconciliation")
public class OpenRefineReconciliationEndpoint {

  private final ReconciliationQueryExecuter executer;

  public OpenRefineReconciliationEndpoint(ReconciliationQueryExecuter executer) {
    this.executer = executer;
  }

  @POST
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  @Produces("application/json")
  public Response doPost(@FormParam("queries") String message) throws IOException {
    System.err.println(message);
    Map<String, Query> query;
    ObjectMapper objectMapper = new ObjectMapper();
    try {
      query = objectMapper.readValue(message, new TypeReference<Map<String,Query>>() {
      });
      System.err.println("query : " + query);
    } catch (IOException e) {
      return Response.status(400).entity(e.getMessage()).build();
    }

    Map<String, QueryResults> queryResult = executer.excute(query);


    try {
      System.err.println("queryResult: " + objectMapper.writeValueAsString(queryResult));
      return Response.ok(queryResult).build();
    } catch (JsonProcessingException e) {
      e.printStackTrace();
    }

    return Response.serverError().build();
  }

  @GET
  public Response query(@QueryParam("queries") String queries,
                        @QueryParam("callback") String callback) throws ResourceSyncException {

    String result = "{ \"queries\" : \"" + queries + "\" }";
    System.err.println(result);
    if (queries == null) {
      result = "{ \"name\": \"Timbuctoo\", \"view\" : {\"url\": \"http://localhost:8080/v5/openrefinereconciliation/{{id}}\"}, \"defaultTypes\" : [{\"id\":\"/getname\",\"name\":\"Person\"}], \"identifierSpace\": \"http://rdf.freebase.com/ns/authority.netflix.movie\",\"schemaSpace\": \"http://rdf.freebase.com/ns/type.object.id\" }";
    }
    if (callback != null) {
      result = "/**/" + callback + "(" + result + ");";
      System.err.println(result);
      return Response.ok(result, MediaType.TEXT_PLAIN).build();

    }
    System.err.println(result);
    return Response.ok(result, MediaType.APPLICATION_JSON_TYPE).build();
  }

}
