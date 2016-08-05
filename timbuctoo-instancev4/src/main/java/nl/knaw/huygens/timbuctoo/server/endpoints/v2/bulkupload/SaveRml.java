package nl.knaw.huygens.timbuctoo.server.endpoints.v2.bulkupload;

import com.fasterxml.jackson.databind.JsonNode;
import nl.knaw.huygens.timbuctoo.model.vre.Vre;
import nl.knaw.huygens.timbuctoo.server.UriHelper;
import nl.knaw.huygens.timbuctoo.server.GraphWrapper;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Transaction;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;


/**
 * This endpoint is the second step for the excel bulk upload.
 * For the first step {@link nl.knaw.huygens.timbuctoo.experimental.server.endpoints.v2.BulkUpload}.
 * This maps the tabular data to a Timbuctoo data model.
 */
@Path("/v2.1/bulk-upload/{vre}/rml/save")
public class SaveRml {

  private static final Logger LOG = LoggerFactory.getLogger(SaveRml.class);
  public static final String HAS_TRIPLES_MAP_EDGE_NAME = "hasTriplesMap";
  public static final String HAS_RML_MAPPING_EDGE_NAME = "hasRmlMapping";
  public static final String HAS_LOGICAL_SOURCE_EDGE_NAME = "hasLogicalSource";
  public static final String HAS_SOURCE_EDGE_NAME = "hasSource";
  public static final String TIM_RAW_COLLECTION_PROP_NAME = "tim:rawCollection";
  public static final String TIM_VRE_NAME_PROP_NAME = "tim:vreName";
  public static final String HAS_SUBJECT_MAP_EDGE_NAME = "hasSubjectMap";
  public static final String CLASS_PROP_NAME = "class";
  public static final String TEMPLATE_PROP_NAME = "template";
  public static final String HAS_PREDICATE_OBJECT_MAP_EDGE_NAME = "hasPredicateObjectMap";
  public static final String PREDICATE_PROP_NAME = "predicate";
  public static final String HAS_OBJECT_MAP_EDGE_NAME = "hasObjectMap";
  public static final String COLUMN_PROP_NAME = "column";
  public static final String HAS_REFERENCE_EDGE_NAME = "hasReference";
  public static final String PARENT_TRIPLES_MAP_PROP_NAME = "parentTriplesMap";
  public static final String HAS_JOIN_CONDITION_EDGE_NAME = "hasJoinCondition";
  public static final String CHILD_PROP_NAME = "child";
  public static final String PARENT_PROP_NAME = "parent";
  private final GraphWrapper graphWrapper;
  private final UriHelper uriHelper;

  public SaveRml(GraphWrapper graphWrapper, UriHelper uriHelper) {
    this.graphWrapper = graphWrapper;

    this.uriHelper = uriHelper;
  }

  public URI makeUri(String vreName) {
    URI resourceUri = UriBuilder.fromResource(SaveRml.class).resolveTemplate("vre", vreName).build();
    return uriHelper.fromResourceUri(resourceUri);
  }

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  public Response mapRml(JsonNode jsonNode, @PathParam("vre") String vreId) {
    LOG.debug("Start mapping for vre: {}", vreId);
    if (jsonNode == null) {
      return Response.status(Response.Status.BAD_REQUEST).entity("Body should contain a Json object.").build();
    }

    org.apache.tinkerpop.gremlin.structure.Graph graph = graphWrapper.getGraph();
    GraphTraversal<Vertex, Vertex> vreT = graph.traversal().V()
                                               .hasLabel(Vre.DATABASE_LABEL).has(Vre.VRE_NAME_PROPERTY_NAME, vreId);

    if (!vreT.hasNext()) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }

    Vertex vreVertex = vreT.next();
    Transaction tx = graph.tx();
    if (!tx.isOpen()) {
      tx.open();
    }
    removeOldMapping(vreVertex);
    tx.commit();

    addNewMapping(jsonNode, graph, vreVertex);
    tx.commit();

    return Response.noContent().build();
  }

  private void removeOldMapping(Vertex vreVertex) {
    vreVertex.edges(Direction.OUT, HAS_RML_MAPPING_EDGE_NAME).forEachRemaining(edge -> {
        Vertex rmlMapping = edge.inVertex();
        removeMappingPart(rmlMapping);
        edge.remove();
        rmlMapping.remove();
      }
    );
  }

  private void removeMappingPart(Vertex mappingPart) {
    LOG.debug("Remove old mapping: {}", mappingPart.label());
    mappingPart.edges(Direction.OUT).forEachRemaining(edge -> {
        Vertex subPart = edge.inVertex();
        removeMappingPart(subPart);
        edge.remove();
        subPart.remove();
      }
    );

  }

  private void addNewMapping(JsonNode jsonNode, Graph graph, Vertex vreVertex) {
    Vertex rmlMappingDocument = graph.addVertex("RmlMappingDocument");
    vreVertex.addEdge(HAS_RML_MAPPING_EDGE_NAME, rmlMappingDocument);

    jsonNode.get("@graph").iterator().forEachRemaining(triplesMapNode -> {
      Vertex triplesMap = graph.addVertex("TriplesMap");
      rmlMappingDocument.addEdge(HAS_TRIPLES_MAP_EDGE_NAME, triplesMap);
      triplesMap.property("@id", triplesMapNode.get("@id").asText());
      addLogicalSource(triplesMapNode, triplesMap);
      addSubject(triplesMapNode, triplesMap);
      triplesMapNode.get("predicateObjectMap").iterator().forEachRemaining(pom -> {
        addPredicateObjectMap(pom, triplesMap);
      });
    });
  }

  private void addPredicateObjectMap(JsonNode pomNode, Vertex triplesMap) {
    LOG.debug("Add predicateObjectMap: {}", pomNode);
    JsonNode objectMapNode = pomNode.get("objectMap");
    // TODO refactor
    Graph graph = graphWrapper.getGraph();
    Vertex predicateObjectMap = graph.addVertex("predicateObjectMap");
    triplesMap.addEdge(HAS_PREDICATE_OBJECT_MAP_EDGE_NAME, predicateObjectMap);
    predicateObjectMap.property(PREDICATE_PROP_NAME, pomNode.get("predicate").asText());

    addObjectMap(objectMapNode, predicateObjectMap);
  }

  private void addObjectMap(JsonNode objectMapNode, Vertex predicateObjectMap) {
    Graph graph = graphWrapper.getGraph();
    Vertex objectMap = graph.addVertex("objectMap");
    predicateObjectMap.addEdge(HAS_OBJECT_MAP_EDGE_NAME, objectMap);

    if (objectMapNode.has(COLUMN_PROP_NAME)) {
      objectMap.property("column", objectMapNode.get("column").asText());
    } else if (objectMapNode.has("reference")) {
      addReference(objectMapNode, objectMap);
    }
  }

  private void addReference(JsonNode objectMapNode, Vertex objectMap) {
    JsonNode referenceNode = objectMapNode.get("reference");
    Vertex reference = graphWrapper.getGraph().addVertex("reference");
    reference.property(PARENT_TRIPLES_MAP_PROP_NAME, referenceNode.get("parentTriplesMap").asText());
    objectMap.addEdge(HAS_REFERENCE_EDGE_NAME, reference);

    JsonNode joinConditionNode = referenceNode.get("joinCondition");
    Vertex joinCondition = graphWrapper.getGraph().addVertex("joinCondition");
    joinCondition.property(CHILD_PROP_NAME, joinConditionNode.get("child").asText());
    joinCondition.property(PARENT_PROP_NAME, joinConditionNode.get("parent").asText());
    reference.addEdge(HAS_JOIN_CONDITION_EDGE_NAME, joinCondition);
  }

  private void addSubject(JsonNode triplesMapNode, Vertex triplesMap) {
    // TODO support full rml spec
    LOG.debug("Add subject for triplesMapNode: {}", triplesMapNode);
    Graph graph = graphWrapper.getGraph();
    Vertex subject = graph.addVertex("subjectMap");
    triplesMap.addEdge(HAS_SUBJECT_MAP_EDGE_NAME, subject);
    JsonNode subjectNode = triplesMapNode.get("subjectMap");
    subject.property(CLASS_PROP_NAME, subjectNode.get("class").asText());
    subject.property(TEMPLATE_PROP_NAME, subjectNode.get("template").asText());
  }

  private void addLogicalSource(JsonNode triplesMapNode, Vertex triplesMap) {
    LOG.debug("Add logical source for triplesMapNode: {}", triplesMapNode);
    // TODO support full rml spec
    Graph graph = graphWrapper.getGraph();
    Vertex logicalSource = graph.addVertex("logicalSource");
    triplesMap.addEdge(HAS_LOGICAL_SOURCE_EDGE_NAME, logicalSource);
    Vertex source = graph.addVertex("source");
    logicalSource.addEdge(HAS_SOURCE_EDGE_NAME, source);
    JsonNode sourceNode = triplesMapNode.get("rml:logicalSource").get("rml:source");
    source.property(TIM_RAW_COLLECTION_PROP_NAME, sourceNode.get("tim:rawCollection").asText());
    source.property(TIM_VRE_NAME_PROP_NAME, sourceNode.get("tim:vreName").asText());
  }


}
