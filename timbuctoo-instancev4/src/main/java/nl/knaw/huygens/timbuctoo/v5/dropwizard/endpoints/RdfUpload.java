package nl.knaw.huygens.timbuctoo.v5.dropwizard.endpoints;

import nl.knaw.huygens.timbuctoo.v5.datastores.DataStoreFactory;
import nl.knaw.huygens.timbuctoo.v5.logprocessing.FileBasedLog;
import nl.knaw.huygens.timbuctoo.v5.logprocessing.ImportManager;
import nl.knaw.huygens.timbuctoo.v5.logprocessing.QuadHandler;
import nl.knaw.huygens.timbuctoo.v5.logprocessing.datastore.LogMetadata;
import nl.knaw.huygens.timbuctoo.v5.logprocessing.datastore.LogStorage;
import nl.knaw.huygens.timbuctoo.v5.logprocessing.dto.LocalLog;
import nl.knaw.huygens.timbuctoo.v5.logprocessing.exceptions.LogProcessingFailedException;
import nl.knaw.huygens.timbuctoo.v5.rdfreader.implementations.rdf4j.Rdf4jRdfParser;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import static org.apache.poi.util.IOUtils.copy;

@Path("/v4/rdf-upload/{dataSet}")
public class RdfUpload {

  protected final ImportManager importManager;

  public RdfUpload(DataStoreFactory dataStoreFactory) {
    NoOpLogMetadata noOp = new NoOpLogMetadata();
    importManager = new ImportManager(noOp, noOp, new Rdf4jRdfParser(), dataStoreFactory);
  }

  @Consumes(MediaType.MULTIPART_FORM_DATA)
  @POST
  public void upload(@FormDataParam("file") final InputStream rdfInputStream,
                     @FormDataParam("file") final FormDataContentDisposition disposition,
                     @FormDataParam("uri") final URI uri,
                     @PathParam("dataSet") final String dataSetId) throws IOException, LogProcessingFailedException {
    File tempFile = File.createTempFile("timbuctoo-bulkupload-", null, null);
    copy(rdfInputStream, new FileOutputStream(tempFile));

    importManager.addLog(
      dataSetId,
      new FileBasedLog(uri, tempFile.getAbsolutePath(), disposition.getType())
    );
  }

  private class NoOpLogMetadata implements LogMetadata, LogStorage {

    @Override
    public void addLog(String dataSet, URI logUri) {

    }

    @Override
    public LocalLog startOrContinueAppendLog(String dataSet) {
      return null;
    }

    @Override
    public void appendToLogFinished(String dataSet) {

    }

    @Override
    public QuadHandler startWritingToLog(LocalLog log) {
      return null;
    }

    @Override
    public void writeFinished(String dataSet) {

    }
  }
}
