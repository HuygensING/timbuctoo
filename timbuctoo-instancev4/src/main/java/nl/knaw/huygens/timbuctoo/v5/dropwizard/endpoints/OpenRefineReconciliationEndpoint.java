package nl.knaw.huygens.timbuctoo.v5.dropwizard.endpoints;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import nl.knaw.huygens.timbuctoo.v5.datastores.resourcesync.ResourceSyncException;
import nl.knaw.huygens.timbuctoo.v5.openrefine.Query;
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
  private final ReconciliationQueryExecutor executor;
  private final ObjectMapper objectMapper;

  public OpenRefineReconciliationEndpoint(ReconciliationQueryExecutor executor) {
    objectMapper = new ObjectMapper();
    this.executor = executor;
  }

  @POST
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  @Produces("application/json")
  public Response doPost(@FormParam("queries") String queries) throws IOException {
    Map<String, Query> query;
    try {
      query = objectMapper.readValue(queries, new TypeReference<Map<String,Query>>() { });
    } catch (IOException e) {
      return Response.status(400).entity(e.getMessage()).build();
    }
    return Response.ok(executor.execute(query)).build();
  }

  @GET
  @Produces("application/json")
  public Response query(@QueryParam("queries") String queries,
                        @QueryParam("callback") String callback) throws ResourceSyncException {
    JsonNode result;
    if (queries != null) {
      result = jsnO("queries", jsn(queries));
    } else {
      result = jsnO(
        "name", jsn("Timbuctoo"),
        "view", jsnO("url", jsn("http://localhost:8080/v5/openrefinereconciliation/{{id}}")),
        "identifierSpace", jsn("http://rdf.freebase.com/ns/authority.netflix.movie"),
        "schemaSpace", jsn("http://rdf.freebase.com/ns/type.object.id"),
        "defaultTypes", jsnA(
          jsnO("id", jsn("/getname"), "name", jsn("Person"))
        )
      );
    }
    if (callback != null) {
      return Response.ok("/**/" + callback + "(" + result.toString() + ");", MediaType.TEXT_PLAIN).build();
    } else {
      return Response.ok(result, MediaType.APPLICATION_JSON_TYPE).build();
    }
  }

}
