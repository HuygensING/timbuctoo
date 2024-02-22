package nl.knaw.huygens.timbuctoo.contractdiff.jsondiff;

import nl.knaw.huygens.timbuctoo.contractdiff.diffresults.DiffResult;
import nl.knaw.huygens.timbuctoo.contractdiff.JsonBuilder;

import java.util.HashMap;
import java.util.Map;

public class ObjectDiffResult extends DiffResult {
  private final Map<String, DiffResult> innerResults = new HashMap<>();
  private boolean wasSuccess = true;

  public void add(String key, DiffResult propResult) {
    innerResults.put(key, propResult);
    if (!propResult.wasSuccess()) {
      wasSuccess = false;
    }
  }

  @Override
  public String asHtml() {
    StringBuilder resultStr = new StringBuilder("{\n");
    for (Map.Entry<String, DiffResult> result: innerResults.entrySet()) {
      resultStr
          .append(result.getValue().asHtml(
              JsonBuilder.jsn(result.getKey()).toString() + ": ", "  ", ","))
          .append("\n");
    }
    return resultStr + "}\n";
  }

  @Override
  public String asHtml(String key, String indent, String delimiter) {
    StringBuilder resultStr = new StringBuilder(indent + key + "{\n");
    for (Map.Entry<String, DiffResult> result: innerResults.entrySet()) {
      resultStr
          .append(result.getValue().asHtml(
              JsonBuilder.jsn(result.getKey()).toString() + ": ", indent + "  ", ","))
          .append("\n");
    }
    return resultStr + indent + "}" + delimiter;
  }

  @Override
  public String asConsole() {
    StringBuilder resultStr = new StringBuilder(RESET + "{\n");
    for (Map.Entry<String, DiffResult> result: innerResults.entrySet()) {
      resultStr
          .append(result.getValue().asConsole(
              JsonBuilder.jsn(result.getKey()).toString() + ": ", "  ", ","))
          .append(RESET)
          .append("\n");
    }
    return resultStr + RESET + "}\n";
  }

  @Override
  public String asConsole(String key, String indent, String delimiter) {
    StringBuilder resultStr = new StringBuilder(indent + key + "{\n");
    for (Map.Entry<String, DiffResult> result: innerResults.entrySet()) {
      resultStr
          .append(indent)
          .append(result.getValue().asConsole(
              JsonBuilder.jsn(result.getKey()).toString() + ": ", indent + "  ", ","))
          .append(RESET)
          .append("\n");
    }
    return resultStr + indent + "}" + delimiter;
  }

  @Override
  public boolean wasSuccess() {
    return wasSuccess;
  }
}
