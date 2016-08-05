package nl.knaw.huygens.timbuctoo.server.endpoints.v2.bulkupload;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import nl.knaw.huygens.timbuctoo.server.UriHelper;
import nl.knaw.huygens.timbuctoo.server.GraphWrapper;
import nl.knaw.huygens.timbuctoo.server.security.UserPermissionChecker;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.Iterator;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static nl.knaw.huygens.timbuctoo.bulkupload.savers.TinkerpopSaver.FIRST_RAW_PROPERTY_EDGE_NAME;
import static nl.knaw.huygens.timbuctoo.bulkupload.savers.TinkerpopSaver.NEXT_RAW_PROPERTY_EDGE_NAME;
import static nl.knaw.huygens.timbuctoo.bulkupload.savers.TinkerpopSaver.RAW_COLLECTION_EDGE_NAME;
import static nl.knaw.huygens.timbuctoo.bulkupload.savers.TinkerpopSaver.RAW_PROPERTY_NAME;
import static nl.knaw.huygens.timbuctoo.util.JsonBuilder.jsn;
import static nl.knaw.huygens.timbuctoo.util.JsonBuilder.jsnO;

@Path("/v2.1/bulk-upload/{vre}")
public class BulkUploadVre {
  private final GraphWrapper graphWrapper;
  private final UriHelper uriHelper;
  private final RawCollection rawCollection;
  private final SaveRml saveRml;
  private final ExecuteRml executeRml;
  private final UserPermissionChecker permissionChecker;

  public BulkUploadVre(GraphWrapper graphWrapper, UriHelper uriHelper, RawCollection rawCollection, SaveRml saveRml,
                       ExecuteRml executeRml, UserPermissionChecker permissionChecker) {
    this.graphWrapper = graphWrapper;
    this.uriHelper = uriHelper;
    this.rawCollection = rawCollection;
    this.saveRml = saveRml;
    this.executeRml = executeRml;
    this.permissionChecker = permissionChecker;
  }

  public URI createUri(String vre) {
    URI resourceUri = UriBuilder.fromResource(BulkUploadVre.class).resolveTemplate("vre", vre).build();

    return uriHelper.fromResourceUri(resourceUri);
  }

  @GET
  @Produces(APPLICATION_JSON)
  public Response get(@PathParam("vre") String vreName, @HeaderParam("Authorization") String authorizationHeader) {
    UserPermissionChecker.UserPermission permission = permissionChecker.check(vreName, authorizationHeader);

    switch (permission) {
      case UNKNOWN_USER:
        return Response.status(Response.Status.UNAUTHORIZED).build();
      case NO_PERMISSION:
        return Response.status(Response.Status.FORBIDDEN).build();
      case ALLOWED_TO_WRITE:
        break;
      default:
        return Response.status(Response.Status.UNAUTHORIZED).build();
    }
    org.apache.tinkerpop.gremlin.structure.Graph graph = graphWrapper.getGraph();

    GraphTraversal<Vertex, Vertex> vreT =
      graph.traversal().V().hasLabel(nl.knaw.huygens.timbuctoo.model.vre.Vre.DATABASE_LABEL).has(
        nl.knaw.huygens.timbuctoo.model.vre.Vre.VRE_NAME_PROPERTY_NAME, vreName);

    if (!vreT.hasNext()) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }

    Vertex vreVertex = vreT.next();

    ObjectNode result = jsnO("vre", jsn(vreName));
    result.put("saveMapping", saveRml.makeUri(vreName).toString());
    result.put("executeMapping", executeRml.makeUri(vreName).toString());

    ArrayNode collectionArrayNode = result.putArray("collections");
    vreVertex.vertices(Direction.OUT, RAW_COLLECTION_EDGE_NAME)
             .forEachRemaining(v -> addCollection(collectionArrayNode, v, vreName));

    return Response.ok(result).build();
  }

  private void addCollection(ArrayNode collectionArrayNode, Vertex collectionVertex, String vreName) {
    String collectionName = collectionVertex.value("name");
    ObjectNode collection = jsnO("name", jsn(collectionName));

    collection.put("data", rawCollection.makeUri(vreName, collectionName).toString());
    collection.put("dataWithErrors",  rawCollection.makeUri(vreName, collectionName, true).toString());
    collectionArrayNode.add(collection);
    ArrayNode variables = collection.putArray("variables");

    Iterator<Vertex> firstVariableT = collectionVertex.vertices(Direction.OUT, FIRST_RAW_PROPERTY_EDGE_NAME);
    if (firstVariableT.hasNext()) {
      Vertex firstVariable = firstVariableT.next();
      addVariables(variables, firstVariable);
    }
  }


  private void addVariables(ArrayNode variables, Vertex variable) {
    variables.add(jsn(variable.value(RAW_PROPERTY_NAME)));
    Iterator<Vertex> nextVariableT = variable.vertices(Direction.OUT, NEXT_RAW_PROPERTY_EDGE_NAME);
    if (nextVariableT.hasNext()) {
      Vertex next = nextVariableT.next();
      addVariables(variables, next);
    }
  }


}
