package nl.knaw.huygens.timbuctoo.experimental.bulkupload.loaders.rangebasedxlsloader;

import nl.knaw.huygens.timbuctoo.experimental.bulkupload.loaders.CellHelper;
import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Name;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.AreaReference;
import org.apache.poi.ss.util.CellReference;

class Range {
  private boolean isValid;
  private int minRow;
  private int minCol;
  private int maxCol;
  private Sheet sheet;
  private final String nameWithoutSuffix;
  private final Cell cellForError;
  private int maxRow;

  public Range(Workbook wb, Name range) {

    final String rangeName = range.getNameName();
    nameWithoutSuffix = rangeName.substring(0, rangeName.length() - "_data".length());

    final CellReference aCellInRange = AreaReference.generateContiguous(range.getRefersToFormula())[0].getFirstCell();
    if (rangeName.endsWith("_data")) {
      AreaReference aref;
      try {
        aref = new AreaReference(range.getRefersToFormula(), SpreadsheetVersion.EXCEL2007);
      } catch (IllegalArgumentException e) {
        //non-contiguous range
        Cell firstCell = wb
          .getSheet(aCellInRange.getSheetName())
          .getRow(aCellInRange.getRow())
          .getCell(aCellInRange.getCol(), Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);

        CellHelper.addFailure(
          firstCell,
          "Range is non-contiguous (i.e. there are multiple seperate ranges) this is not supported."
        );
        isValid = false;
        this.cellForError = firstCell;
        return;
      }
      CellReference firstCell = aref.getFirstCell();
      CellReference lastCell = aref.getLastCell();
      minRow = Math.min(firstCell.getRow(), lastCell.getRow());
      maxRow = Math.max(firstCell.getRow(), lastCell.getRow());
      minCol = Math.min(firstCell.getCol(), lastCell.getCol());
      maxCol = Math.max(firstCell.getCol(), lastCell.getCol());

      sheet = wb.getSheet(firstCell.getSheetName());

      if (minRow == -1) {
        minRow = 0;
        maxRow = sheet.getLastRowNum();
      }
      if (minCol == -1) {
        minCol = 0;
        for (int j = minRow; j <= maxRow; j++) {
          maxCol = Math.max(maxCol, sheet.getRow(j).getLastCellNum());
        }
      }
      this.cellForError = sheet
        .getRow(minRow)
        .getCell(minCol, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
      isValid = true;
    } else {
      this.cellForError = wb
        .getSheet(aCellInRange.getSheetName())
        .getRow(aCellInRange.getRow())
        .getCell(aCellInRange.getCol(), Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
      isValid = false;
    }
  }

  boolean isValid() {
    return isValid;
  }

  public int getMinRow() {
    return minRow;
  }

  public int getMinCol() {
    return minCol;
  }

  public int getMaxCol() {
    return maxCol;
  }

  public int getMaxRow() {
    return maxRow;
  }

  public Sheet getSheet() {
    return sheet;
  }

  public String getNameWithoutSuffix() {
    return nameWithoutSuffix;
  }

  public Cell getCellForError() {
    return cellForError;
  }
}
