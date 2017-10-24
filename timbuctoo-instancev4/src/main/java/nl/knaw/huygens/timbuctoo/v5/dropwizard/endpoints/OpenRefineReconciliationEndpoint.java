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
  private static String[] qList = {"q0","q1","q2","q3","q4","q5","q6","q7","q8","q9"};

  public OpenRefineReconciliationEndpoint(ResourceSync resourceSync, UriHelper uriHelper) {
    this.resourceSync = resourceSync;
    this.uriHelper = uriHelper;
  }


  @GET
  @Path("getname/{firstName}")
  public Response getName(@PathParam("firstName") String firstName) throws ResourceSyncException {

    String result = "{ \"firstname\" : \"" + firstName + "\" }";
    System.err.println(result);
    return Response.ok(result, MediaType.APPLICATION_JSON_TYPE).build();
  }

  @GET
  @Path("getBirthPlace/{firstName}/{lastName}")
  public Response getBirthPlace(@PathParam("firstName") String firstName,
                          @PathParam("lastName") String lastName
  ) throws ResourceSyncException {

    String result = "{ \"birthplace\" : \"Amsterdam\" }";
    System.err.println(result);
    return Response.ok(result, MediaType.APPLICATION_JSON_TYPE).build();
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
  
  @POST
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  @Produces("text/plain")
  public String doPost(@FormParam("queries") String message) {
    System.err.println(message);
    //    String translated = URLDecoder.decode(message,"utf8");
    Map<String, Query> result;
    try {
      result = new ObjectMapper().readValue(message, new TypeReference<Map<String,Query>>() {
      });
      System.err.println("message: " + result);
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    //    JsonObject jsonObject = new JsonParser().parse(translated.substring(8)).getAsJsonObject();
    //    System.err.println(jsonObject);
    //    Set<Entry<String, JsonElement>> jsonSet = jsonObject.entrySet();
    //    String result = "{";
    //    JsonObject jsonResult = new JsonObject();
    //    for (Entry<String, JsonElement> jsonItem : jsonSet) {
    //      String key = jsonItem.getKey();
    //      JsonObject value = (JsonObject) jsonItem.getValue();
    //      JsonPrimitive name = value.getAsJsonPrimitive("query");
    //      System.err.println("query: " + name.toString());
    //      // System.err.println("limit: " + value.getAsJsonPrimitive("limit"));
    //
    //      result += "\"" + key + "\": {";
    //      result += "\"result\" : [ { \"id\" : " + key.substring(1) + ",";
    //      result += "\"name\" : " + name.toString() + ",";
    //      result += "\"type\" : [\"String\"] ,";
    //      result += "\"score\" : 1.0 ,";
    //      result += "\"match\" : true";
    //      result += " } ] } ,";
    //      // building the result as a json object should be more easy, but...
    //      JsonObject itemResult = new JsonObject();
    //      itemResult.addProperty("id", key.substring(1));
    //      itemResult.add("name", name);
    //      JsonArray types = new JsonArray();
    //      types.add(new JsonPrimitive("String"));
    //      itemResult.add("type", types);
    //      itemResult.addProperty("score", 1.0);
    //      itemResult.addProperty("match", true);
    //      JsonArray itemArray = new JsonArray();
    //      itemArray.add(itemResult);
    //      JsonObject thisResult = new JsonObject();
    //      thisResult.add("result",itemArray);
    //      jsonResult.add(key, thisResult);
    //    }
    //    result = result.substring(0, result.length() - 2) + " }";
    //    System.err.println(result);
    //    System.err.println(jsonResult.getClass());
    //    System.err.println(jsonResult.toString());
    //    
    //    return jsonResult.toString();
    return "";
  }
}
