package nl.knaw.huygens.timbuctoo.server.endpoints.v2.bulkupload;

import nl.knaw.huygens.timbuctoo.core.TransactionEnforcer;
import nl.knaw.huygens.timbuctoo.model.vre.Vre;
import nl.knaw.huygens.timbuctoo.model.vre.Vres;
import nl.knaw.huygens.timbuctoo.rml.RmlExecutorService;
import nl.knaw.huygens.timbuctoo.rml.jena.JenaBasedReader;
import nl.knaw.huygens.timbuctoo.rml.rmldata.RmlMappingDocument;
import nl.knaw.huygens.timbuctoo.server.TinkerPopGraphManager;
import nl.knaw.huygens.timbuctoo.server.UriHelper;
import nl.knaw.huygens.timbuctoo.server.security.UserPermissionChecker;
import nl.knaw.huygens.timbuctoo.util.JsonBuilder;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.glassfish.jersey.server.ChunkedOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.Consumes;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;

import static nl.knaw.huygens.timbuctoo.bulkupload.savers.TinkerpopSaver.RAW_COLLECTION_EDGE_NAME;
import static nl.knaw.huygens.timbuctoo.core.TransactionState.commit;
import static nl.knaw.huygens.timbuctoo.util.JsonBuilder.jsn;
import static nl.knaw.huygens.timbuctoo.util.JsonBuilder.jsnA;
import static nl.knaw.huygens.timbuctoo.util.JsonBuilder.jsnO;

@Path("/v2.1/bulk-upload/{vre}/rml/execute")
public class ExecuteRml {
  public static final Logger LOG = LoggerFactory.getLogger(ExecuteRml.class);
  private final UriHelper uriHelper;
  private final TinkerPopGraphManager graphWrapper;
  private final Vres vres;
  private final UserPermissionChecker permissionChecker;
  private final JenaBasedReader rmlBuilder;
  private final DataSourceFactory dataSourceFactory;
  private final TransactionEnforcer transactionEnforcer;

  public ExecuteRml(UriHelper uriHelper, TinkerPopGraphManager graphWrapper, Vres vres, JenaBasedReader rmlBuilder,
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
  @Produces("text/plain")
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

    final Model model = ModelFactory.createDefaultModel();
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

    final ChunkedOutput<String> output = new ChunkedOutput<>(String.class);

    new Thread() {
      public void run() {
        try {
          new RmlExecutorService(transactionEnforcer, vreName, graphWrapper, model, rmlMappingDocument, vres)
            .execute(msg -> {
              try {
                output.write(msg + "\n");
              } catch (IOException e) {
                LOG.error("Could not write to output stream", e);
              }
            });
        } finally {
          try {
            transactionEnforcer.execute(timbuctooActions -> {
              try {
                if (timbuctooActions.hasMappingErrors(vreName)) {
                  timbuctooActions.setVrePublishState(vreName, Vre.PublishStates.MAPPING_CREATION_AFTER_ERRORS);
                  output.write("failure");
                } else {
                  timbuctooActions.setVrePublishState(vreName, Vre.PublishStates.AVAILABLE);
                  output.write("success");
                }
              } catch (IOException e) {
                LOG.error("Could not write to output stream", e);
              }
              return commit();
            });
            output.close();
          } catch (IOException e) {
            LOG.error("Couldn't close the output stream", e);
          }
        }
      }
    }.start();

    return Response.ok().entity(output).build();

  }
}
