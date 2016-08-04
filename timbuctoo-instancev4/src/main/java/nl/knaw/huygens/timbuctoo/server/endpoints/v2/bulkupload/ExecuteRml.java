package nl.knaw.huygens.timbuctoo.server.endpoints.v2.bulkupload;

import nl.knaw.huygens.timbuctoo.model.vre.Vre;
import nl.knaw.huygens.timbuctoo.server.GraphWrapper;
import nl.knaw.huygens.timbuctoo.server.UriHelper;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;

import static nl.knaw.huygens.timbuctoo.experimental.bulkupload.savers.TinkerpopSaver.RAW_COLLECTION_EDGE_NAME;
import static nl.knaw.huygens.timbuctoo.server.endpoints.v2.bulkupload.SaveRml.HAS_RML_MAPPING_EDGE_NAME;

@Path("/v2.1/bulk-upload/{vre}/rml/execute")
public class ExecuteRml {
  private final UriHelper uriHelper;
  private final GraphWrapper graphWrapper;

  public ExecuteRml(UriHelper uriHelper, GraphWrapper graphWrapper) {
    this.uriHelper = uriHelper;
    this.graphWrapper = graphWrapper;
  }

  public URI makeUri(String vreName) {
    URI resourceUri = UriBuilder.fromResource(ExecuteRml.class).resolveTemplate("vre", vreName).build();

    return uriHelper.fromResourceUri(resourceUri);
  }

  @POST
  public Response post(@PathParam("vre") String vreName) {
    Graph graph = graphWrapper.getGraph();
    GraphTraversal<Vertex, Vertex> vreT =
      graph.traversal().V().hasLabel(Vre.DATABASE_LABEL).has(Vre.VRE_NAME_PROPERTY_NAME, vreName);
    if (!vreT.hasNext()) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }
    Vertex vreVertex = vreT.next();
    if (!vreVertex.vertices(Direction.OUT, HAS_RML_MAPPING_EDGE_NAME).hasNext()) {
      return Response.status(Response.Status.PRECONDITION_FAILED)
                     .entity("The VRE is missing an rml mapping to execute.")
                     .build();
    }

    if (!vreVertex.vertices(Direction.OUT, RAW_COLLECTION_EDGE_NAME).hasNext()) {
      return Response.status(Response.Status.PRECONDITION_FAILED)
                     .entity("The VRE is missing raw collections to map.")
                     .build();
    }


    return Response.status(501).entity("Yet to be implemented").build();
  }
}
