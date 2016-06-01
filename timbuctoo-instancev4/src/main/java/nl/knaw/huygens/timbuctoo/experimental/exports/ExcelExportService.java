package nl.knaw.huygens.timbuctoo.experimental.exports;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import nl.knaw.huygens.timbuctoo.model.properties.LocalProperty;
import nl.knaw.huygens.timbuctoo.model.vre.Vres;
import nl.knaw.huygens.timbuctoo.server.GraphWrapper;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ExcelExportService {

  private final Vres mappings;
  private final GraphWrapper graphWrapper;

  private static class PropertyColDescription {
    final int amountOfCols;
    final String propertyType;

    PropertyColDescription(int amountOfCols, String propertyType) {
      this.amountOfCols = amountOfCols;
      this.propertyType = propertyType;
    }

    private static int getAmountOfCols(PropertyColDescription propertyColDescription) {
      return propertyColDescription.amountOfCols;
    }

    @Override
    public String toString() {
      return "PropertyColDescription{" +
        "amountOfCols=" + amountOfCols +
        ", propertyType='" + propertyType + '\'' +
        '}';
    }
  }

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

    Map<String, PropertyColDescription> propertyColDescriptions = Maps.newHashMap();

    entities.asAdmin().clone().map(entityT -> {
      List<GraphTraversal> propertyGetters = mapping
        .entrySet().stream()
        .map(prop -> prop.getValue().getExcelDescription().sideEffect(x -> {
          ExcelDescription excelDescription = x.get().get();
          final PropertyColDescription currentPropColDesc = new PropertyColDescription(excelDescription.getCols(),
            excelDescription.getType());
          final PropertyColDescription lastPropColDesc = propertyColDescriptions.containsKey(prop.getKey()) ?
            propertyColDescriptions.get(prop.getKey()) : null;

          if (lastPropColDesc == null || currentPropColDesc.amountOfCols > lastPropColDesc.amountOfCols) {
            propertyColDescriptions.put(prop.getKey(), currentPropColDesc);
          }

        })).collect(Collectors.toList());

      graphWrapper.getGraph().traversal().V(entityT.get().id())
        .union(propertyGetters.toArray(new GraphTraversal[propertyGetters.size()])).forEachRemaining(x -> {
          // side effects
        });
      return null;
    }).forEachRemaining(x -> {
      // more side effects
    });

    System.out.println(propertyColDescriptions);

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
