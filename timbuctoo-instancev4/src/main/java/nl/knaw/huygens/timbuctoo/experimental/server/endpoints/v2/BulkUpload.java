package nl.knaw.huygens.timbuctoo.experimental.server.endpoints.v2;

import nl.knaw.huygens.timbuctoo.experimental.bulkupload.BulkUploadService;
import nl.knaw.huygens.timbuctoo.experimental.bulkupload.InvalidExcelFileException;
import nl.knaw.huygens.timbuctoo.security.AuthorizationException;
import nl.knaw.huygens.timbuctoo.security.AuthorizationUnavailableException;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.InputStream;

@Path("/v2.1/bulk-upload")
public class BulkUpload {

  private final BulkUploadService uploadService;

  public BulkUpload(BulkUploadService uploadService) {
    this.uploadService = uploadService;
  }

  @POST
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  //@Produces("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
  @Produces("text/html")
  public Response uploadExcelFile(
    @FormDataParam("vre") String vre,
    @FormDataParam("data") FormDataContentDisposition contentDisposition,
    @FormDataParam("file") InputStream fileInputStream) {
    try {
      return Response.ok().entity(uploadService.saveToDb(vre, fileInputStream)).build();
    } catch (AuthorizationUnavailableException | AuthorizationException | InvalidExcelFileException e) {
      e.printStackTrace();
      return Response.status(500).build();
    }
  }

}
