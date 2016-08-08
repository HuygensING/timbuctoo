package nl.knaw.huygens.timbuctoo.server.endpoints.v2.bulkupload;

import nl.knaw.huygens.timbuctoo.model.vre.Vre;
import nl.knaw.huygens.timbuctoo.model.vre.Vres;
import nl.knaw.huygens.timbuctoo.rdf.ImportPreparer;
import nl.knaw.huygens.timbuctoo.rdf.TripleImporter;
import nl.knaw.huygens.timbuctoo.rml.DataSourceFactory;
import nl.knaw.huygens.timbuctoo.rml.rmldata.RmlLogicalSource;
import nl.knaw.huygens.timbuctoo.rml.rmldata.RmlMappingDocument;
import nl.knaw.huygens.timbuctoo.rml.rmldata.RrPredicateObjectMap;
import nl.knaw.huygens.timbuctoo.rml.rmldata.RrTriplesMap;
import nl.knaw.huygens.timbuctoo.rml.rmldata.rmlsources.TimbuctooRawCollectionSource;
import nl.knaw.huygens.timbuctoo.server.GraphWrapper;
import nl.knaw.huygens.timbuctoo.server.UriHelper;
import nl.knaw.huygens.timbuctoo.server.security.UserPermissionChecker;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Node_URI;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Transaction;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.structure.VertexProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;

import static nl.knaw.huygens.timbuctoo.bulkupload.savers.TinkerpopSaver.RAW_COLLECTION_EDGE_NAME;
import static nl.knaw.huygens.timbuctoo.server.endpoints.v2.bulkupload.SaveRml.CHILD_PROP_NAME;
import static nl.knaw.huygens.timbuctoo.server.endpoints.v2.bulkupload.SaveRml.CLASS_PROP_NAME;
import static nl.knaw.huygens.timbuctoo.server.endpoints.v2.bulkupload.SaveRml.COLUMN_PROP_NAME;
import static nl.knaw.huygens.timbuctoo.server.endpoints.v2.bulkupload.SaveRml.HAS_JOIN_CONDITION_EDGE_NAME;
import static nl.knaw.huygens.timbuctoo.server.endpoints.v2.bulkupload.SaveRml.HAS_LOGICAL_SOURCE_EDGE_NAME;
import static nl.knaw.huygens.timbuctoo.server.endpoints.v2.bulkupload.SaveRml.HAS_OBJECT_MAP_EDGE_NAME;
import static nl.knaw.huygens.timbuctoo.server.endpoints.v2.bulkupload.SaveRml.HAS_PREDICATE_OBJECT_MAP_EDGE_NAME;
import static nl.knaw.huygens.timbuctoo.server.endpoints.v2.bulkupload.SaveRml.HAS_REFERENCE_EDGE_NAME;
import static nl.knaw.huygens.timbuctoo.server.endpoints.v2.bulkupload.SaveRml.HAS_RML_MAPPING_EDGE_NAME;
import static nl.knaw.huygens.timbuctoo.server.endpoints.v2.bulkupload.SaveRml.HAS_SOURCE_EDGE_NAME;
import static nl.knaw.huygens.timbuctoo.server.endpoints.v2.bulkupload.SaveRml.HAS_SUBJECT_MAP_EDGE_NAME;
import static nl.knaw.huygens.timbuctoo.server.endpoints.v2.bulkupload.SaveRml.HAS_TRIPLES_MAP_EDGE_NAME;
import static nl.knaw.huygens.timbuctoo.server.endpoints.v2.bulkupload.SaveRml.PARENT_PROP_NAME;
import static nl.knaw.huygens.timbuctoo.server.endpoints.v2.bulkupload.SaveRml.PARENT_TRIPLES_MAP_PROP_NAME;
import static nl.knaw.huygens.timbuctoo.server.endpoints.v2.bulkupload.SaveRml.PREDICATE_PROP_NAME;
import static nl.knaw.huygens.timbuctoo.server.endpoints.v2.bulkupload.SaveRml.TEMPLATE_PROP_NAME;
import static nl.knaw.huygens.timbuctoo.server.endpoints.v2.bulkupload.SaveRml.TIM_RAW_COLLECTION_PROP_NAME;
import static nl.knaw.huygens.timbuctoo.server.endpoints.v2.bulkupload.SaveRml.TIM_VRE_NAME_PROP_NAME;

@Path("/v2.1/bulk-upload/{vre}/rml/execute")
public class ExecuteRml {
  public static final Logger LOG = LoggerFactory.getLogger(ExecuteRml.class);
  private final UriHelper uriHelper;
  private final GraphWrapper graphWrapper;
  private final ImportPreparer importPreparer;
  private final Vres vres;
  private final UserPermissionChecker permissionChecker;

  public ExecuteRml(UriHelper uriHelper, GraphWrapper graphWrapper, Vres vres,
                    UserPermissionChecker permissionChecker) {
    this.uriHelper = uriHelper;
    this.graphWrapper = graphWrapper;
    importPreparer = new ImportPreparer(graphWrapper);
    this.vres = vres;
    this.permissionChecker = permissionChecker;
  }

  public URI makeUri(String vreName) {
    URI resourceUri = UriBuilder.fromResource(ExecuteRml.class).resolveTemplate("vre", vreName).build();

    return uriHelper.fromResourceUri(resourceUri);
  }

  @POST
  public Response post(@PathParam("vre") String vreName, @HeaderParam("Authorization") String authorizationHeader) {
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

    Graph graph = graphWrapper.getGraph();
    GraphTraversal<Vertex, Vertex> vreT =
      graph.traversal().V().hasLabel(Vre.DATABASE_LABEL).has(Vre.VRE_NAME_PROPERTY_NAME, vreName);
    if (!vreT.hasNext()) {
      return Response.status(Response.Status.NOT_FOUND)
                     .entity(String.format("VRE with name '%s' cannot be found", vreName))
                     .build();
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

    Vertex mappingVertex = vreVertex.vertices(Direction.OUT, HAS_RML_MAPPING_EDGE_NAME).next();
    final TripleImporter tripleImporter = new TripleImporter(graphWrapper, vreName);
    try (Transaction tx = graphWrapper.getGraph().tx()) {
      importPreparer.setUpAdminVre(); // FIXME find a better place to setup an Admin VRE.

      createMappingDocument(mappingVertex).execute().forEach(tripleImporter::importTriple);
      tx.commit();
    }

    vres.reload();

    return Response.noContent().build();
  }

  private RmlMappingDocument createMappingDocument(Vertex mappingVertex) {
    RmlMappingDocument.Builder builder = RmlMappingDocument.rmlMappingDocument();

    mappingVertex.vertices(Direction.OUT, HAS_TRIPLES_MAP_EDGE_NAME)
                 .forEachRemaining(v -> addTriplesMap(v, builder));
    return builder.build(new DataSourceFactory(graphWrapper));
  }

  private void addTriplesMap(Vertex triplesMapvertex, RmlMappingDocument.Builder document) {
    RrTriplesMap.Builder triplesMap = RrTriplesMap.rrTriplesMap();
    document.withTripleMap(triplesMap);
    triplesMap.withUri(fromUri(triplesMapvertex.value("@id")));

    addLogicalSource(triplesMapvertex, triplesMap);
    addSubjectMap(triplesMapvertex, triplesMap);

    // add Predicate object maps
    triplesMapvertex.vertices(Direction.OUT, HAS_PREDICATE_OBJECT_MAP_EDGE_NAME)
                    .forEachRemaining(vertex -> addPredicateObjectMap(vertex, triplesMap));

  }

  private void addPredicateObjectMap(Vertex pomVertex, RrTriplesMap.Builder triplesMap) {
    RrPredicateObjectMap.Builder predicateMap =
      triplesMap.withPredicateObjectMap().withPredicate(fromUri(pomVertex.value(PREDICATE_PROP_NAME)));

    addObjectMap(pomVertex, predicateMap);

  }

  private void addObjectMap(Vertex pomVertex, RrPredicateObjectMap.Builder predicateObjectMap) {
    Vertex objectMapVertex = pomVertex.vertices(Direction.OUT, HAS_OBJECT_MAP_EDGE_NAME).next();
    VertexProperty<String> columnProp = objectMapVertex.property(COLUMN_PROP_NAME);
    if (columnProp.isPresent()) {
      predicateObjectMap.withColumn(columnProp.value());
    } else if (objectMapVertex.vertices(Direction.OUT, HAS_REFERENCE_EDGE_NAME).hasNext()) {
      addReference(objectMapVertex, predicateObjectMap);
    } else {
      LOG.error("Unknown type ObjectMap with id {}", objectMapVertex.id());
    }
  }

  private void addReference(Vertex objectMapVertex, RrPredicateObjectMap.Builder predicateObjectMap) {
    Vertex reference = objectMapVertex.vertices(Direction.OUT, HAS_REFERENCE_EDGE_NAME).next();
    Vertex joinCondVertex = reference.vertices(Direction.OUT, HAS_JOIN_CONDITION_EDGE_NAME).next();

    predicateObjectMap.withReference()
                      .withParentTriplesMap(reference.value(PARENT_TRIPLES_MAP_PROP_NAME))
                      .withJoinCondition(joinCondVertex.value(CHILD_PROP_NAME), joinCondVertex.value(PARENT_PROP_NAME));
  }

  private Node_URI fromUri(String uriString) {
    return (Node_URI) NodeFactory.createURI(uriString);
  }

  private void addSubjectMap(Vertex triplesMapvertex, RrTriplesMap.Builder triplesMap) {
    Vertex subjectVertex = triplesMapvertex.vertices(Direction.OUT, HAS_SUBJECT_MAP_EDGE_NAME).next();

    triplesMap.withSubjectMap()
              .withClass(fromUri(subjectVertex.value(CLASS_PROP_NAME)))
              .withTemplateTerm(subjectVertex.value(TEMPLATE_PROP_NAME));
  }

  private void addLogicalSource(Vertex triplesMapvertex, RrTriplesMap.Builder triplesMap) {
    Vertex logicalSource = triplesMapvertex.vertices(Direction.OUT, HAS_LOGICAL_SOURCE_EDGE_NAME).next();
    Vertex source = logicalSource.vertices(Direction.OUT, HAS_SOURCE_EDGE_NAME).next();
    String vreName = source.value(TIM_VRE_NAME_PROP_NAME);
    String collectionName = source.value(TIM_RAW_COLLECTION_PROP_NAME);

    triplesMap.withLogicalSource(
      RmlLogicalSource.rrLogicalSource().withSource(new TimbuctooRawCollectionSource(collectionName, vreName))
    );
  }
}
