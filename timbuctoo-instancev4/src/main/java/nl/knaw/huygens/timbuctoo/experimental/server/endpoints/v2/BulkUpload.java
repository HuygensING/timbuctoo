package nl.knaw.huygens.timbuctoo.experimental.server.endpoints.v2;

import com.google.common.collect.ImmutableMap;
import nl.knaw.huygens.timbuctoo.experimental.bulkupload.BulkUploadService;
import nl.knaw.huygens.timbuctoo.experimental.bulkupload.InvalidExcelFileException;
import nl.knaw.huygens.timbuctoo.rml.UriHelper;
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

  public BulkUpload(BulkUploadService uploadService, UriHelper uriHelper) {
    this.uploadService = uploadService;
    this.uriHelper = uriHelper;
  }

  @POST
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  //@Produces("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
  @Produces("text/html")
  public Response uploadExcelFile(
    @FormDataParam("vre") String vre,
    @FormDataParam("file") InputStream fileInputStream) {

    if (vre == null || fileInputStream == null) {
      StringBuilder errormessageBuilder = new StringBuilder("The following form params are missing:");
      errormessageBuilder.append(System.getProperty("line.separator"));
      addIfNull(errormessageBuilder, vre, "vre");
      addIfNull(errormessageBuilder, fileInputStream, "file");
      StringBuilder errorMessageBuilder = errormessageBuilder;

      return Response.status(Response.Status.BAD_REQUEST).entity(errorMessageBuilder.toString()).build();
    }

    try {
      return Response.ok()
                     .entity(uploadService.saveToDb(vre, fileInputStream))
                     .location(uriHelper.makeUri(BulkUploadVre.class, ImmutableMap.of("vre", vre)))
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
