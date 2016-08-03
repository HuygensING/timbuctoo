package nl.knaw.huygens.timbuctoo.server.endpoints.v2.bulkupload;

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
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;


/**
 * This endpoint is the second step for the excel bulk upload.
 * For the first step {@link nl.knaw.huygens.timbuctoo.experimental.server.endpoints.v2.BulkUpload}.
 * This maps the tabular data to a Timbuctoo data model.
 */
@Path("/v2.1/bulk-upload/{vre}/rml/save")
public class SaveRml {

  public static final Logger LOG = LoggerFactory.getLogger(SaveRml.class);
  private final GraphWrapper graphWrapper;

  public SaveRml(GraphWrapper graphWrapper) {
    this.graphWrapper = graphWrapper;

  }

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  public Response mapRml(JsonNode jsonNode, @PathParam("vre") String vreId) {
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
        addPredicateObjectMap(pom, triplesMap);
      });
    });

    return Response.noContent().build();
  }

  private void addPredicateObjectMap(JsonNode pomNode, Vertex triplesMap) {
    LOG.info("Add predicateObjectMap: {}", pomNode);
    JsonNode objectMapNode = pomNode.get("objectMap");
    // TODO refactor
    Graph graph = graphWrapper.getGraph();
    Vertex predicateObjectMap = graph.addVertex("predicateObjectMap");
    triplesMap.addEdge("hasPredicateObjectMap", predicateObjectMap);
    predicateObjectMap.property("predicate", pomNode.get("predicate").asText());

    addObjectMap(objectMapNode, predicateObjectMap);
  }

  private void addObjectMap(JsonNode objectMapNode, Vertex predicateObjectMap) {
    Graph graph = graphWrapper.getGraph();
    Vertex objectMap = graph.addVertex("objectMap");
    predicateObjectMap.addEdge("hasObjectMap", objectMap);

    if (objectMapNode.has("column")) {
      objectMap.property("column", objectMapNode.get("column").asText());
    } else if (objectMapNode.has("reference")) {
      addReference(objectMapNode, objectMap);
    }
  }

  private void addReference(JsonNode objectMapNode, Vertex objectMap) {
    JsonNode referenceNode = objectMapNode.get("reference");
    Vertex reference = graphWrapper.getGraph().addVertex("reference");
    reference.property("parentTriplesMap", referenceNode.get("parentTriplesMap").asText());
    objectMap.addEdge("hasReference", reference);

    JsonNode joinConditionNode = referenceNode.get("joinCondition");
    Vertex joinCondition = graphWrapper.getGraph().addVertex("joinCondition");
    joinCondition.property("child", joinConditionNode.get("child").asText());
    joinCondition.property("parent", joinConditionNode.get("parent").asText());
    reference.addEdge("hasJoinCondition", joinCondition);
  }

  private void addSubject(JsonNode triplesMapNode, Vertex triplesMap) {
    // TODO support full rml spec
    LOG.info("Add subject for triplesMapNode: {}", triplesMapNode);
    Graph graph = graphWrapper.getGraph();
    Vertex subject = graph.addVertex("subjectMap");
    triplesMap.addEdge("hasSubjectMap", subject);
    JsonNode subjectNode = triplesMapNode.get("subjectMap");
    subject.property("class", subjectNode.get("class").asText());
    subject.property("template", subjectNode.get("template").asText());
  }

  private void addLogicalSource(JsonNode triplesMapNode, Vertex triplesMap) {
    LOG.info("Add logical source for triplesMapNode: {}", triplesMapNode);
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
