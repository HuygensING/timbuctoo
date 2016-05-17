package nl.knaw.huygens.timbuctoo.experimental.server.endpoints.v2;

import nl.knaw.huygens.timbuctoo.experimental.bulkupload.BulkUploadService;
import nl.knaw.huygens.timbuctoo.security.AuthorizationException;
import nl.knaw.huygens.timbuctoo.security.AuthorizationUnavailableException;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

@Path("/v2.1/bulk-upload")
public class BulkUpload {

  private final BulkUploadService uploadService;

  public BulkUpload(BulkUploadService uploadService) {
    this.uploadService = uploadService;
  }

  @POST
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  @Produces("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
  public byte[] uploadExcelFile(
    @FormDataParam("vre") String vre,
    @FormDataParam("data") FormDataContentDisposition contentDisposition,
    @FormDataParam("file") InputStream fileInputStream) {
    try {

      final XSSFWorkbook wb = new XSSFWorkbook(OPCPackage.open(fileInputStream));

      final Response.ResponseBuilder response;

      if (uploadService.saveToDb(vre, wb)) {
        response = Response.ok();
      } else {
        response = Response.status(400);
      }
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      wb.write(out);
      return out.toByteArray();
      //FIXME: make response work if possible
    } catch (AuthorizationUnavailableException | InvalidFormatException | IOException e) {
      //return Response.status(500).entity(e).build();
    } catch (AuthorizationException e) {
      //return Response.status(403).entity(e).build();
    }
    throw new RuntimeException("asdasa");
  }

  //@GET
  //@Produces("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
  //public StreamingOutput getExcelFile(@QueryParam("vre") String vre) {
  //
  //  return new StreamingOutput() {
  //    @Override
  //    public void write(OutputStream outputStream) throws IOException, WebApplicationException {
  //      uploadService.getEmptyTemplate(vre).write(outputStream);
  //    }
  //  };
  //}


}
