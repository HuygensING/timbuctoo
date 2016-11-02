package nl.knaw.huygens.timbuctoo.server.endpoints.v2.bulkupload;

import nl.knaw.huygens.timbuctoo.database.TransactionEnforcer;
import nl.knaw.huygens.timbuctoo.model.vre.Vre;
import nl.knaw.huygens.timbuctoo.model.vre.Vres;
import nl.knaw.huygens.timbuctoo.rdf.Database;
import nl.knaw.huygens.timbuctoo.rdf.tripleprocessor.TripleProcessorImpl;
import nl.knaw.huygens.timbuctoo.rml.jena.JenaBasedReader;
import nl.knaw.huygens.timbuctoo.rml.rmldata.RmlMappingDocument;
import nl.knaw.huygens.timbuctoo.server.TinkerpopGraphManager;
import nl.knaw.huygens.timbuctoo.server.UriHelper;
import nl.knaw.huygens.timbuctoo.server.security.UserPermissionChecker;
import nl.knaw.huygens.timbuctoo.util.JsonBuilder;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Transaction;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.Consumes;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.io.ByteArrayInputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static nl.knaw.huygens.timbuctoo.bulkupload.savers.TinkerpopSaver.RAW_COLLECTION_EDGE_NAME;
import static nl.knaw.huygens.timbuctoo.database.TransactionState.commit;
import static nl.knaw.huygens.timbuctoo.database.dto.dataset.Collection.COLLECTION_LABEL_PROPERTY_NAME;
import static nl.knaw.huygens.timbuctoo.database.dto.dataset.Collection.ENTITY_TYPE_NAME_PROPERTY_NAME;
import static nl.knaw.huygens.timbuctoo.model.vre.Vre.HAS_COLLECTION_RELATION_NAME;
import static nl.knaw.huygens.timbuctoo.server.endpoints.v2.bulkupload.BulkUploadedDataSource.HAS_NEXT_ERROR;
import static nl.knaw.huygens.timbuctoo.util.JsonBuilder.jsn;
import static nl.knaw.huygens.timbuctoo.util.JsonBuilder.jsnA;
import static nl.knaw.huygens.timbuctoo.util.JsonBuilder.jsnO;

@Path("/v2.1/bulk-upload/{vre}/rml/execute")
public class ExecuteRml {
  public static final Logger LOG = LoggerFactory.getLogger(ExecuteRml.class);
  private final UriHelper uriHelper;
  private final TinkerpopGraphManager graphWrapper;
  private final Vres vres;
  private final UserPermissionChecker permissionChecker;
  private final JenaBasedReader rmlBuilder;
  private final DataSourceFactory dataSourceFactory;
  private final TransactionEnforcer transactionEnforcer;

  public ExecuteRml(UriHelper uriHelper, TinkerpopGraphManager graphWrapper, Vres vres, JenaBasedReader rmlBuilder,
                    UserPermissionChecker permissionChecker, DataSourceFactory dataSourceFactory,
                    TransactionEnforcer transactionEnforcer) {
    this.uriHelper = uriHelper;
    this.graphWrapper = graphWrapper;
    this.vres = vres;
    this.permissionChecker = permissionChecker;
    this.rmlBuilder = rmlBuilder;
    this.dataSourceFactory = dataSourceFactory;
    this.transactionEnforcer = transactionEnforcer;
  }

  public URI makeUri(String vreName) {
    URI resourceUri = UriBuilder.fromResource(ExecuteRml.class).resolveTemplate("vre", vreName).build();

    return uriHelper.fromResourceUri(resourceUri);
  }

  @POST
  @Consumes("application/ld+json")
  @Produces(MediaType.APPLICATION_JSON)
  public Response post(String rdfData, @PathParam("vre") String vreName,
                       @HeaderParam("Authorization") String authorizationHeader) {
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

    if (rdfData == null || rdfData.length() == 0) {
      return Response.status(Response.Status.BAD_REQUEST)
                     .entity(jsnO(
                       "success", jsn(false),
                       "errors", jsnA(jsn("Body should contain a Json-LD object."))
                     ))
                     .build();
    }

    Model model = ModelFactory.createDefaultModel() ;
    model.read(new ByteArrayInputStream(rdfData.getBytes(StandardCharsets.UTF_8)), null, "JSON-LD");
    final RmlMappingDocument rmlMappingDocument = rmlBuilder.fromRdf(model, dataSourceFactory);
    if (rmlMappingDocument.getErrors().size() > 0) {
      return Response.status(Response.Status.BAD_REQUEST)
                     .entity(jsnO(
                       "success", jsn(false),
                       "errors", jsnA(rmlMappingDocument.getErrors().stream().map(JsonBuilder::jsn))
                     ))
                     .build();
    }

    LOG.info(rmlMappingDocument.toString());

    Graph graph = graphWrapper.getGraph();
    GraphTraversal<Vertex, Vertex> vreT =
      graph.traversal().V().hasLabel(Vre.DATABASE_LABEL).has(Vre.VRE_NAME_PROPERTY_NAME, vreName);
    if (!vreT.hasNext()) {
      return Response.status(Response.Status.NOT_FOUND)
                     .entity(jsnO(
                       "success", jsn(false),
                       "errors", jsnA(jsn(String.format("VRE with name '%s' cannot be found", vreName)))
                     ))
                     .build();
    }
    Vertex vreVertex = vreT.next();

    if (!vreVertex.vertices(Direction.OUT, RAW_COLLECTION_EDGE_NAME).hasNext()) {
      return Response.status(Response.Status.PRECONDITION_FAILED)
                     .entity(jsnO(
                       "success", jsn(false),
                       "errors", jsnA(jsn("The VRE is missing raw collections to map."))
                     ))
                     .build();
    }

    transactionEnforcer.execute(db -> {
      db.ensureVreExists(vreName);
      db.removeCollectionsAndEntities(vreName);
      return commit();
    });
    try (Transaction tx = graphWrapper.getGraph().tx()) {
      if (!tx.isOpen()) {
        tx.open();
      }
      Map<String, String> vreMappings = new HashMap<>();
      Property collectionRefProp = model.getProperty("http://timbuctoo.com/mapping/existingTimbuctooVre");
      Property predicatProp = model.getProperty("http://www.w3.org/ns/r2rml#predicate");
      model.listResourcesWithProperty(collectionRefProp)
           .forEachRemaining(resource -> {
             vreMappings.put(
               resource.getProperty(predicatProp).getObject().asResource().getURI(),
               resource.getProperty(collectionRefProp).getObject().asLiteral().toString()
             );
           });

      final TripleProcessorImpl processor = new TripleProcessorImpl(new Database(graphWrapper), vreMappings);

      //first save the archetype mappings
      model
        .listStatements(
          null,
          model.createProperty("http://www.w3.org/2000/01/rdf-schema#subClassOf"),
          (String) null
        )
        .forEachRemaining(statement ->
          processor.process(vreName, true, new Triple(
            statement.getSubject().asNode(),
            statement.getPredicate().asNode(),
            statement.getObject().asNode()
          ))
        );

      rmlMappingDocument.execute(new LoggingErrorHandler()).forEach(
        (triple) -> processor.process(vreName, true, triple));

      //Give the collections a proper name
      graphWrapper
        .getGraph()
        .traversal()
        .V()
        .hasLabel(Vre.DATABASE_LABEL)
        .has(Vre.VRE_NAME_PROPERTY_NAME, vreName)
        .out(HAS_COLLECTION_RELATION_NAME)
        .forEachRemaining(v -> {
          if (!v.property(COLLECTION_LABEL_PROPERTY_NAME).isPresent()) {
            String typeName = v.value(ENTITY_TYPE_NAME_PROPERTY_NAME);
            v.property(COLLECTION_LABEL_PROPERTY_NAME, typeName.substring(vreName.length()));
          }
        });

      tx.commit();
    }

    vres.reload();

    boolean hasError = graph.traversal().V()
                            .hasLabel(Vre.DATABASE_LABEL)
                            .has(Vre.VRE_NAME_PROPERTY_NAME, vreName)
                            .out(RAW_COLLECTION_EDGE_NAME)
                            .out(HAS_NEXT_ERROR)
                            .hasNext();

    return Response.ok().entity(jsnO("success", jsn(!hasError))).build();
  }
}
