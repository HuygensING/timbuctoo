package nl.knaw.huygens.timbuctoo.experimental.bulkupload.loaders.rangebasedxlsloader;

import nl.knaw.huygens.timbuctoo.experimental.bulkupload.loaders.CellHelper;
import nl.knaw.huygens.timbuctoo.experimental.bulkupload.loaders.BulkLoader;
import nl.knaw.huygens.timbuctoo.experimental.bulkupload.loaders.ValueType;
import nl.knaw.huygens.timbuctoo.experimental.bulkupload.parsingstatemachine.Importer;
import nl.knaw.huygens.timbuctoo.experimental.bulkupload.parsingstatemachine.Result;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;

import static nl.knaw.huygens.timbuctoo.experimental.bulkupload.loaders.CellHelper.getValueAsStringAndMarkError;

/*

this class:

 - iterates over the workbook and sends the value to the current responder
 - does workbook specific parsing (determining if what valuetype is in a cell)
 - when a responder returns a new responder it will link it to the correct columns
 - it writes the errors to the sheet
*/

public class RangebasedXlsLoader implements BulkLoader<Workbook> {

  private int failures = 0;
  private int successes = 0;
  private int ignoreds;

  public void loadWorkbookAndMarkErrors(Workbook wb, Importer importer) {
    for (int i = 0; i < wb.getNumberOfNames(); i++) {
      Range range = new Range(wb, wb.getNameAt(i));
      if (range.isValid()) {
        handleResult(range, importer.startCollection(range.getNameWithoutSuffix()));
        for (int r = range.getMinRow(); r <= range.getMaxRow(); r++) {
          Row row = range.getSheet().getRow(r);
          for (int c = range.getMinCol(); c <= range.getMaxCol(); c++) {
            String value = getValueAsStringAndMarkError(row.getCell(c), "An error cannot be imported").orElse("");
            switch (getValueTypeFor(value, r, range.getMinRow())) {
              case PROPERTYNAME:
                handleResult(row, c, importer.registerPropertyName(c, value));
                break;
              case IDENTITY_PROPERTYNAME:
                handleResult(row, c, importer.registerPropertyName(c, value.substring(0, value.length() - 1)).and(
                  importer.registerUnique(c, true)
                ));
                break;
              case RELATIONSPECIFICATION:
                handleResult(row, c, importer.registerTargetCollection(c, value.split(" ")[1]).and(
                  importer.registerRelationName(c, value.split(" ")[0])
                ));
                break;
              case VALUE:
                if (c == range.getMinCol()) {
                  importer.startEntity();
                }
                importer.setValue(c, value);
                if (c == range.getMaxCol()) {
                  importer.finishEntity()
                    .forEach((idx, res) -> handleResult(row, idx, res));
                }
                break;
              case NONE:
                handleResult(row, c, Result.ignored());
                break;
              default:
                throw new RuntimeException("Not all enum cases have been handled");
            }
          }
        }
        handleResult(range, importer.finishCollection());
      }
    }
    handleResult(wb, importer.finishImport());
  }


  private void handleResult(Workbook wb, Result result) {
    result.handle(
      () -> successes++,
      failure -> {
        final Cell someCell = wb.getSheetAt(0).getRow(0).getCell(0, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
        CellHelper.addFailure(someCell, failure);
        failures++;
      },
      () -> ignoreds++
    );
  }


  private void handleResult(Range range, Result result) {
    result.handle(
      () -> successes++,
      failure -> {
        failures++;
        CellHelper.addFailure(range.getCellForError(), failure);
      },
      () -> ignoreds++
    );
  }

  private void handleResult(Row row, int cell, Result result) {
    result.handle(
      () -> {
        successes++;
        Cell headerCellNotNull = row.getCell(cell, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
        CellHelper.addSuccess(headerCellNotNull);
      },
      error -> {
        failures++;
        Cell headerCellNotNull = row.getCell(cell, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
        CellHelper.addFailure(headerCellNotNull, error);
      },
      () -> ignoreds++);
  }

  private ValueType getValueTypeFor(String contents, int cellRow, int headerRow) {
    if (contents.equals("")) {
      return ValueType.NONE;
    } else {
      if (cellRow == headerRow) {
        if (contents.endsWith("*")) {
          return ValueType.IDENTITY_PROPERTYNAME;
        } else if (contents.contains(" ")) {
          return ValueType.RELATIONSPECIFICATION;
        } else {
          return ValueType.PROPERTYNAME;
        }
      } else {
        return ValueType.VALUE;
      }
    }
  }

  public int getSuccesses() {
    return successes;
  }

  public int getFailures() {
    return failures;
  }

  public int getIgnoreds() {
    return ignoreds;
  }
}
