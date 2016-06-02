package nl.knaw.huygens.timbuctoo.experimental.exports.excel;

import nl.knaw.huygens.timbuctoo.model.vre.Vres;
import nl.knaw.huygens.timbuctoo.server.GraphWrapper;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.util.List;

public class ExcelExportService {

  private final Vres mappings;
  private final GraphWrapper graphWrapper;


  public ExcelExportService(Vres vres, GraphWrapper graphWrapper) {
    this.mappings = vres;
    this.graphWrapper = graphWrapper;
  }


  /**
   * Exports a list of vertices as excel workbook
   * TODO: this operation should move to class representing a workbook having instances of sheets
   * @param vertices the vertices to export
   * @return the export as workbook
   */
  public SXSSFWorkbook toExcel(List<Vertex> vertices, String type) {
    SXSSFWorkbook workbook = new SXSSFWorkbook();
    new EntitySheet(workbook.createSheet(type), graphWrapper, mappings).renderToSheet(vertices);

    // Assumption: list of vertices contains one type
    // ensure uniqueness via Set<UUID> loadedTimIds after traversing?



    // TODO: extract method for sheet per type.
    // TODO: relate sheets through edges.

    return workbook;
  }
}
