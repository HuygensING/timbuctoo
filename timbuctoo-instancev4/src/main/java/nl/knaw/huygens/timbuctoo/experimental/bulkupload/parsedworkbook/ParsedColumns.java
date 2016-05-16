package nl.knaw.huygens.timbuctoo.experimental.bulkupload.parsedworkbook;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

import java.io.IOException;
import java.util.Iterator;
import java.util.Optional;

public class ParsedColumns {
  static Optional<ParsedColumns> factory(Row headerRow, Iterator<Row> rows, int column) {
    final Cell captionCell = headerRow.getCell(column, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);

    try {
      Optional<String> captionOpt = Helpers.getValueAsString(captionCell);
      if (!captionOpt.isPresent()) {
        Helpers.addFailure(
          captionCell,
          "The caption should not be blank."
        );
        return Optional.empty();
      } else {
        final String caption = captionOpt.get();
        final String[] captionParts = caption.split(" ");
        if (captionParts.length == 1) {
          return Optional.of(
            new PropertyColumns(rows, captionCell, column)
          );
        } else if (captionParts.length == 2) {
          //FIXME add assertion that start and endcolumn are the same
          return Optional.of(
            new RelationColumns(rows, headerRow, captionParts[0], captionParts[1], column)
          );
        } else {
          Helpers.addFailure(captionCell, "The caption should either contain no spaces (for properties) or a " +
            "relationName and a collectionName seperated by 1 space.");
          return Optional.empty();
        }
      }
    } catch (IOException e) {
      Helpers.addFailure(captionCell, "The caption should not contain an error.");
      return Optional.empty();
    }
  }
}
