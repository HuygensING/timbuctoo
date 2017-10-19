package nl.knaw.huygens.timbuctoo.v5.dropwizard.endpoints;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import nl.knaw.huygens.timbuctoo.util.UriHelper;
import nl.knaw.huygens.timbuctoo.v5.datastores.resourcesync.ResourceSync;
import nl.knaw.huygens.timbuctoo.v5.datastores.resourcesync.ResourceSyncException;

@Path("v5/openrefinereconciliation")
public class OpenRefineReconciliationEndpoint {

  private ResourceSync resourceSync;
  private UriHelper uriHelper;

  public OpenRefineReconciliationEndpoint(ResourceSync resourceSync, UriHelper uriHelper) {
    this.resourceSync = resourceSync;
    this.uriHelper = uriHelper;
  }


  @GET
  @Path("getname/{firstName}")
  public Response getName(@PathParam("firstName") String firstName) throws ResourceSyncException {

    String result = "{ \"firstname\" : \"" + firstName + "\" }";
    return Response.ok(result, MediaType.APPLICATION_JSON_TYPE).build();
  }

  @GET
  @Path("getBirthPlace/{firstName}/{lastName}")
  public Response getBirthPlace(@PathParam("firstName") String firstName,
                          @PathParam("lastName") String lastName
  ) throws ResourceSyncException {

    String result = "{ \"birthplace\" : \"Amsterdam\" }";
    return Response.ok(result, MediaType.APPLICATION_JSON_TYPE).build();
  }

  @GET
  public Response query(@QueryParam("queries") String queries,
      @QueryParam("callback") String callback) throws ResourceSyncException {

    String result = "{ \"queries\" : \"" + queries + "\" }";
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

