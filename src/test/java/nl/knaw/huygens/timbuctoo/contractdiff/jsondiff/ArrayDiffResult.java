package nl.knaw.huygens.timbuctoo.contractdiff.jsondiff;

import nl.knaw.huygens.timbuctoo.contractdiff.diffresults.DiffResult;

import java.util.ArrayList;
import java.util.List;

public class ArrayDiffResult extends DiffResult {
  private final List<DiffResult> innerResults;
  private boolean wasSuccess = true;

  public ArrayDiffResult() {
    this.innerResults = new ArrayList<>();
  }

  @Override
  public String asHtml() {
    StringBuilder resultStr = new StringBuilder("[\n");
    for (DiffResult result: innerResults) {
      resultStr.append(result.asHtml("", "  ", ",")).append("\n");
    }
    return resultStr + "]\n";
  }

  @Override
  public String asHtml(String key, String indent, String delimiter) {
    StringBuilder resultStr = new StringBuilder(indent + key + "[\n");
    for (DiffResult result: innerResults) {
      resultStr.append(result.asHtml("", indent + "  ", ",")).append("\n");
    }
    return resultStr + indent + "]" + delimiter;
  }

  @Override
  public String asConsole() {
    StringBuilder resultStr = new StringBuilder(RESET + "[\n");
    for (DiffResult result: innerResults) {
      resultStr.append(RESET).append(result.asConsole("", "  ", ",")).append("\n");
    }
    return resultStr + RESET + "]\n";
  }

  @Override
  public String asConsole(String key, String indent, String delimiter) {
    StringBuilder resultStr = new StringBuilder(indent + key + "[\n");
    for (DiffResult result: innerResults) {
      resultStr.append(RESET).append(indent).append(result.asConsole("", indent + "  ", ",")).append("\n");
    }
    return resultStr + indent + "]" + delimiter;
  }

  @Override
  public boolean wasSuccess() {
    return wasSuccess;
  }

  public void add(int index, DiffResult diffResult) {
    this.innerResults.add(index, diffResult);
    if (!diffResult.wasSuccess()) {
      wasSuccess = false;
    }
  }
}
