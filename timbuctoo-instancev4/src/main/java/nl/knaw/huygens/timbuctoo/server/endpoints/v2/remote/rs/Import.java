package nl.knaw.huygens.timbuctoo.server.endpoints.v2.remote.rs;

import com.google.common.base.Charsets;
import com.google.common.collect.Sets;
import nl.knaw.huygens.timbuctoo.remote.rs.download.RemoteFile;
import nl.knaw.huygens.timbuctoo.remote.rs.download.ResourceSyncFileLoader;
import nl.knaw.huygens.timbuctoo.v5.dataset.ImportManager;
import nl.knaw.huygens.timbuctoo.v5.dataset.DataSetRepository;
import nl.knaw.huygens.timbuctoo.v5.dataset.dto.DataSet;
import nl.knaw.huygens.timbuctoo.v5.dataset.exceptions.DataStoreCreationException;
import nl.knaw.huygens.timbuctoo.v5.util.TimbuctooRdfIdHelper;
import nl.knaw.huygens.timbuctoo.v5.dropwizard.endpoints.ErrorResponseHelper;
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
  private final DataSetRepository dataSetRepository;
  private final TimbuctooRdfIdHelper rdfIdHelper;
  private final ErrorResponseHelper errorResponseHelper;

  public Import(ResourceSyncFileLoader resourceSyncFileLoader, DataSetRepository dataSetRepository,
                ErrorResponseHelper errorResponseHelper, TimbuctooRdfIdHelper rdfIdHelper) {
    this.resourceSyncFileLoader = resourceSyncFileLoader;
    this.dataSetRepository = dataSetRepository;
    this.errorResponseHelper = errorResponseHelper;
    this.rdfIdHelper = rdfIdHelper;
  }

  @POST
  @Produces("application/json")
  public Response importData(@HeaderParam("Authorization") String authorization,
                             @QueryParam("forceCreation") boolean forceCreation,
                             ImportData importData)
    throws DataStoreCreationException {
    final Optional<DataSet> dataSetOpt = dataSetRepository.getDataSet(importData.userId, importData.dataSetId);
    final DataSet dataSet;
    if (dataSetOpt.isPresent()) {
      dataSet = dataSetOpt.get();
    } else if (forceCreation) {
      dataSet = dataSetRepository.createDataSet(importData.userId, importData.dataSetId);
    } else {
      return errorResponseHelper.dataSetNotFound(importData.userId, importData.dataSetId);
    }
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
            rdfIdHelper.dataSet(importData.userId, importData.dataSetId),
            rdfIdHelper.dataSet(importData.userId, importData.dataSetId),
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
