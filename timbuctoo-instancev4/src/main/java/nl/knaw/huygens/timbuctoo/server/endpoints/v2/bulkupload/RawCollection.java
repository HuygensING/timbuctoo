package nl.knaw.huygens.timbuctoo.server.endpoints.v2.bulkupload;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import nl.knaw.huygens.timbuctoo.model.vre.Vre;
import nl.knaw.huygens.timbuctoo.server.GraphWrapper;
import nl.knaw.huygens.timbuctoo.util.UriHelper;
import nl.knaw.huygens.timbuctoo.server.security.UserPermissionChecker;
import nl.knaw.huygens.timbuctoo.v5.dropwizard.endpoints.ErrorResponseHelper;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.Iterator;
import java.util.Optional;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static nl.knaw.huygens.timbuctoo.bulkupload.savers.TinkerpopSaver.ERROR_PREFIX;
import static nl.knaw.huygens.timbuctoo.bulkupload.savers.TinkerpopSaver.NEXT_RAW_ITEM_EDGE_NAME;
import static nl.knaw.huygens.timbuctoo.bulkupload.savers.TinkerpopSaver.RAW_COLLECTION_EDGE_NAME;
import static nl.knaw.huygens.timbuctoo.bulkupload.savers.TinkerpopSaver.RAW_COLLECTION_NAME_PROPERTY_NAME;
import static nl.knaw.huygens.timbuctoo.bulkupload.savers.TinkerpopSaver.RAW_ITEM_EDGE_NAME;
import static nl.knaw.huygens.timbuctoo.bulkupload.savers.TinkerpopSaver.VALUE_PREFIX;
import static nl.knaw.huygens.timbuctoo.server.endpoints.v2.bulkupload.BulkUploadedDataSource.HAS_NEXT_ERROR;
import static nl.knaw.huygens.timbuctoo.util.JsonBuilder.jsn;
import static nl.knaw.huygens.timbuctoo.util.JsonBuilder.jsnO;

@Path("/v2.1/bulk-upload/{vre}/raw-collections/{collection}")
public class RawCollection {
  public static final String NUMBER_OF_ITEMS = "rows";
  public static final String START_ID = "startId";
  public static final String ONLY_ERRORS = "onlyErrors";
  private final GraphWrapper graphWrapper;
  private final UriHelper uriHelper;
  private final UserPermissionChecker userPermissionChecker;
  private final ErrorResponseHelper errorResponseHelper;

  public RawCollection(GraphWrapper graphWrapper, UriHelper uriHelper, UserPermissionChecker userPermissionChecker,
                       ErrorResponseHelper errorResponseHelper) {
    this.graphWrapper = graphWrapper;
    this.uriHelper = uriHelper;
    this.userPermissionChecker = userPermissionChecker;
    this.errorResponseHelper = errorResponseHelper;
  }

  public URI makeUri(String vreName, String collectionName, boolean onlyErrors) {
    URI resourceUri = minimumUri(vreName, collectionName)
      .queryParam(ONLY_ERRORS, onlyErrors)
      .build();
    return uriHelper.fromResourceUri(resourceUri);
  }

  public URI makeUri(String vreName, String collectionName) {
    URI resourceUri = minimumUri(vreName, collectionName)
      .build();
    return uriHelper.fromResourceUri(resourceUri);
  }

  private URI createNextLink(String vreName, String collectionName, String startId, int numberOfItems,
                             boolean onlyErrors) {
    URI resourceUri = minimumUri(vreName, collectionName)
      .queryParam(START_ID, startId).queryParam(NUMBER_OF_ITEMS, numberOfItems)
      .queryParam(ONLY_ERRORS, onlyErrors)
      .build();
    return uriHelper.fromResourceUri(resourceUri);
  }

  private UriBuilder minimumUri(String vreName, String collectionName) {
    return UriBuilder.fromResource(RawCollection.class)
                     .resolveTemplate("vre", vreName)
                     .resolveTemplate("collection", collectionName);
  }

  @GET
  @Produces(APPLICATION_JSON)
  public Response get(@PathParam("vre") String vreName, @PathParam("collection") String collectionName,
                      @QueryParam(NUMBER_OF_ITEMS) @DefaultValue("10") int numberOfItems,
                      @QueryParam(START_ID) String startId, @HeaderParam("Authorization") String authorizationString,
                      @QueryParam(ONLY_ERRORS) @DefaultValue("false") boolean onlyErrors) {

    Optional<Response> filterResponse = userPermissionChecker.checkPermissionWithResponse(vreName, authorizationString);

    if (filterResponse.isPresent()) {
      return filterResponse.get();
    }

    GraphTraversal<Vertex, Vertex> collection = graphWrapper.getGraph().traversal().V()
                                                            .hasLabel(Vre.DATABASE_LABEL)
                                                            .has(Vre.VRE_NAME_PROPERTY_NAME, vreName)
                                                            .out(RAW_COLLECTION_EDGE_NAME)
                                                            .has(RAW_COLLECTION_NAME_PROPERTY_NAME, collectionName);

    if (collectionExists(collection)) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }
    if (numberOfItems > 100 || numberOfItems < 1) {
      return errorResponseHelper.error(400, "number of items must be between 1 and 100");
    }

    final String edgeLabel;
    if (onlyErrors) {
      edgeLabel = HAS_NEXT_ERROR;
    } else {
      edgeLabel = NEXT_RAW_ITEM_EDGE_NAME;
    }


    ObjectNode result = jsnO("name", jsn(collectionName));
    ArrayNode items = result.putArray("items");

    Optional<Vertex> lastAddedVertex = getFirstItemTraversal(startId, collection, edgeLabel)
      .emit()
      .repeat(__.out(edgeLabel))
      .times(numberOfItems - 1)
      .dedup()
      .toStream()
      //construct array of results
      .peek(vertex -> {
        ObjectNode values = jsnO();
        ObjectNode errors = jsnO();
        vertex.properties().forEachRemaining(p -> {
          if (p.key().startsWith(VALUE_PREFIX)) {
            values.put(p.key().substring(VALUE_PREFIX.length()), "" + p.value());
          }
          if (p.key().startsWith(ERROR_PREFIX)) {
            errors.put(p.key().substring(ERROR_PREFIX.length()), "" + p.value());
          }
        });
        items.add(jsnO("values", values, "errors", errors));
      })
      //keep reference to the last result
      .reduce((prev, cur) -> cur);


    if (lastAddedVertex.isPresent()) {
      Iterator<Vertex> nextLinkT = lastAddedVertex.get().vertices(Direction.OUT, edgeLabel);
      if (nextLinkT.hasNext()) {
        Vertex nextLinkVertex = nextLinkT.next();
        String id = nextLinkVertex.value("tim_id");
        URI nextLink = createNextLink(vreName, collectionName, id, numberOfItems, onlyErrors);
        result.put("_next", nextLink.toString());
      }
    }

    return Response.ok(result).build();
  }

  private boolean collectionExists(GraphTraversal<Vertex, Vertex> collection) {
    return !collection.asAdmin().clone().hasNext();
  }

  private GraphTraversal<Vertex, Vertex> getFirstItemTraversal(String startId,
                                                               GraphTraversal<Vertex, Vertex> collection,
                                                               String edgeLabel) {
    GraphTraversal<Vertex, Vertex> firstItem;
    if (startId == null) {
      firstItem = collection.out(edgeLabel);
    } else {
      firstItem = collection.out(RAW_ITEM_EDGE_NAME).has("tim_id", startId);
    }
    return firstItem;
  }

}
