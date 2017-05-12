package nl.knaw.huygens.timbuctoo.server.endpoints.v2.remote.rs;

import nl.knaw.huygens.timbuctoo.remote.rs.download.RemoteFile;
import nl.knaw.huygens.timbuctoo.remote.rs.download.ResourceSyncFileLoader;
import nl.knaw.huygens.timbuctoo.server.endpoints.v2.bulkupload.VreAuthIniter;
import nl.knaw.huygens.timbuctoo.v5.filestorage.FileSaver;
import nl.knaw.huygens.timbuctoo.v5.logprocessing.ImportManager;
import nl.knaw.huygens.timbuctoo.v5.logprocessing.LocalData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import java.util.Iterator;
import java.util.Optional;

@Path("/v2.1/remote/rs/import")
public class Import {

  public static final Logger LOG = LoggerFactory.getLogger(Import.class);
  private final ResourceSyncFileLoader resourceSyncFileLoader;
  private final VreAuthIniter vreAuthIniter;
  private final ImportManager importManager;
  private final FileSaver fileSaver;

  public Import(ResourceSyncFileLoader resourceSyncFileLoader, VreAuthIniter vreAuthIniter,
                ImportManager importManager, FileSaver fileSaver) {
    this.resourceSyncFileLoader = resourceSyncFileLoader;
    this.vreAuthIniter = vreAuthIniter;
    this.importManager = importManager;
    this.fileSaver = fileSaver;
  }

  @POST
  public Response importData(@HeaderParam("Authorization") String authorization, ImportData importData) {
    return vreAuthIniter.addVreAuthorizations(authorization, importData.vreName)
                        .getOrElseGet(vreId -> {
                          try {
                            LOG.info("Loading files");
                            Iterator<RemoteFile> files =
                              resourceSyncFileLoader.loadFiles(importData.name).iterator();
                            LOG.info("Found files '{}'", files.hasNext());
                            while (files.hasNext()) {
                              RemoteFile file = files.next();
                              LocalData storedFile = fileSaver.store(
                                file.getMimeType(),
                                Optional.empty(),
                                file.getData()
                              );
                              importManager.addLog(
                                vreId,
                                file.getUrl(),
                                storedFile
                              );
                            }
                            return Response.ok().header("VRE_ID", vreId).build();
                          } catch (Exception e) {
                            LOG.error("Could not read files to import", e);
                            return Response.serverError().build();
                          }
                        });
  }


  public static class ImportData {
    public String source;
    public String name;
    public String vreName;
  }
}
