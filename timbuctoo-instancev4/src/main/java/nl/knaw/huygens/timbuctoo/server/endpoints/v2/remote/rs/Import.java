package nl.knaw.huygens.timbuctoo.server.endpoints.v2.remote.rs;

import nl.knaw.huygens.timbuctoo.core.TransactionEnforcer;
import nl.knaw.huygens.timbuctoo.model.vre.Vres;
import nl.knaw.huygens.timbuctoo.rdf.RdfImporter;
import nl.knaw.huygens.timbuctoo.remote.rs.download.RemoteFile;
import nl.knaw.huygens.timbuctoo.remote.rs.download.ResourceSyncFileLoader;
import nl.knaw.huygens.timbuctoo.server.TinkerPopGraphManager;
import nl.knaw.huygens.timbuctoo.server.endpoints.v2.bulkupload.VreAuthIniter;
import org.glassfish.jersey.server.ChunkedOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;

import static nl.knaw.huygens.timbuctoo.core.TransactionState.commit;
import static nl.knaw.huygens.timbuctoo.core.TransactionState.rollback;

@Path("/v2.1/remote/rs/discover")
public class Import {

  public static final Logger LOG = LoggerFactory.getLogger(Discover.class);
  private final ResourceSyncFileLoader resourceSyncFileLoader;
  private final TransactionEnforcer transactionEnforcer;
  private final TinkerPopGraphManager graphWrapper;
  private final ExecutorService rdfExecutorService;
  private final VreAuthIniter vreAuthIniter;
  private final Vres vres;

  public Import(ResourceSyncFileLoader resourceSyncFileLoader, TransactionEnforcer transactionEnforcer,
                TinkerPopGraphManager graphWrapper, ExecutorService rdfExecutorService, VreAuthIniter vreAuthIniter,
                Vres vres) {
    this.resourceSyncFileLoader = resourceSyncFileLoader;
    this.transactionEnforcer = transactionEnforcer;
    this.graphWrapper = graphWrapper;
    this.rdfExecutorService = rdfExecutorService;
    this.vreAuthIniter = vreAuthIniter;
    this.vres = vres;
  }

  @POST
  public Response importData(@HeaderParam("Authorization") String authorization, ImportData importData) {
    ChunkedOutput<String> output = new ChunkedOutput<>(String.class);
    return vreAuthIniter.addVreAuthorizations(authorization, importData.vreName)
      .getOrElseGet(vreId -> {
        rdfExecutorService.submit(() -> {
          try {
            transactionEnforcer.execute(timbuctooActions -> {
              timbuctooActions.rdfCleanImportSession(importData.vreName, session -> {
                final RdfImporter rdfImporter = new RdfImporter(graphWrapper, vreId, vres, session);
                try {
                  Iterator<RemoteFile> files = resourceSyncFileLoader.loadFiles(importData.source).iterator();
                  while (files.hasNext()) {
                    RemoteFile file = files.next();
                    LOG.info("processing " + file.getUrl());
                    rdfImporter.importRdf(file.getData());
                  }
                } catch (IOException e) {
                  LOG.error("rdf import failed", e);
                  return rollback();
                }
                return commit();
              });
              return commit();
            });
          } catch (Exception e) {
            LOG.error("An unexpected error occurred ", e);
          } finally {
            try {
              output.close();
            } catch (IOException e) {
              //ignore, we don't care
            }
          }
        });

        return Response.ok(output).build();
      });
  }


  private class ImportData {
    public String source;
    public String name;
    public String vreName;
  }
}
