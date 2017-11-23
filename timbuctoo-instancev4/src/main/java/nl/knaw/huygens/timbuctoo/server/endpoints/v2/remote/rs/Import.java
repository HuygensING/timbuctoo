package nl.knaw.huygens.timbuctoo.server.endpoints.v2.remote.rs;

import com.google.common.base.Charsets;
import com.google.common.collect.Sets;
import javaslang.control.Either;
import nl.knaw.huygens.timbuctoo.remote.rs.download.RemoteFile;
import nl.knaw.huygens.timbuctoo.remote.rs.download.ResourceSyncFileLoader;
import nl.knaw.huygens.timbuctoo.v5.dataset.ImportManager;
import nl.knaw.huygens.timbuctoo.v5.dataset.dto.DataSet;
import nl.knaw.huygens.timbuctoo.v5.dataset.exceptions.DataStoreCreationException;
import nl.knaw.huygens.timbuctoo.v5.dropwizard.endpoints.auth.AuthCheck;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.Iterator;
import java.util.Optional;
import java.util.Set;

@Path("/v2.1/remote/rs/import")
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
                             ImportData importData)
    throws DataStoreCreationException {

    final Either<Response, Response> responses = authCheck
      .getOrCreate(authorization, importData.userId, importData.dataSetId, forceCreation)
      .flatMap(userAndDs -> authCheck.hasAdminAccess(userAndDs.getLeft(), userAndDs.getRight()))
      .map(userAndDs -> {
        final DataSet dataSet = userAndDs.getRight();
        ImportManager importManager = dataSet.getImportManager();
        try {
          LOG.info("Loading files");
          Iterator<RemoteFile> files =
            resourceSyncFileLoader.loadFiles(importData.source.toString()).iterator();
          LOG.info("Found files '{}'", files.hasNext());
          ResourceSyncResport resourceSyncResport = new ResourceSyncResport();

          while (files.hasNext()) {
            RemoteFile file = files.next();
            MediaType parsedMediatype = MediaType.APPLICATION_OCTET_STREAM_TYPE;
            try {
              parsedMediatype = MediaType.valueOf(file.getMimeType());
            } catch (IllegalArgumentException e) {
              LOG.error("Failed to get mediatype", e);
            }
            if (importManager.isRdfTypeSupported(parsedMediatype)) {
              resourceSyncResport.importedFiles.add(file.getUrl());
              importManager.addLog(
                dataSet.getMetadata().getBaseUri(),
                dataSet.getMetadata().getBaseUri(),
                file.getUrl().substring(file.getUrl().lastIndexOf('/') + 1),
                file.getData(),
                Optional.of(Charsets.UTF_8),
                parsedMediatype
              );
            } else {
              resourceSyncResport.ignoredFiles.add(file.getUrl());
              importManager.addFile(
                file.getData(),
                file.getUrl(),
                parsedMediatype
              );
            }
          }

          return Response.ok(resourceSyncResport).build();
        } catch (Exception e) {
          LOG.error("Could not read files to import", e);
          return Response.serverError().build();
        }
      });
    if (responses.isLeft()) {
      return responses.getLeft();
    } else {
      return responses.get();
    }
  }

  public static class ResourceSyncResport {
    public final Set<String> importedFiles = Sets.newTreeSet();
    public final Set<String> ignoredFiles = Sets.newTreeSet();
  }


  public static class ImportData {
    public URI source;
    public String userId;
    public String dataSetId;
  }
}
