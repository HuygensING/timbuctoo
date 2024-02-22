package nl.knaw.huygens.timbuctoo.contractdiff.httpdiff;

import com.google.common.collect.LinkedListMultimap;
import nl.knaw.huygens.timbuctoo.contractdiff.diffresults.DiffResult;

import java.util.Map;

public class HttpHeadersDiffResult extends DiffResult {
  private final LinkedListMultimap<String, DiffResult> innerResults;
  private boolean wasSuccess = true;

  public HttpHeadersDiffResult() {
    this.innerResults = LinkedListMultimap.create();
  }

  @Override
  public String asHtml() {
    StringBuilder resultStr = new StringBuilder();
    for (Map.Entry<String, DiffResult> result: innerResults.entries()) {
      resultStr.append(result.getValue().asHtml(result.getKey() + ": ", "", "")).append("\n");
    }
    return resultStr.toString();
  }

  @Override
  public String asHtml(String key, String indent, String delimiter) {
    StringBuilder resultStr = new StringBuilder(indent + key);
    for (Map.Entry<String, DiffResult> result: innerResults.entries()) {
      resultStr.append("\n").append(result.getValue().asHtml(result.getKey() + ": ", indent + "  ", ""));
    }
    return resultStr + delimiter;
  }

  @Override
  public String asConsole() {
    StringBuilder resultStr = new StringBuilder();
    for (Map.Entry<String, DiffResult> result: innerResults.entries()) {
      resultStr.append(RESET).append(result.getValue().asConsole(result.getKey() + ": ", "  ", "")).append("\n");
    }
    return resultStr + RESET;
  }

  @Override
  public String asConsole(String key, String indent, String delimiter) {
    StringBuilder resultStr = new StringBuilder(indent + key);
    for (Map.Entry<String, DiffResult> result: innerResults.entries()) {
      resultStr.append("\n" + RESET).append(result.getValue().asConsole(result.getKey() + ": ", indent + "  ", ""));
    }
    return resultStr + delimiter + RESET;
  }

  @Override
  public boolean wasSuccess() {
    return wasSuccess;
  }

  public void add(String key, DiffResult diffResult) {
    this.innerResults.put(key, diffResult);
    if (!diffResult.wasSuccess()) {
      wasSuccess = false;
    }
  }

  public void addMultiHeaderExpectation(String fieldName) {
    this.innerResults.put(fieldName, new ExpectationContainsMultipleKeys());
    wasSuccess = false;
  }

  private static class ExpectationContainsMultipleKeys extends DiffResult {
    @Override
    public String asHtml() {
      return null;
    }

    @Override
    public String asHtml(String key, String indent, String delimiter) {
      return "<span class=\"missing\">" +
        "<span class=\"expected\"> //Only one expectaction per fieldname supported</span>" +
        "</span>";
    }

    @Override
    public String asConsole() {
      return String.format(BAD +  "%s " + NORMAL + "//Only one expectaction per fieldname supported");
    }

    @Override
    public String asConsole(String key, String indent, String delimiter) {
      return String.format(NORMAL + indent + BAD + "%s" + //key
          NORMAL + "%s" + //delimiter
          "//Only one expectaction per fieldname supported", //expected
        key, delimiter);
    }

    @Override
    public boolean wasSuccess() {
      return false;
    }
  }
}
