package nl.knaw.huygens.timbuctoo.v5.dropwizard.endpoints;

import nl.knaw.huygens.timbuctoo.v5.datastores.dto.StoreStatus;
import nl.knaw.huygens.timbuctoo.v5.filestorage.FileSaver;
import nl.knaw.huygens.timbuctoo.v5.logprocessing.ImportManager;
import nl.knaw.huygens.timbuctoo.v5.logprocessing.LocalData;
import nl.knaw.huygens.timbuctoo.v5.logprocessing.exceptions.LogProcessingFailedException;
import nl.knaw.huygens.timbuctoo.v5.logprocessing.exceptions.LogStorageFailedException;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataParam;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

@Path("/v4/{dataSet}/datafiles")
public class DataUpload {

  protected final ImportManager importManager;
  private final FileSaver fileSaver;

  public DataUpload(ImportManager importManager, FileSaver fileSaver) {
    this.importManager = importManager;
    this.fileSaver = fileSaver;
  }

  @Consumes(MediaType.MULTIPART_FORM_DATA)
  @POST
  public void upload(@FormDataParam("file") final InputStream rdfInputStream,
                     @FormDataParam("file") final FormDataBodyPart body,
                     @FormDataParam("encoding") final String encoding,
                     @FormDataParam("uri") final URI uri,
                     @PathParam("dataSet") final String dataSetId)
      throws IOException, LogProcessingFailedException, LogStorageFailedException, ExecutionException,
      InterruptedException {
    LocalData data = fileSaver.store(
      body == null || body.getMediaType() == null ?
        Optional.empty() :
        Optional.of(body.getMediaType().toString()),
      Optional.of(Charset.forName(encoding)),
      rdfInputStream
    );
    importManager.addDataFile(dataSetId, uri, data)
      .get(null, null); //block until the upload is done
  }
}
