package nl.knaw.huygens.timbuctoo.v5.dropwizard.endpoints;

import nl.knaw.huygens.timbuctoo.v5.filestorage.ImageSaver;
import nl.knaw.huygens.timbuctoo.v5.logprocessing.ImportManager;
import nl.knaw.huygens.timbuctoo.v5.logprocessing.exceptions.LogProcessingFailedException;
import nl.knaw.huygens.timbuctoo.v5.logprocessing.exceptions.LogStorageFailedException;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataParam;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.concurrent.ExecutionException;

@Path("/v4/{dataSet}/datafiles")
public class ImageUpload {

  protected final ImportManager importManager;
  private final ImageSaver imageSaver;

  public ImageUpload(ImportManager importManager, ImageSaver imageSaver) {
    this.importManager = importManager;
    this.imageSaver = imageSaver;
  }

  @Consumes(MediaType.MULTIPART_FORM_DATA)
  @POST
  public void upload(@FormDataParam("file") final InputStream rdfInputStream,
                     @FormDataParam("file") final FormDataBodyPart body,
                     @PathParam("dataSet") final String dataSetId)
      throws IOException, LogProcessingFailedException, LogStorageFailedException, ExecutionException,
      InterruptedException {
    URI uri = imageSaver.store(rdfInputStream);
    //FIXME! implement schema for metadata
    // importManager.storeQuads(dataSetId, new Quad[] {
    //   create(uri.toString(), "i")
    // })
    //   .get(); //block until the upload is done
  }
}
