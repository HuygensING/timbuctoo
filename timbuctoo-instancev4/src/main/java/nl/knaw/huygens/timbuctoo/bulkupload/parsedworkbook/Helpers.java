package nl.knaw.huygens.timbuctoo.bulkupload.parsedworkbook;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.Comment;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.RichTextString;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellReference;

import java.io.IOException;
import java.util.Optional;

public class Helpers {

  public static void addFailure(CellReference cellReference, Workbook wb, String text) {
    final Sheet sheet = wb.getSheet(cellReference.getSheetName());
    Cell cell = sheet.getRow(cellReference.getRow()).getCell(cellReference.getCol());
    if (cell == null) {
      cell = sheet.getRow(cellReference.getRow()).createCell(cellReference.getCol());
    }
    addFailure(cell, text);
  }

  public static void addFailure(Cell cell, String text) {
    Workbook wb = cell.getSheet().getWorkbook();

    Comment comment = cell.getCellComment();
    CreationHelper factory = wb.getCreationHelper();
    if (comment == null) {
      Sheet sheet = cell.getSheet();
      Row row = cell.getRow();

      // When the comment box is visible, have it show in a 10x2 space
      ClientAnchor anchor = factory.createClientAnchor();
      anchor.setCol1(cell.getColumnIndex());
      anchor.setCol2(cell.getColumnIndex() + 10);
      anchor.setRow1(row.getRowNum());
      anchor.setRow2(row.getRowNum() + 2);

      // Create the comment and set the text+author
      Drawing drawing = sheet.createDrawingPatriarch();
      comment = drawing.createCellComment(anchor);
    }
    RichTextString str = comment.getString();
    if (str == null) {
      str = factory.createRichTextString(text);
    } else {
      str = factory.createRichTextString(str.getString() + "\n\n" + text);
    }

    comment.setString(str);
    comment.setAuthor("Timbuctoo importer");

    // Assign the comment to the cell
    cell.setCellComment(comment);

    final CellStyle cellStyle = wb.createCellStyle();
    cellStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
    cellStyle.setFillForegroundColor(IndexedColors.RED.getIndex());

    cell.setCellStyle(cellStyle);
  }

  public static void addSuccess(Cell cell) {
    Workbook wb = cell.getSheet().getWorkbook();
    final CellStyle cellStyle = wb.createCellStyle();
    cellStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
    cellStyle.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());

    cell.setCellStyle(cellStyle);
  }

  public static Optional<String> getValueAsString(Cell cell) throws IOException {
    if (cell == null) {
      return Optional.empty(); //Apparently getRow().getCell() (might) return null if the cell is blank
    }
    final int cellType = cell.getCellType();
    if (cellType == Cell.CELL_TYPE_FORMULA) {
      return getValueAsString(cell, cell.getCachedFormulaResultType());
    } else {
      return getValueAsString(cell, cellType);
    }
  }

  private static Optional<String> getValueAsString(Cell cell, int cellType) throws IOException {
    switch (cellType) {
      case Cell.CELL_TYPE_BLANK:
        return Optional.empty();
      case Cell.CELL_TYPE_ERROR:
        throw new IOException(cell.getErrorCellValue() + "");
      case Cell.CELL_TYPE_BOOLEAN:
        return Optional.of(cell.getBooleanCellValue() + "");
      case Cell.CELL_TYPE_NUMERIC:
        return Optional.of(cell.getNumericCellValue() + "");
      case Cell.CELL_TYPE_STRING:
        return Optional.of(cell.getStringCellValue());
      default:
        throw new RuntimeException("Unknown celltype: " + cellType);
    }
  }


  public static boolean hasData(Cell data) {
    if (data == null) {
      return false;
    } else if (data.getCellType() == Cell.CELL_TYPE_BLANK) {
      return false;
    } else if (data.getCellType() == Cell.CELL_TYPE_ERROR) {
      return false;
    } else if (data.getCellType() == Cell.CELL_TYPE_FORMULA) {
      if (data.getCachedFormulaResultType() == Cell.CELL_TYPE_BLANK) {
        return false;
      } else if (data.getCachedFormulaResultType() == Cell.CELL_TYPE_ERROR) {
        return false;
      }
    }
    return true;
  }
}
