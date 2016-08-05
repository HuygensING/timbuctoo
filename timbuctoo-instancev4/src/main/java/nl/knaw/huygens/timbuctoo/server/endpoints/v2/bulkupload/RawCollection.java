package nl.knaw.huygens.timbuctoo.server.endpoints.v2.bulkupload;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import nl.knaw.huygens.timbuctoo.model.vre.Vre;
import nl.knaw.huygens.timbuctoo.server.UriHelper;
import nl.knaw.huygens.timbuctoo.server.GraphWrapper;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.Iterator;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static nl.knaw.huygens.timbuctoo.bulkupload.savers.TinkerpopSaver.FIRST_RAW_ITEM_EDGE_NAME;
import static nl.knaw.huygens.timbuctoo.bulkupload.savers.TinkerpopSaver.NEXT_RAW_ITEM_EDGE_NAME;
import static nl.knaw.huygens.timbuctoo.bulkupload.savers.TinkerpopSaver.RAW_COLLECTION_EDGE_NAME;
import static nl.knaw.huygens.timbuctoo.bulkupload.savers.TinkerpopSaver.RAW_COLLECTION_NAME_PROPERTY_NAME;
import static nl.knaw.huygens.timbuctoo.bulkupload.savers.TinkerpopSaver.RAW_ITEM_EDGE_NAME;
import static nl.knaw.huygens.timbuctoo.util.JsonBuilder.jsn;
import static nl.knaw.huygens.timbuctoo.util.JsonBuilder.jsnO;

@Path("/v2.1/bulk-upload/{vre}/raw-collections/{collection}")
public class RawCollection {
  public static final String NUMBER_OF_ITEMS = "rows";
  public static final String START_ID = "startId";
  private final GraphWrapper graphWrapper;
  private final UriHelper uriHelper;

  public RawCollection(GraphWrapper graphWrapper, UriHelper uriHelper) {
    this.graphWrapper = graphWrapper;
    this.uriHelper = uriHelper;
  }

  public URI makeUri(String vreName, String collectionName, boolean onlyErrors) {
    URI resourceUri = minimumUri(vreName, collectionName)
                                .queryParam("onlyErrors", onlyErrors)
                                .build();
    return uriHelper.fromResourceUri(resourceUri);
  }

  public URI makeUri(String vreName, String collectionName) {
    URI resourceUri = minimumUri(vreName, collectionName)
                                .build();
    return uriHelper.fromResourceUri(resourceUri);
  }

  private URI createNextLink(String vreName, String collectionName, String startId, int numberOfItems) {
    URI resourceUri = minimumUri(vreName, collectionName)
                                .queryParam(START_ID, startId).queryParam(NUMBER_OF_ITEMS, numberOfItems)
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
                      @QueryParam(START_ID) String startId) {
    GraphTraversal<Vertex, Vertex> collection = graphWrapper.getGraph().traversal().V()
                                                            .hasLabel(Vre.DATABASE_LABEL)
                                                            .has(Vre.VRE_NAME_PROPERTY_NAME, vreName)
                                                            .out(RAW_COLLECTION_EDGE_NAME)
                                                            .has(RAW_COLLECTION_NAME_PROPERTY_NAME, collectionName);

    if (!collection.asAdmin().clone().hasNext()) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }

    ObjectNode result = jsnO("name", jsn(collectionName));
    ArrayNode items = result.putArray("items");

    GraphTraversal<Vertex, Vertex> firstItem = getFirstItemTraversal(startId, collection);

    if (firstItem.hasNext()) {
      Vertex lastAddedVertex = addToArray(items, firstItem.next(), numberOfItems);

      Iterator<Vertex> nextLinkT = lastAddedVertex.vertices(Direction.OUT, NEXT_RAW_ITEM_EDGE_NAME);
      if (nextLinkT.hasNext()) {
        Vertex nextLinkVertex = nextLinkT.next();
        String id = nextLinkVertex.value("tim_id");
        URI nextLink = createNextLink(vreName, collectionName, id, numberOfItems);
        result.put("_next", nextLink.toString());
      }
    }

    return Response.ok(result).build();
  }

  private GraphTraversal<Vertex, Vertex> getFirstItemTraversal(String startId,
                                                               GraphTraversal<Vertex, Vertex> collection) {
    GraphTraversal<Vertex, Vertex> firstItem;
    if (startId == null) {
      firstItem = collection.out(FIRST_RAW_ITEM_EDGE_NAME);
    } else {
      firstItem = collection.out(RAW_ITEM_EDGE_NAME).has("tim_id", startId);
    }
    return firstItem;
  }

  // returns the last added vertex
  private Vertex addToArray(ArrayNode items, Vertex vertex, int numberOfItems) {
    ObjectNode item = jsnO();
    items.add(item);
    vertex.properties().forEachRemaining(p -> item.put(p.label(), "" + p.value()));

    Iterator<Vertex> nextItem = vertex.vertices(Direction.OUT, NEXT_RAW_ITEM_EDGE_NAME);
    if (--numberOfItems > 0 && nextItem.hasNext()) {
      return addToArray(items, nextItem.next(), numberOfItems);
    }
    return vertex;
  }


}
