package nl.knaw.huygens.timbuctoo.server.endpoints.v2.system;

import com.fasterxml.jackson.databind.node.ArrayNode;
import nl.knaw.huygens.timbuctoo.crud.NotFoundException;
import nl.knaw.huygens.timbuctoo.experimental.exports.excel.ExcelExportService;
import nl.knaw.huygens.timbuctoo.model.properties.JsonMetadata;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

@Path("/v2.1/system/vres")
@Produces(MediaType.APPLICATION_JSON)
public class VresEndpoint {

  private final JsonMetadata jsonMetadata;
  private final ExcelExportService excelExportService;

  public VresEndpoint(JsonMetadata jsonMetadata, ExcelExportService excelExportService) {

    this.jsonMetadata = jsonMetadata;
    this.excelExportService = excelExportService;
  }


  @GET
  public Response get() {
    ArrayNode result = jsonMetadata.listVres();
    return Response.ok(result).build();
  }

  @GET
  @Path("{vreId}/xls")
  @Produces("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
  public Response exportExcel(@PathParam("vreId") String vreId) {
    SXSSFWorkbook workbook = new SXSSFWorkbook();
    try {
      workbook = excelExportService.exportVre(vreId);
    } catch (NotFoundException e) {
      workbook.createSheet("result").createRow(0).createCell(0).setCellValue("VRE not found: " + vreId);
    }

    return Response.ok((StreamingOutput) workbook::write)
                   .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"result.xlsx\"")
                   .build();
  }

}
