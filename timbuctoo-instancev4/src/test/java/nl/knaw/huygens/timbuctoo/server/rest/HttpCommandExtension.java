package nl.knaw.huygens.timbuctoo.server.rest;

import org.concordion.api.*;
import org.concordion.api.extension.ConcordionExtender;
import org.concordion.api.extension.ConcordionExtension;

import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

public class HttpCommandExtension implements ConcordionExtension {
  private HttpCommand httpCommand;
  private final HttpCaller caller;

  public HttpCommandExtension(HttpCaller caller) {
    this.caller = caller;
    httpCommand = new HttpCommand();
  }

  @Override
  public void addTo(ConcordionExtender concordionExtender) {
    concordionExtender.withCommand("http://huygens.knaw.nl/concordion-http-verifier", "http", httpCommand);
    concordionExtender.withCommand("http://huygens.knaw.nl/concordion-http-verifier", "request", new RequestCommand());
    concordionExtender.withCommand("http://huygens.knaw.nl/concordion-http-verifier", "response", new ResponseCommand());
  }

  private class RequestCommand extends AbstractCommand {

  }
  private class ResponseCommand extends AbstractCommand {

  }

  private class HttpCommand extends AbstractCommand {
    @Override
    public void execute(CommandCall commandCall, Evaluator evaluator, ResultRecorder resultRecorder) {
      HttpRequest request = parseRequest(commandCall.getChildren().get(0).getElement());
      HttpExpectation expectation = parseExpectation(commandCall.getChildren().get(1).getElement());
      Response callResult = caller.call(request);

      if (callResult.getStatus() == expectation.status) {
        resultRecorder.record(Result.SUCCESS);
      } else {
        resultRecorder.record(Result.FAILURE);
      }
    }

    private HttpExpectation parseExpectation(Element expectation) {
      String[] content = splitLines(expectation.getText());
      String statusCode = content[1].trim().split(" ")[1].trim();

      MultivaluedHashMap<String, Object> headers = new MultivaluedHashMap<>();
      int i = 2;
      while (!content[i].trim().equals("")) {
        String name = content[i].split(":")[0].trim();
        String value = content[i].split(":")[1].trim();
        headers.add(name, value);
        i++;
      }
      String body = null;
      if (i < content.length - 1) {
        body = "";
        for (int j = i; j < content.length - 1; j++) {
          body += content[j] + "\n";
        }
      }

      return new HttpExpectation(Integer.parseInt(statusCode), body, headers);
    }

    private String[] splitLines(String input) {
      return input.split("\n");
    }

    private HttpRequest parseRequest(Element request) {
      String[] content = splitLines(request.getText());
      String method = content[1].trim().split(" ")[0];
      String url = content[1].trim().split(" ")[1];

      MultivaluedHashMap<String, Object> headers = new MultivaluedHashMap<>();
      for (int i = 2; i< content.length - 1; i++) {
        String name = content[i].split(":")[0].trim();
        String value = content[i].split(":")[1].trim();
        headers.add(name, value);
      }

      return new HttpRequest(method, url, headers);
    }
  }
  public interface HttpCaller {
    Response call(HttpRequest value);
  }
  public class HttpRequest {
    final String method;
    final String url;
    final MultivaluedMap<String, Object> headers;

    public HttpRequest(String method, String url, MultivaluedMap<String, Object> headers) {
      this.method = method;
      this.url = url;
      this.headers = headers;
    }
  }
  public class HttpExpectation {
    final int status;
    final String body;
    final MultivaluedMap<String, Object> headers;

    public HttpExpectation(int status, String body, MultivaluedMap<String, Object> headers) {
      this.status = status;
      this.body = body;
      this.headers = headers;
    }
  }
}