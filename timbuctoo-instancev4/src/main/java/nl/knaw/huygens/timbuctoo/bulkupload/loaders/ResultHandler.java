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
    extraResults.forEach((column, result) -> result.handle(msg -> statusUpdate.accept("failure: " + msg)));
  }

  public void endSheet() {
  }

  public void endImport() {
  }

  public void handle(int column, String value, Result result) {
    result.handle(msg -> statusUpdate.accept("failure: " + msg));
  }

}
