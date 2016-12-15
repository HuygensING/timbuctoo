package nl.knaw.huygens.timbuctoo.server.endpoints.v2.bulkupload;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.util.RawValue;
import nl.knaw.huygens.timbuctoo.core.TransactionEnforcer;
import nl.knaw.huygens.timbuctoo.core.TransactionStateAndResult;
import nl.knaw.huygens.timbuctoo.model.vre.Vre;
import nl.knaw.huygens.timbuctoo.model.vre.VreMetadata;
import nl.knaw.huygens.timbuctoo.server.GraphWrapper;
import nl.knaw.huygens.timbuctoo.server.UriHelper;
import nl.knaw.huygens.timbuctoo.server.security.UserPermissionChecker;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Iterator;
import java.util.Optional;

import static com.google.common.io.ByteStreams.limit;
import static com.google.common.io.ByteStreams.toByteArray;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static nl.knaw.huygens.timbuctoo.bulkupload.savers.TinkerpopSaver.FIRST_RAW_PROPERTY_EDGE_NAME;
import static nl.knaw.huygens.timbuctoo.bulkupload.savers.TinkerpopSaver.NEXT_RAW_PROPERTY_EDGE_NAME;
import static nl.knaw.huygens.timbuctoo.bulkupload.savers.TinkerpopSaver.RAW_COLLECTION_EDGE_NAME;
import static nl.knaw.huygens.timbuctoo.bulkupload.savers.TinkerpopSaver.RAW_PROPERTY_NAME;
import static nl.knaw.huygens.timbuctoo.bulkupload.savers.TinkerpopSaver.SAVED_MAPPING_STATE;
import static nl.knaw.huygens.timbuctoo.util.JsonBuilder.jsn;
import static nl.knaw.huygens.timbuctoo.util.JsonBuilder.jsnO;

@Path("/v2.1/bulk-upload/{vre}")
public class BulkUploadVre {
  public static final Logger LOG = LoggerFactory.getLogger(BulkUploadVre.class);

  private final GraphWrapper graphWrapper;
  private final UriHelper uriHelper;
  private final RawCollection rawCollection;
  private final ExecuteRml executeRml;
  private final UserPermissionChecker permissionChecker;
  private final SaveRml saveRml;
  private final TransactionEnforcer transactionEnforcer;
  private final int maxFileSize;

  public BulkUploadVre(GraphWrapper graphWrapper, UriHelper uriHelper, RawCollection rawCollection,
                       ExecuteRml executeRml, UserPermissionChecker permissionChecker, SaveRml saveRml,
                       TransactionEnforcer transactionEnforcer, int maxFileSize) {
    this.graphWrapper = graphWrapper;
    this.uriHelper = uriHelper;
    this.rawCollection = rawCollection;
    this.executeRml = executeRml;
    this.permissionChecker = permissionChecker;
    this.saveRml = saveRml;
    this.transactionEnforcer = transactionEnforcer;
    this.maxFileSize = maxFileSize;
  }

  public URI createUri(String vre) {
    URI resourceUri = UriBuilder.fromResource(BulkUploadVre.class).resolveTemplate("vre", vre).build();

    return uriHelper.fromResourceUri(resourceUri);
  }

  @GET
  @Produces(APPLICATION_JSON)
  public Response get(@PathParam("vre") String vreName, @HeaderParam("Authorization") String authorizationHeader) {
    Optional<Response> filterResponse = permissionChecker.checkPermissionWithResponse(vreName, authorizationHeader);

    if (filterResponse.isPresent()) {
      return filterResponse.get();
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
    result.put("executeMapping", executeRml.makeUri(vreName).toString());
    result.put("saveMapping", saveRml.makeUri(vreName).toString());
    if (vreVertex.property(SAVED_MAPPING_STATE).isPresent()) {
      RawValue rawMappingState = new RawValue(vreVertex.<String>property(SAVED_MAPPING_STATE).value());
      result.putRawValue(SAVED_MAPPING_STATE, rawMappingState);
    }
    if (vreVertex.property(Vre.PUBLISH_STATE_PROPERTY_NAME).isPresent()) {
      result.put(Vre.PUBLISH_STATE_PROPERTY_NAME, vreVertex.<String>property(Vre.PUBLISH_STATE_PROPERTY_NAME).value());
    }
    ArrayNode collectionArrayNode = result.putArray("collections");
    vreVertex.vertices(Direction.OUT, RAW_COLLECTION_EDGE_NAME)
             .forEachRemaining(v -> addCollection(collectionArrayNode, v, vreName));

    return Response.ok(result).build();
  }

  @PUT
  @Consumes(APPLICATION_JSON)
  @Produces(APPLICATION_JSON)
  public Response update(String updateJson,
                         @PathParam("vre") String vreName, @HeaderParam("Authorization") String authorizationHeader) {
    Optional<Response> filterResponse = permissionChecker.checkPermissionWithResponse(vreName, authorizationHeader);

    if (filterResponse.isPresent()) {
      return filterResponse.get();
    }

    return transactionEnforcer.executeAndReturn(timbuctooActions -> {
      try {
        final Vre vre = timbuctooActions.getVre(vreName);
        if (vre == null) {
          return TransactionStateAndResult.commitAndReturn(Response.status(Response.Status.NOT_FOUND).build());
        }

        final VreMetadata vreMetadataUpdate = new ObjectMapper().readValue(updateJson, VreMetadata.class);

        timbuctooActions.setVreMetadata(vreName, vreMetadataUpdate);

        return TransactionStateAndResult.commitAndReturn(Response.ok().build());
      } catch (IOException e) {
        return TransactionStateAndResult.commitAndReturn(Response.status(Response.Status.BAD_REQUEST).build());
      }
    });
  }

  @POST
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  @Path("/image")

  public Response uploadImage(@FormDataParam("file") InputStream fileUpload,
                              @FormDataParam("file") FormDataBodyPart body) {

    System.out.println(body.getMediaType());
    return transactionEnforcer.executeAndReturn(timbuctooActions -> {
      try {
        final byte[] uploadedBytes = getUploadedBytes(fileUpload);


        return TransactionStateAndResult.commitAndReturn(Response.ok().build());
      } catch (IOException e) {
        LOG.error("Reading upload failed", e);
        return TransactionStateAndResult.commitAndReturn(Response.status(Response.Status.BAD_REQUEST)
                       .entity(e.getMessage())
                       .build());
      } catch (IllegalArgumentException e) {
        return TransactionStateAndResult.commitAndReturn(Response.status(Response.Status.BAD_REQUEST)
                       .entity(e.getMessage())
                       .build());
      }
    });
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

  private byte[] getUploadedBytes(InputStream fileUpload) throws IOException {
    byte[] bytes;
    bytes = toByteArray(limit(fileUpload, maxFileSize));
    if (fileUpload.read() != -1) {
      throw new IllegalArgumentException("The file may not be larger than " +
        humanReadableByteCount(maxFileSize) + " bytes");
    }
    return bytes;
  }

  private String humanReadableByteCount(long bytes) {
    int unit = 1024;
    if (bytes < unit) {
      return bytes + " B";
    }
    int exp = (int) (Math.log(bytes) / Math.log(unit));
    String pre = ("KMGTPE").charAt(exp - 1) + "iB";
    return String.format("%.2f %s", bytes / Math.pow(unit, exp), pre);
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
