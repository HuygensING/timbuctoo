package nl.knaw.huygens.timbuctoo.server.endpoints.remote.rs;

import nl.knaw.huygens.timbuctoo.remote.rs.download.ResourceSyncFileLoader;
import nl.knaw.huygens.timbuctoo.remote.rs.download.ResourceSyncImport;
import nl.knaw.huygens.timbuctoo.remote.rs.download.exceptions.CantRetrieveFileException;
import nl.knaw.huygens.timbuctoo.dataset.dto.DataSet;
import nl.knaw.huygens.timbuctoo.dataset.exceptions.DataStoreCreationException;
import nl.knaw.huygens.timbuctoo.dropwizard.endpoints.auth.AuthCheck;
import nl.knaw.huygens.timbuctoo.graphql.mutations.ResourceSyncMutationFileHelper;
import nl.knaw.huygens.timbuctoo.graphql.mutations.dto.ResourceSyncReport;
import nl.knaw.huygens.timbuctoo.util.Either;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import java.net.URI;

@Path("/remote/rs/import")
public class Import {
  public static final Logger LOG = LoggerFactory.getLogger(Import.class);
  private final ResourceSyncFileLoader resourceSyncFileLoader;
  private final AuthCheck authCheck;

  public Import(ResourceSyncFileLoader resourceSyncFileLoader, AuthCheck authCheck) {
    this.resourceSyncFileLoader = resourceSyncFileLoader;
    this.authCheck = authCheck;
  }

  @POST
  @Produces("application/json")
  public Response importData(@HeaderParam("Authorization") String authorization,
                             @QueryParam("forceCreation") boolean forceCreation,
                             @QueryParam("dataSetFile") String dataSetFile,
                             @QueryParam("async") @DefaultValue("true") final boolean async,
                             ImportData importData)
      throws DataStoreCreationException {
    final Either<Response, Response> responses = authCheck
        .getOrCreate(authorization, importData.userId, importData.dataSetId, forceCreation)
        .flatMap(userAndDs -> authCheck.allowedToImport(userAndDs.left(), userAndDs.right()))
        .map(userAndDs -> {
          final DataSet dataSet = userAndDs.right();
          try {
            LOG.info("Loading files");

            ResourceSyncReport resourceSyncReport = new ResourceSyncReport();
            ResourceSyncMutationFileHelper fileHelper = new ResourceSyncMutationFileHelper(dataSet, resourceSyncReport);

            ResourceSyncImport resourceSyncImport = new ResourceSyncImport(resourceSyncFileLoader, async);
            resourceSyncImport.filterAndImport(
                importData.source.toString(), dataSetFile, null, null, fileHelper);

            return Response.ok(resourceSyncReport).build();
          } catch (CantRetrieveFileException e) {
            return Response.status(400).entity(e.getMessage()).build();
          } catch (Exception e) {
            LOG.error("Could not read files to import", e);
            return Response.serverError().entity(e).build();
          }
        });
    if (responses.left() != null) {
      return responses.left();
    } else {
      return responses.right();
    }
  }

  public static class ImportData {
    public URI source;
    public String userId;
    public String dataSetId;
  }
}
