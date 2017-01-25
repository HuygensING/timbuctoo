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
import nl.knaw.huygens.timbuctoo.solr.Webhooks;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Transaction;
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
import java.util.Map;
import java.util.Optional;

import static nl.knaw.huygens.timbuctoo.bulkupload.savers.TinkerpopSaver.RAW_COLLECTION_EDGE_NAME;
import static nl.knaw.huygens.timbuctoo.core.TransactionState.commit;

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
  private final Webhooks webhooks;

  public ExecuteRml(UriHelper uriHelper, TinkerPopGraphManager graphWrapper, Vres vres, JenaBasedReader rmlBuilder,
                    UserPermissionChecker permissionChecker, DataSourceFactory dataSourceFactory,
                    TransactionEnforcer transactionEnforcer, Webhooks webhooks) {
    this.uriHelper = uriHelper;
    this.graphWrapper = graphWrapper;
    this.vres = vres;
    this.permissionChecker = permissionChecker;
    this.rmlBuilder = rmlBuilder;
    this.dataSourceFactory = dataSourceFactory;
    this.transactionEnforcer = transactionEnforcer;
    this.webhooks = webhooks;
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
    Optional<Response> filterResponse = permissionChecker.checkPermissionWithResponse(vreName, authorizationHeader);

    if (filterResponse.isPresent()) {
      return filterResponse.get();
    }

    if (rdfData == null || rdfData.length() == 0) {
      return Response.status(Response.Status.BAD_REQUEST)
        .entity("failure: Body should contain a Json-LD object.")
        .build();
    }

    final Model model = ModelFactory.createDefaultModel();
    model.read(new ByteArrayInputStream(rdfData.getBytes(StandardCharsets.UTF_8)), null, "JSON-LD");
    final RmlMappingDocument rmlMappingDocument = rmlBuilder.fromRdf(model, dataSourceFactory);
    if (rmlMappingDocument.getErrors().size() > 0) {
      return Response.status(Response.Status.BAD_REQUEST)
        .entity("failure: " + String.join("\nfailure: ", rmlMappingDocument.getErrors()) + "\n")
        .build();
    }

    Graph graph = graphWrapper.getGraph();
    try (Transaction transaction = graph.tx()) {
      GraphTraversal<Vertex, Vertex> vreT =
        graph.traversal().V().hasLabel(Vre.DATABASE_LABEL).has(Vre.VRE_NAME_PROPERTY_NAME, vreName);
      if (!vreT.hasNext()) {
        return Response.status(Response.Status.NOT_FOUND)
          .entity(String.format("failure: VRE with name '%s' cannot be found", vreName))
          .build();
      }
      Vertex vreVertex = vreT.next();

      if (!vreVertex.vertices(Direction.OUT, RAW_COLLECTION_EDGE_NAME).hasNext()) {
        return Response.status(Response.Status.PRECONDITION_FAILED)
          .entity("failure: The VRE is missing raw collections to map.")
          .build();
      }
      transaction.close();
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
                  if (LOG.isDebugEnabled()) {
                    Map<String, Map<String, String>> mappingErrors = timbuctooActions.getMappingErrors(vreName);
                    for (Map.Entry<String, Map<String, String>> vertex : mappingErrors.entrySet()) {
                      LOG.debug(vertex.getKey());
                      for (Map.Entry<String, String> error : vertex.getValue().entrySet()) {
                        LOG.debug("  " + error.getKey() + ": " + error.getValue());
                      }
                    }
                  }
                  timbuctooActions.setVrePublishState(vreName, Vre.PublishState.MAPPING_CREATION_AFTER_ERRORS);
                  output.write("failure");
                } else {
                  timbuctooActions.setVrePublishState(vreName, Vre.PublishState.AVAILABLE);
                  webhooks.startIndexingForVre(vreName);
                  output.write("success");
                }
              } catch (IOException e) {
                LOG.error("Could not write to output stream", e);
              }
              return commit();
            });
            output.close();
            LOG.info("Finished RML import");
          } catch (IOException e) {
            LOG.error("Couldn't close the output stream", e);
          }
        }
      }
    }.start();

    return Response.ok().entity(output).build();
  }
}
