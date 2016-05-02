package nl.knaw.huygens.timbuctoo.server.endpoints.v2;

import nl.knaw.huygens.timbuctoo.bulkupload.BulkUploadService;
import nl.knaw.huygens.timbuctoo.model.vre.Vres;
import nl.knaw.huygens.timbuctoo.security.AuthorizationException;
import nl.knaw.huygens.timbuctoo.security.AuthorizationUnavailableException;
import nl.knaw.huygens.timbuctoo.server.GraphWrapper;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

@Path("/v2.1/bulk-upload")
public class BulkUpload {

  private final Vres mappings;
  private final GraphWrapper wrapper;

  public BulkUpload(Vres mappings, GraphWrapper wrapper) {
    this.mappings = mappings;
    this.wrapper = wrapper;
  }

  @POST
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  @Produces("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
  public byte[] uploadExcelFile(
    @FormDataParam("vre") String vre,
    @FormDataParam("data") FormDataContentDisposition contentDisposition,
    @FormDataParam("file") InputStream fileInputStream) {
    try {
      BulkUploadService uploadService = new BulkUploadService(mappings.getVre(vre), wrapper);

      final Workbook wb = WorkbookFactory.create(fileInputStream);

      final Response.ResponseBuilder response;

      if (uploadService.saveToDb(wb)) {
        response = Response.ok();
      } else {
        response = Response.status(400);
      }
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      wb.write(out);
      return out.toByteArray();
    } catch (AuthorizationUnavailableException | InvalidFormatException | IOException e) {
      //return Response.status(500).entity(e).build();
    } catch (AuthorizationException e) {
      //return Response.status(403).entity(e).build();
    }
    throw new RuntimeException("asdasa");
  }

  @GET
  @Produces("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
  public StreamingOutput getExcelFile(@QueryParam("vre") String vre) {

    BulkUploadService uploadService = new BulkUploadService(mappings.getVre(vre), wrapper);
    return new StreamingOutput() {
      @Override
      public void write(OutputStream outputStream) throws IOException, WebApplicationException {
        uploadService.getEmptyTemplate().write(outputStream);
      }
    };
  }


}
