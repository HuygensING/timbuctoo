package nl.knaw.huygens.timbuctoo.bulkupload.parsingstatemachine;

import java.util.HashMap;
import java.util.function.Consumer;

public class ResultReporter {
  private final Consumer<String> statusUpdate;
  private int curRow;

  public ResultReporter(Consumer<String> statusUpdate) {
    this.statusUpdate = statusUpdate;
    this.curRow = 1;
  }

  public void startCollection(String name, Result result) {
    statusUpdate.accept("sheet: " + name);
    result.handle(msg -> statusUpdate.accept("failure: " + msg));
  }

  public void registerPropertyName(int column, String value, Result result) {
    result.handle(msg -> log(column, msg));
  }

  public void startEntity() {
    statusUpdate.accept("" + curRow++);
  }

  public void finishEntity(HashMap<Integer, Result> extraResults) {
    extraResults.forEach((column, result) -> result.handle(msg -> log(column, msg)));
  }

  public void setValue(int column, String value, Result result) {

    result.handle(msg -> log(column, msg));
  }

  public void finishCollection() {
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
