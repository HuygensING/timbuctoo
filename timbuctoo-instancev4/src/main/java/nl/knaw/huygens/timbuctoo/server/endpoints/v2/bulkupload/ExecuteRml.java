package nl.knaw.huygens.timbuctoo.server.endpoints.v2.bulkupload;

import nl.knaw.huygens.timbuctoo.rml.DataSource;
import nl.knaw.huygens.timbuctoo.rml.RmlExecutorService;
import nl.knaw.huygens.timbuctoo.rml.jena.JenaBasedReader;
import nl.knaw.huygens.timbuctoo.rml.rmldata.RmlMappingDocument;
import nl.knaw.huygens.timbuctoo.server.UriHelper;
import nl.knaw.huygens.timbuctoo.server.security.UserPermissionChecker;
import nl.knaw.huygens.timbuctoo.solr.Webhooks;
import nl.knaw.huygens.timbuctoo.v5.datastores.DataSetManager;
import nl.knaw.huygens.timbuctoo.v5.datastores.dto.DataStores;
import nl.knaw.huygens.timbuctoo.v5.logprocessing.ImportManager;
import nl.knaw.huygens.timbuctoo.v5.logprocessing.exceptions.LogProcessingFailedException;
import nl.knaw.huygens.timbuctoo.v5.logprocessing.exceptions.LogStorageFailedException;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
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
import java.util.function.BiFunction;

@Path("/v2.1/bulk-upload/{dataSetId}/rml/execute")
public class ExecuteRml {
  public static final Logger LOG = LoggerFactory.getLogger(ExecuteRml.class);
  private final UriHelper uriHelper;
  private final UserPermissionChecker permissionChecker;
  private final JenaBasedReader rmlBuilder;
  private final Webhooks webhooks;
  private final ImportManager importManager;
  private final DataSetManager dataSetManager;

  public ExecuteRml(UriHelper uriHelper, JenaBasedReader rmlBuilder, UserPermissionChecker permissionChecker,
                    Webhooks webhooks, ImportManager importManager, DataSetManager dataSetManager) {
    this.uriHelper = uriHelper;
    this.permissionChecker = permissionChecker;
    this.rmlBuilder = rmlBuilder;
    this.webhooks = webhooks;
    this.importManager = importManager;
    this.dataSetManager = dataSetManager;
  }

  public URI makeUri(String vreName) {
    URI resourceUri = UriBuilder.fromResource(ExecuteRml.class).resolveTemplate("vre", vreName).build();

    return uriHelper.fromResourceUri(resourceUri);
  }

  @POST
  @Consumes("application/ld+json")
  @Produces("text/plain")
  public Response post(String rdfData, @PathParam("dataSetId") String dataSetId,
                       @HeaderParam("Authorization") String authorizationHeader)
      throws IOException, LogProcessingFailedException, LogStorageFailedException {
    if (!dataSetManager.exists(dataSetId)) {
      return Response.status(Response.Status.BAD_REQUEST).entity("Data set does not exist").build();
    }
    DataStores dataStores = dataSetManager.getDataStores(dataSetId);

    Optional<Response> filterResponse = permissionChecker.checkPermissionWithResponse(dataSetId, authorizationHeader);

    if (filterResponse.isPresent()) {
      return filterResponse.get();
    }

    if (rdfData == null || rdfData.length() == 0) {
      return Response.status(Response.Status.BAD_REQUEST)
        .entity("failure: Body should contain a Json-LD object.")
        .build();
    }

    final BiFunction<String, Map<String, String>, DataSource> dataSourceFactory = dataStores.getDataSourceFactory();
    final Model model = ModelFactory.createDefaultModel();
    model.read(new ByteArrayInputStream(rdfData.getBytes(StandardCharsets.UTF_8)), null, "JSON-LD");
    final RmlMappingDocument rmlMappingDocument = rmlBuilder.fromRdf(
      model,
      rdfResource -> {
        return DataSourceDescriptionParser.getDataSourceDescription(rdfResource)
          .map(description -> dataSourceFactory.apply(description.getCollection(), description.getCustomFields()));
      }
    );
    if (rmlMappingDocument.getErrors().size() > 0) {
      return Response.status(Response.Status.BAD_REQUEST)
        .entity("failure: " + String.join("\nfailure: ", rmlMappingDocument.getErrors()) + "\n")
        .build();
    }

    //FIXME! set file processing status in rdf store
    new RmlExecutorService(dataSetId, model, rmlMappingDocument, importManager).execute();


    return Response.ok().build();
  }
}
