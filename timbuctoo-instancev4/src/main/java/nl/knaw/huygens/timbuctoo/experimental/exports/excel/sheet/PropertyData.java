package nl.knaw.huygens.timbuctoo.experimental.exports.excel.sheet;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import nl.knaw.huygens.timbuctoo.experimental.exports.excel.description.ExcelDescription;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.streaming.SXSSFRow;
import org.apache.poi.xssf.streaming.SXSSFSheet;

import java.util.List;
import java.util.Map;

class PropertyData {
  // Map containing excel info for properties
  private final Map<String, ExcelDescription> excelDescriptions = Maps.newHashMap();

  private final String timId;


  PropertyData(String timId) {
    this.timId = timId;
  }

  void putProperty(String propertyName, ExcelDescription excelDescription) {
    excelDescriptions.put(propertyName, excelDescription);
  }

  // Renders the data for this entity to the given excel sheet
  int renderToSheet(SXSSFSheet sheet, PropertyColumnMetadata propertyColumnMetadata, int currentRow) {

    // Determine the amount of rows needed for this entity
    int reservedRows = 0;
    for (Map.Entry<String, ExcelDescription> entry : excelDescriptions.entrySet()) {
      reservedRows = entry.getValue().getRows() > reservedRows ? entry.getValue().getRows() : reservedRows;
    }

    // Add reserved rows to the sheet for this entity
    List<SXSSFRow> sheetRows = Lists.newArrayList();
    for (int row = currentRow; row < currentRow + reservedRows; row++) {
      sheetRows.add(sheet.createRow(row));
    }

    // Fill the cells for this entity
    for (Map.Entry<String, ExcelDescription> entry : excelDescriptions.entrySet()) {

      // The left colOffset column for this property
      int offsetCol = propertyColumnMetadata.getColumnOffsetOf(entry.getKey());

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
      sheet.addMergedRegion(new CellRangeAddress(currentRow, currentRow + reservedRows - 1, 0, 0));
    }

    // Increment the current row by the number of reserved rows for this entity
    return reservedRows;
  }

  String getTimId() {
    return timId;
  }
}
