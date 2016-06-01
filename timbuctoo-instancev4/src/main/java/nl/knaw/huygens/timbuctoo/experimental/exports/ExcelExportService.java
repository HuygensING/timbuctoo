package nl.knaw.huygens.timbuctoo.experimental.exports;

import nl.knaw.huygens.timbuctoo.model.properties.LocalProperty;
import nl.knaw.huygens.timbuctoo.model.vre.Vres;
import nl.knaw.huygens.timbuctoo.server.GraphWrapper;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ExcelExportService {

  private final Vres mappings;
  private final GraphWrapper graphWrapper;

  public ExcelExportService(Vres vres, GraphWrapper graphWrapper) {
    this.mappings = vres;
    this.graphWrapper = graphWrapper;
  }


  /**
   * Exports a list of vertices as excel workbook
   * @param vertices the vertices to export
   * @return the export as workbook
   */
  public SXSSFWorkbook toExcel(List<Vertex> vertices, String type) {
    SXSSFWorkbook workbook = new SXSSFWorkbook();
    // Assumption: list of vertices contains one type
    // ensure uniqueness via Set<UUID> loadedTimIds after traversing?

    // 1) read the mappings to get the properties to export
    Map<String, LocalProperty> mapping = mappings.getCollectionForType(type).get().getWriteableProperties();
    GraphTraversal<Vertex, Vertex> entities = graphWrapper.getGraph().traversal().V(vertices);

    // 2) traverse vertices to determine amount of cols needed for (properties in) the sheet

    entities.asAdmin().clone().map(entityT -> {
      List<GraphTraversal> propertyGetters = mapping
        .entrySet().stream()
        .map(prop -> prop.getValue().getExcelDescription().sideEffect(x -> {
          ExcelDescription excelDescription = x.get().get();
          // TODO: map amount of cols.
        })).collect(Collectors.toList());

      graphWrapper.getGraph().traversal().V(entityT.get().id())
        .union(propertyGetters.toArray(new GraphTraversal[propertyGetters.size()])).forEachRemaining(x -> {
          // side effects
        });
      return null;
    }).toList();



    // 3) traverse vertices again to load data into the sheet



    // TODO: extract method for sheet per type.
    // TODO: relate sheets through edges.

    return workbook;
  }


  /**
   * Exports an entire VRE as excel workbook
   * @param vreId the VRE to export
   * @return the export as workbook
   */
  public SXSSFWorkbook toExcel(String vreId) {
    SXSSFWorkbook workbook = new SXSSFWorkbook();
    // for all collections invoke export with depth=-1
    // ensure uniqueness via Set<UUID> loadedTimIds


    return workbook;
  }
}
