package nl.knaw.huygens.timbuctoo.experimental.exports;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import nl.knaw.huygens.timbuctoo.model.properties.LocalProperty;
import nl.knaw.huygens.timbuctoo.model.vre.Vres;
import nl.knaw.huygens.timbuctoo.server.GraphWrapper;
import nl.knaw.huygens.timbuctoo.util.Tuple;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.streaming.SXSSFCell;
import org.apache.poi.xssf.streaming.SXSSFRow;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ExcelExportService {

  private final Vres mappings;
  private final GraphWrapper graphWrapper;

  private static class PropertyColDescription {
    final int amountOfCols;
    final String propertyType;
    final int valueWidth;
    int offset;

    PropertyColDescription(int amountOfCols, String propertyType, int valueWidth) {
      this.amountOfCols = amountOfCols;
      this.propertyType = propertyType;
      this.valueWidth = valueWidth;
    }
  }

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
    SXSSFSheet sheet = workbook.createSheet(type);

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

          // Determine the property value which leads to the greatest amount cols for this property name
          ExcelDescription excelDescription = x.get().get();

          // The property-col description for the current cell
          final PropertyColDescription currentPropColDesc = new PropertyColDescription(excelDescription.getCols(),
            excelDescription.getType(), excelDescription.getValueWidth());

          // The latest proprety-col description for this property name
          final PropertyColDescription lastPropColDesc = propertyColDescriptions.containsKey(prop.getKey()) ?
            propertyColDescriptions.get(prop.getKey()) : null;

          // Add to / replace in the map of property-col descriptions is the amount of cols of current is greater
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

    // set column headers and determine column offset per property name
    SXSSFRow propertyNameRow = sheet.createRow(0);
    SXSSFRow propertyTypeRow = sheet.createRow(1);
    SXSSFRow propertyMetadataRow = sheet.createRow(2);

    propertyNameRow.createCell(0).setCellValue("tim_id");
    propertyTypeRow.createCell(0).setCellValue("uuid");

    int currentStartCol = 1;
    for (Map.Entry<String, PropertyColDescription> entry : propertyColDescriptions.entrySet()) {

      // Get the property-col-description value
      PropertyColDescription pcdValue = entry.getValue();

      // Set the propertyName cell
      propertyNameRow.createCell(currentStartCol).setCellValue(entry.getKey());

      // Set the propertyType cell
      propertyTypeRow.createCell(currentStartCol).setCellValue(pcdValue.propertyType);


      if (pcdValue.amountOfCols > 1) {

        // Merge headers that belong to the same property Name
        sheet.addMergedRegion(new CellRangeAddress(0, 0, currentStartCol, currentStartCol + pcdValue.amountOfCols - 1));
        sheet.addMergedRegion(new CellRangeAddress(1, 1, currentStartCol, currentStartCol + pcdValue.amountOfCols - 1));

        // Set the property metadata cells
        for (int col = currentStartCol, i = 1;
             col < currentStartCol + pcdValue.amountOfCols;
             col += pcdValue.valueWidth, i++) {

          propertyMetadataRow.createCell(col).setCellValue(i);
          // Merge cells that belong to the same value
          if (pcdValue.valueWidth > 1) {
            sheet.addMergedRegion(new CellRangeAddress(2, 2, col, col + pcdValue.valueWidth - 1));
          }
        }
      }

      // Determine the offset of the property col description
      pcdValue.offset = currentStartCol;

      // Increment current start col with the amount of cols
      currentStartCol += pcdValue.amountOfCols;
    }

    // 3) traverse vertices again to prepare data for the sheet

    long startTimePrepare = System.currentTimeMillis();
    final AtomicInteger currentRow = new AtomicInteger(3);
    entities.map(entityT -> {

      // Map the cells per property for this entity
      Map<String, ExcelDescription> excelDescriptions = Maps.newHashMap();
      List<GraphTraversal> propertyGetters = mapping
        .entrySet().stream()
        .map(prop -> prop.getValue().getExcelDescription().sideEffect(x -> {

          // Put the excel description into the map for this entity
          excelDescriptions.put(prop.getKey(), x.get().get());

        })).collect(Collectors.toList());


      graphWrapper.getGraph().traversal().V(entityT.get().id())
        .union(propertyGetters.toArray(new GraphTraversal[propertyGetters.size()])).forEachRemaining(x -> {
          // side effects
        });


      final String timId = entityT.get().value("tim_id");

      // Determine the amount of rows needed for this entity
      int reservedRows = 0;
      for (Map.Entry<String, ExcelDescription> entry : excelDescriptions.entrySet()) {
        reservedRows = entry.getValue().getRows() > reservedRows ? entry.getValue().getRows() : reservedRows;
      }

      // Add reserved rows to the sheet for this entity
      List<SXSSFRow> sheetRows = Lists.newArrayList();
      for (int row = currentRow.get(); row < currentRow.get() + reservedRows; row++) {
        sheetRows.add(sheet.createRow(row));
      }

      // Fill the cells for this entity
      for (Map.Entry<String, ExcelDescription> entry : excelDescriptions.entrySet()) {

        // The left offset column for this property
        int offsetCol = propertyColDescriptions.get(entry.getKey()).offset;

        // Loop through the cell data and fill the sheet with the cells
        ExcelDescription excelDescription = entry.getValue();
        for (int row = 0; row < excelDescription.getRows(); row++) {
          for (int col = 0; col < excelDescription.getCols(); col++) {
            sheetRows.get(row).createCell(col + offsetCol).setCellValue(excelDescription.getCells()[row][col]);
          }
        }
      }

      // Set the identity cell and in case of multiple rows, merge it.
      sheetRows.get(0).createCell(0).setCellValue(timId);
      if (reservedRows > 1) {
        sheet.addMergedRegion(new CellRangeAddress(currentRow.get(), currentRow.get() + reservedRows - 1, 0, 0));
      }

      // Increment the current row by the number of reserved rows for this entity
      currentRow.getAndAdd(reservedRows);

      return null;
    }).forEachRemaining(x -> {
      // side effects
    });
    System.out.println("LOAD DATA TIME: " + (System.currentTimeMillis() - startTimePrepare) / 1000);



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
