package nl.knaw.huygens.timbuctoo.server.endpoints.v2.remote.rs;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Charsets;
import com.google.common.collect.Sets;
import nl.knaw.huygens.timbuctoo.remote.rs.download.RemoteFile;
import nl.knaw.huygens.timbuctoo.remote.rs.download.ResourceSyncFileLoader;
import nl.knaw.huygens.timbuctoo.v5.dataset.DataSetFactory;
import nl.knaw.huygens.timbuctoo.v5.dataset.ImportManager;
import nl.knaw.huygens.timbuctoo.v5.datastores.exceptions.DataStoreCreationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
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
  private final DataSetFactory dataSetFactory;

  public Import(ResourceSyncFileLoader resourceSyncFileLoader, DataSetFactory dataSetFactory) {
    this.resourceSyncFileLoader = resourceSyncFileLoader;
    this.dataSetFactory = dataSetFactory;
  }

  @POST
  @Produces("application/json")
  public Response importData(@HeaderParam("Authorization") String authorization, ImportData importData)
    throws DataStoreCreationException {
    ImportManager importManager = dataSetFactory.createImportManager(importData.userId, importData.dataSetId);
    try {
      LOG.info("Loading files");
      Iterator<RemoteFile> files =
        resourceSyncFileLoader.loadFiles(importData.source.toString()).iterator();
      LOG.info("Found files '{}'", files.hasNext());
      ResourceSyncResport resourceSyncResport = new ResourceSyncResport();

      while (files.hasNext()) {
        RemoteFile file = files.next();
        MediaType parsedMediatype = null;
        try {
          parsedMediatype = MediaType.valueOf(file.getMimeType());
        } catch (IllegalArgumentException e) {
          LOG.error("Failed to get mediatype", e);
        }
        if (importManager.isRdfTypeSupported(parsedMediatype)) {
          resourceSyncResport.importedFiles.add(file.getUrl());
          importManager.addLog(
            URI.create(file.getUrl()),
            file.getData(),
            Optional.of(Charsets.UTF_8),
            Optional.ofNullable(parsedMediatype)
          );
        } else {
          resourceSyncResport.ignoredFiles.add(file.getUrl());
          importManager.addFile(
            file.getData(),
            file.getUrl(),
            Optional.ofNullable(parsedMediatype)
          );
        }
      }

      return Response.ok(resourceSyncResport).build();
    } catch (Exception e) {
      LOG.error("Could not read files to import", e);
      return Response.serverError().build();
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
