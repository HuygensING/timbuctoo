package nl.knaw.huygens.timbuctoo.v5.dropwizard.endpoints;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import nl.knaw.huygens.timbuctoo.v5.datastores.resourcesync.ResourceSyncException;
import nl.knaw.huygens.timbuctoo.v5.openrefine.Query;
import nl.knaw.huygens.timbuctoo.v5.openrefine.QueryResults;
import nl.knaw.huygens.timbuctoo.v5.openrefine.ReconciliationQueryExecutor;

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

import static nl.knaw.huygens.timbuctoo.util.JsonBuilder.jsn;
import static nl.knaw.huygens.timbuctoo.util.JsonBuilder.jsnA;
import static nl.knaw.huygens.timbuctoo.util.JsonBuilder.jsnO;

@Path("v5/openrefinereconciliation")
public class OpenRefineReconciliationEndpoint {
  private final ReconciliationQueryExecutor executer;
  private final ObjectMapper objectMapper;

  public OpenRefineReconciliationEndpoint(ReconciliationQueryExecutor executer) {
    objectMapper = new ObjectMapper();
    this.executer = executer;
  }

  @POST
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  @Produces("application/json")
  public Response doPost(@FormParam("queries") String message) throws IOException {
    Map<String, Query> query;
    try {
      query = objectMapper.readValue(message, new TypeReference<Map<String,Query>>() {
      });
    } catch (IOException e) {
      return Response.status(400).entity(e.getMessage()).build();
    }
    Map<String, QueryResults> queryResult = executer.execute(query);
    try {
      String result = objectMapper.writeValueAsString(queryResult);
      // System.err.println("queryResult: " + result);
      return Response.ok(queryResult).build();
    } catch (JsonProcessingException e) {
      e.printStackTrace();
    }

    return Response.serverError().build();
  }

  @GET
  public Response query(@QueryParam("query") String queries,
                        @QueryParam("callback") String callback) throws ResourceSyncException {

    String result = "{ \"query (GET)\" : \"" + queries + "\" }";
    System.err.println(result);
    if (queries == null) {
      // websites need changing
      result = "{ \"name\": \"Timbuctoo\", \"view\" : {\"url\": " +
        "\"http://localhost:8080/v5/openrefinereconciliation/{{id}}\"}, \"defaultTypes\" : " +
        "[{\"id\":\"/getname\",\"name\":\"Person\"}], \"identifierSpace\": " +
        "\"http://rdf.freebase.com/ns/authority.netflix.movie\",\"schemaSpace\": " +
        "\"http://rdf.freebase.com/ns/type.object.id\" }";
    }
    if (callback != null) {
      result = "/**/" + callback + "(" + result + ");";
      // System.err.println(result);
      return Response.ok(result, MediaType.TEXT_PLAIN).build();

    }
    // System.err.println(result);
    return Response.ok(result, MediaType.APPLICATION_JSON_TYPE).build();
  }
}
