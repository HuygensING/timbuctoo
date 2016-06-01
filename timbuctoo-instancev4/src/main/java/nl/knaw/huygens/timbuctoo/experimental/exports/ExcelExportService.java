package nl.knaw.huygens.timbuctoo.experimental.exports;

import nl.knaw.huygens.timbuctoo.model.vre.Vres;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;

public class ExcelExportService {

  private final Vres mappings;

  public ExcelExportService(Vres vres) {
    this.mappings = vres;
  }

  public SXSSFWorkbook export(Iterator<Vertex> vertices, int depth, Optional<List<String>> allowedRelations) {
    SXSSFWorkbook workbook = new SXSSFWorkbook();

    return workbook;
  }
}
