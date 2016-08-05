package nl.knaw.huygens.timbuctoo.server.endpoints.v2.bulkupload;

import nl.knaw.huygens.timbuctoo.bulkupload.BulkUploadService;
import nl.knaw.huygens.timbuctoo.bulkupload.InvalidExcelFileException;
import nl.knaw.huygens.timbuctoo.server.UriHelper;
import nl.knaw.huygens.timbuctoo.security.AuthorizationException;
import nl.knaw.huygens.timbuctoo.security.AuthorizationUnavailableException;
import nl.knaw.huygens.timbuctoo.server.endpoints.v2.bulkupload.BulkUploadVre;
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
  private final UriHelper uriHelper;
  private final BulkUploadVre bulkUploadVre;

  public BulkUpload(BulkUploadService uploadService, UriHelper uriHelper, BulkUploadVre bulkUploadVre) {
    this.uploadService = uploadService;
    this.uriHelper = uriHelper;
    this.bulkUploadVre = bulkUploadVre;
  }

  @POST
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  //@Produces("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
  @Produces("text/html")
  public Response uploadExcelFile(
    @FormDataParam("vre") String vre,
    @FormDataParam("file") InputStream fileInputStream) {

    if (vre == null || fileInputStream == null) {
      StringBuilder errorMessageBuilder = new StringBuilder("The following form params are missing:");
      errorMessageBuilder.append(System.getProperty("line.separator"));
      addIfNull(errorMessageBuilder, vre, "vre");
      addIfNull(errorMessageBuilder, fileInputStream, "file");

      return Response.status(Response.Status.BAD_REQUEST).entity(errorMessageBuilder.toString()).build();
    }

    try {
      return Response.ok()
                     .entity(uploadService.saveToDb(vre, fileInputStream))
                     .location(bulkUploadVre.createUri(vre))
                     .build();
    } catch (AuthorizationUnavailableException | AuthorizationException | InvalidExcelFileException e) {
      e.printStackTrace();
      return Response.status(500).build();
    }
  }

  private void addIfNull(StringBuilder messageBuilder, Object parameter, String paramName) {
    if (parameter == null) {
      messageBuilder.append("\"");
      messageBuilder.append(paramName);
      messageBuilder.append("\"");
      messageBuilder.append(",");
    }
  }

}
