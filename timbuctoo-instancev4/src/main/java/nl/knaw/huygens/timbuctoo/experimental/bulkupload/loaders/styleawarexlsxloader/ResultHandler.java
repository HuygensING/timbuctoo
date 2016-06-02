package nl.knaw.huygens.timbuctoo.experimental.bulkupload.loaders.styleawarexlsxloader;

import nl.knaw.huygens.timbuctoo.experimental.bulkupload.parsingstatemachine.Result;

import java.util.ArrayList;
import java.util.HashMap;

public class ResultHandler {
  private StringBuilder results = new StringBuilder();
  int curColumn = 0;
  ArrayList<String> data;
  int curFailures;
  ArrayList<Integer> failures = new ArrayList<>();
  ArrayList<String> sheets = new ArrayList<>();

  private boolean valuePart;

  public ResultHandler() {
    results = new StringBuilder("<!DOCTYPE html>\n" +
                                  "<html>\n" +
                                  "<head>\n" +
                                  "  <meta charset=\"utf-8\">\n" +
                                  "  <meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" />\n" +
                                  "  <meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\">\n" +
                                  "  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">\n" +
                                  "  <title>Huygens ING Data Repository</title>\n" +
                                  "  <link href=\"https://maxcdn.bootstrapcdn.com/bootstrap/3.3.6/css/bootstrap.m" +
                                  "in.css\" rel=" +
                                  "\"stylesheet\" integrity=\"sha384-1q8mTJOASx8j1Au+a5WDVnPi2lkFfwwEAa8hDDdjZlpLeg" +
                                  "xhjVME" +
                                  "1fgjWPGmkzs7\" crossorigin=\"anonymous\">\n" +
                                  "  <script src=\"https://ajax.googleapis.com/ajax/libs/jquery/1.11.3/jquery.mi" +
                                  "n.js\"></script>\n" +
                                  "  <script src=\"https://maxcdn.bootstrapcdn.com/bootstrap/3.3.6/js/bootstrap.min" +
                                  ".js\" integrity=\"sha384-0mSbJDEHialfmuBBQP6A4Qrprq5OVfW37PRR3j5ELqxss1yVqO" +
                                  "tnepnHVP9aJ7xS\" crossorigin=\"anonymous\"></script>\n" +
                                  "</head>\n" +
                                  "\n" +
                                  "<body>\n" +
                                  "<div class=\"container\">\n");
  }

  public void startValuePart() {
    valuePart = true;
  }

  public void startSheet(String name, Result result) {
    curFailures = 0;
    sheets.add(name);
    results.append("  <div class=\"row\" id='").append(name).append("'>\n    <h3>")
      .append(name).append("</h3>\n");
    result.handle(
      msg -> {
        curFailures++;
        results.append("<div class='alert alert-danger'>").append(msg).append("</div>");
      }
    );
    results.append("<table  class=\"table table-condensed table-hover\">");
    valuePart = false;
  }

  public void startRow() {
    curColumn = 0;
    results.append("<tr>");
    data = new ArrayList<>(50);
  }

  public void endRow(HashMap<Integer, Result> extraResults) {
    extraResults.forEach((column, result) -> {
      int valueIdx = column * 2;
      int resultIdx = valueIdx + 1;
      while (data.size() <= resultIdx) {
        data.add(null);
      }

      result.handle(
        () -> {
          if (data.get(resultIdx) == null) {
            data.set(resultIdx, "");
          }
        },
        msg -> {
          curFailures++;
          if (data.get(resultIdx) == null) {
            data.set(resultIdx, msg);
          } else {
            data.set(resultIdx, data.get(resultIdx) + " " + msg);
          }
        },
        () -> { }
      );
    });
    for (int i = 0, dataSize = data.size(); i < dataSize; i += 2) {
      String value = data.get(i);
      String result = data.get(i + 1);
      String className;
      if (result == null) {
        className = "ignored";
        result = "";
      } else if (result.length() > 0) {
        className = "danger";
      } else {
        className = "success";
      }
      if (value == null) {
        value = "";
      }
      String cell;
      if (valuePart) {
        cell = "td";
      } else {
        cell = "th";
      }
      results.append("<").append(cell).append(" data-toggle='tooltip' title='").append(result).append("'")
        .append(" class='").append(className).append("'>")
        .append(value)
        .append("</").append(cell).append(">");
    }

    results.append("</tr>");
  }

  public void endSheet(Result result) {
    results.append("</table>");
    result.handle(
      msg -> {
        curFailures++;
        results.append("<div class='alert alert-danger'>").append(msg).append("</div>");
      }
    );
    results.append("</div>");
    failures.add(curFailures);
  }

  public String endImport(Result result) {
    result.handle(
      msg -> {
        curFailures++;
        results.append("<div class='alert alert-danger'>").append(msg).append("</div>");
      }
    );
    results.append("</div>\n<div class=\"panel panel-default\" style='position:fixed; top: 20px'>" +
                     "<div class='panel-body' id='navbar'><ul class=\"nav nav-pills nav-stacked\">\n");
    for (int i = 0; i < sheets.size(); i++) {
      String sheet = sheets.get(i);
      int failures = this.failures.get(i);
      results.append("<li><a href='#").append(sheet).append("'>").append(sheet).append(" ")
        .append("<span class=\"badge\">").append(failures).append("</span></a></li>");
    }

    results.append("</ul></div></div>");
    results.append("<script>$(function () {\n" +
                     "  $('[data-toggle=\"tooltip\"]').tooltip({container: 'body'})\n" +
                     "}); $('body').scrollspy({ target: '#navbar' })</script></body></html>");
    return results.toString();
  }

  public void handle(int column, String value, Result result) {
    int valueIdx = column * 2;
    int resultIdx = valueIdx + 1;
    while (data.size() <= resultIdx) {
      data.add(null);
    }
    result.handle(
      () -> {
        data.set(valueIdx, value);
        data.set(resultIdx, "");
      },
      msg -> {
        curFailures++;
        data.set(valueIdx, value);
        data.set(resultIdx, msg);
      },
      () -> data.set(valueIdx, value)
    );
  }

}
