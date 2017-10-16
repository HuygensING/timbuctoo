package nl.knaw.huygens.timbuctoo.v5.dropwizard.endpoints;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

import nl.knaw.huygens.timbuctoo.util.UriHelper;
import nl.knaw.huygens.timbuctoo.v5.datastores.resourcesync.ResourceSync;
import nl.knaw.huygens.timbuctoo.v5.datastores.resourcesync.ResourceSyncException;
import nl.knaw.huygens.timbuctoo.v5.openrefine.Query;

@Path("v5/openrefinereconciliation")
public class OpenRefineReconciliationEndpoint {

  private ResourceSync resourceSync;
  private UriHelper uriHelper;

  public OpenRefineReconciliationEndpoint(ResourceSync resourceSync, UriHelper uriHelper) {
    this.resourceSync = resourceSync;
    this.uriHelper = uriHelper;
  }

  @GET
  @Path("{firstName}/{lastName}")
  public Response getname(@PathParam("firstName") String firstName,
                          @PathParam("lastName") String lastName
  ) throws ResourceSyncException {

    String result = "{ \"firstname\" : \"" + firstName + "\" , \"lastname\" : \"" +
      lastName + "\" }";
    return Response.ok(result, MediaType.APPLICATION_JSON_TYPE).build();
  }
}
