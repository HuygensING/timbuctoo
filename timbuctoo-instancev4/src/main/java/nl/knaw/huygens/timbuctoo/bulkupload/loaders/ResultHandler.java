package nl.knaw.huygens.timbuctoo.bulkupload.loaders;

import nl.knaw.huygens.timbuctoo.bulkupload.parsingstatemachine.Result;

import java.util.HashMap;
import java.util.function.Consumer;

public class ResultHandler {
  private final Consumer<String> statusUpdate;
  private int curRow;

  public ResultHandler(Consumer<String> statusUpdate) {
    this.statusUpdate = statusUpdate;
    this.curRow = 1;
  }

  public void startValuePart() {
  }

  public void startSheet(String name, Result result) {
    statusUpdate.accept("sheet: " + name);
    result.handle(msg -> statusUpdate.accept("failure: " + msg));
  }

  public void startRow() {
    statusUpdate.accept("" + curRow++);
  }

  public void endRow(HashMap<Integer, Result> extraResults) {
    extraResults.forEach((column, result) -> result.handle(msg -> log(column, msg)));
  }

  public void endSheet() {
  }

  public void endImport() {
  }

  public void handle(int column, String value, Result result) {

    result.handle(msg -> log(column, msg));
  }

  private void log(int columnNumber, String message) {
    String columnName = getExcelColumnName(columnNumber);
    statusUpdate.accept("failure: at " + columnName + " " + message);
  }

  private String getExcelColumnName(int columnNumber) {
    int dividend = columnNumber;
    String columnName = "";
    int modulo;

    while (dividend > 0) {
      modulo = (dividend - 1) % 26;
      columnName = ((char) (65 + modulo)) + columnName;
      dividend = ((dividend - modulo) / 26);
    }

    return columnName;
  }

}
