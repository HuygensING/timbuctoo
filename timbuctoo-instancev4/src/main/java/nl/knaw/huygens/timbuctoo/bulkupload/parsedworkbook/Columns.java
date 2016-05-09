package nl.knaw.huygens.timbuctoo.bulkupload.parsedworkbook;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellReference;

import java.io.IOException;
import java.util.Optional;

public class Columns {
  static Optional<Columns> factory(int headerRow, int minRow, int maxRow, Sheet sheet, int column) {
    Row header = sheet.getRow(headerRow);
    final Cell headerCell = header.getCell(column);

    Optional<String> caption = null;
    try {
      caption = Helpers.getValueAsString(headerCell);
      if (!caption.isPresent()) {
        Helpers.addFailure(
          new CellReference(sheet.getSheetName(), headerRow, column, true, true),
          sheet.getWorkbook(),
          "The caption should not be blank."
        );
        return Optional.empty();
      }
      final String[] captionParts = caption.get().split(" ");
      if (captionParts.length == 1) {
        return Optional.of(new PropertyColumns(minRow, maxRow, headerRow, sheet, caption.get(), column));
      } else if (captionParts.length == 2) {
        return Optional.of(
          new RelationColumns(minRow, maxRow, headerRow, sheet, captionParts[0], captionParts[1], column)
        );
      } else {
        Helpers.addFailure(headerCell, "The caption should either contain no spaces (for properties) or a " +
          "relationName and a collectionName seperated by 1 space.");
        return Optional.empty();
      }
    } catch (IOException e) {
      Helpers.addFailure(headerCell, "The caption should not contain an error.");
      return Optional.empty();
    }
  }
}
