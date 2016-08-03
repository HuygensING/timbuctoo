package nl.knaw.huygens.timbuctoo.server.endpoints.v2.bulkupload;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import nl.knaw.huygens.timbuctoo.model.vre.Vre;
import nl.knaw.huygens.timbuctoo.server.GraphWrapper;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static nl.knaw.huygens.timbuctoo.experimental.bulkupload.savers.TinkerpopSaver.RAW_COLLECTION_EDGE_NAME;
import static nl.knaw.huygens.timbuctoo.experimental.bulkupload.savers.TinkerpopSaver.RAW_COLLECTION_NAME_PROPERTY_NAME;
import static nl.knaw.huygens.timbuctoo.experimental.bulkupload.savers.TinkerpopSaver.RAW_ITEM_EDGE_NAME;
import static nl.knaw.huygens.timbuctoo.util.JsonBuilder.jsn;
import static nl.knaw.huygens.timbuctoo.util.JsonBuilder.jsnO;

@Path("/v2.1/bulk-upload/{vre}/raw-collections/{collection}")
public class RawCollection {
  private final GraphWrapper graphWrapper;

  public RawCollection(GraphWrapper graphWrapper) {
    this.graphWrapper = graphWrapper;
  }

  @GET
  @Produces(APPLICATION_JSON)
  public Response get(@PathParam("vre") String vreName, @PathParam("collection") String collectionName,
                      @QueryParam("rows") @DefaultValue("10") int numberOfItems) {
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

    collection.out(RAW_ITEM_EDGE_NAME).limit(numberOfItems).forEachRemaining(v -> addToArray(items, v));

    return Response.ok(result).build();
  }

  private void addToArray(ArrayNode items, Vertex vertex) {
    ObjectNode item = jsnO();
    items.add(item);

    vertex.properties().forEachRemaining(p -> item.put(p.label(), "" + p.value()));

  }
}
