package nl.knaw.huygens.timbuctoo.server.endpoints.v2;

import com.fasterxml.jackson.databind.JsonNode;
import nl.knaw.huygens.timbuctoo.model.vre.Vre;
import nl.knaw.huygens.timbuctoo.server.GraphWrapper;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;


/**
 * This endpoint is the second step for the excel bulk upload.
 * For the first step {@link nl.knaw.huygens.timbuctoo.experimental.server.endpoints.v2.BulkUpload}.
 * This maps the tabular data to a Timbuctoo data model.
 */
@Path("/v2.1/maprml")
public class RmlMapping {

  public static final Logger LOG = LoggerFactory.getLogger(RmlMapping.class);
  private final GraphWrapper graphWrapper;

  public RmlMapping(GraphWrapper graphWrapper) {
    this.graphWrapper = graphWrapper;

  }

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  public Response mapRml(JsonNode jsonNode, @QueryParam("vre") String vreId) {
    LOG.info("Start mapping for vre: {}", vreId);

    org.apache.tinkerpop.gremlin.structure.Graph graph = graphWrapper.getGraph();
    GraphTraversal<Vertex, Vertex> vreT = graph.traversal().V()
                                               .hasLabel(Vre.DATABASE_LABEL).has(Vre.VRE_NAME_PROPERTY_NAME, vreId);

    if (!vreT.hasNext()) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }

    // TODO remove old mapping
    Vertex vreVertex = vreT.next();
    Vertex rmlMappingDocument = graph.addVertex("RmlMappingDocument");
    vreVertex.addEdge("hasRmlMapping", rmlMappingDocument);

    jsonNode.get("@graph").iterator().forEachRemaining(triplesMapNode -> {
      Vertex triplesMap = graph.addVertex("TriplesMap");
      rmlMappingDocument.addEdge("hasTriplesMap", triplesMap);
      triplesMap.property("@id", triplesMapNode.get("@id").asText());

      addLogicalSource(triplesMapNode, triplesMap);
      addSubject(triplesMapNode, triplesMap);
      triplesMapNode.get("predicateObjectMap").iterator().forEachRemaining(pom -> {
        LOG.info("pom: {}", pom);
        addObjectMap(pom, triplesMap);
      });
    });


    return Response.noContent().build();
  }

  private void addObjectMap(JsonNode pomNode, Vertex triplesMap) {
    // pomNode.get("objectNode");

  }

  private void addSubject(JsonNode triplesMapNode, Vertex triplesMap) {
    // TODO support full rml spec
    Graph graph = graphWrapper.getGraph();
    Vertex subject = graph.addVertex("subjectMap");
    triplesMap.addEdge("hasSubjectMap", subject);
    JsonNode subjectNode = triplesMapNode.get("subjectMap");
    subject.property("class", subjectNode.get("class").asText());
    subject.property("template", subjectNode.get("template").asText());
  }

  private void addLogicalSource(JsonNode triplesMapNode, Vertex triplesMap) {
    // TODO support full rml spec
    Graph graph = graphWrapper.getGraph();
    Vertex logicalSource = graph.addVertex("logicalSource");
    triplesMap.addEdge("hasLogicalSource", logicalSource);
    Vertex source = graph.addVertex("source");
    logicalSource.addEdge("hasSource", source);
    JsonNode sourceNode = triplesMapNode.get("rml:logicalSource").get("rml:source");
    source.property("tim:rawCollection", sourceNode.get("tim:rawCollection").asText());
    source.property("tim:vreName", sourceNode.get("tim:vreName").asText());
  }

}
