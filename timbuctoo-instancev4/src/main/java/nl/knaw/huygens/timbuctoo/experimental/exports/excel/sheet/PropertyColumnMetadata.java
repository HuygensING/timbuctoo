package nl.knaw.huygens.timbuctoo.experimental.exports.excel.sheet;

import com.google.common.collect.Maps;
import nl.knaw.huygens.timbuctoo.experimental.exports.excel.description.ExcelDescription;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.streaming.SXSSFRow;
import org.apache.poi.xssf.streaming.SXSSFSheet;

import java.util.List;
import java.util.Map;

class PropertyColumnMetadata {
  Map<String, PropertyColDescription> propertyColDescriptions = Maps.newHashMap();

  // Data holder class for for column information
  private static class PropertyColDescription {
    // The max amount of cols properties of this name take
    final int amountOfCols;
    // The property type
    final String propertyType;
    // The amount of columns each value in this property takes
    final int valueWidth;
    // The metadata on the property values
    final List<String> valueDescriptions;
    // The location of the columns for this property in the final excel sheet
    int colOffset = -1;

    PropertyColDescription(int amountOfCols, String propertyType, int valueWidth, List<String> valueDescriptions) {
      this.amountOfCols = amountOfCols;
      this.propertyType = propertyType;
      this.valueWidth = valueWidth;
      this.valueDescriptions = valueDescriptions;
    }
  }

  // Adds / updates a property column description to the propertyColDescriptions
  // - Adds if there was no column for this property yet
  // - Replaces if this property takes up more columns than the last property of this name
  void addColumnInformation(ExcelDescription excelDescription, String propertyName) {

    // The property-col description for the current cell
    final PropertyColDescription currentPropColDesc = new PropertyColDescription(excelDescription.getCols(),
      excelDescription.getType(), excelDescription.getValueWidth(), excelDescription.getValueDescriptions());

    // The latest proprety-col description for this property name
    final PropertyColDescription lastPropColDesc = propertyColDescriptions.containsKey(propertyName) ?
      propertyColDescriptions.get(propertyName) : null;

    // Add to / replace in the map of property-col descriptions is the amount of cols of current is greater
    if (lastPropColDesc == null || currentPropColDesc.amountOfCols > lastPropColDesc.amountOfCols) {
      propertyColDescriptions.put(propertyName, currentPropColDesc);
    }
  }


  // Renders the property metadata as header rows to the excel sheet
  void renderToSheet(SXSSFSheet sheet) {
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
        for (int col = currentStartCol, i = 0;
             col < currentStartCol + pcdValue.amountOfCols;
             col += pcdValue.valueWidth, i++) {

          propertyMetadataRow.createCell(col).setCellValue(pcdValue.valueDescriptions.get(i));
          // Merge cells that belong to the same value
          if (pcdValue.valueWidth > 1) {
            sheet.addMergedRegion(new CellRangeAddress(2, 2, col, col + pcdValue.valueWidth - 1));
          }
        }
      } else if (pcdValue.valueDescriptions.size() > 0) {
        // If there is only one column, but still a value description for it, add it to the header
        propertyMetadataRow.createCell(currentStartCol).setCellValue(pcdValue.valueDescriptions.get(0));
      }

      // Determine the column offset of the property col description
      pcdValue.colOffset = currentStartCol;

      // Increment current start col with the amount of cols
      currentStartCol += pcdValue.amountOfCols;
    }
  }


  int getColumnOffsetOf(String key) {
    if (propertyColDescriptions.get(key).colOffset < 0) {
      throw new RuntimeException("Offset of column can only be determined after renderToSheet is invoked.");
    }
    return propertyColDescriptions.get(key).colOffset;
  }
}
