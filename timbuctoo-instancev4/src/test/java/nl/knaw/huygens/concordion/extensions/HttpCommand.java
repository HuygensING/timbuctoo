package nl.knaw.huygens.concordion.extensions;

import com.google.common.collect.Lists;
import org.apache.http.Header;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.impl.io.DefaultHttpRequestParser;
import org.apache.http.impl.io.DefaultHttpResponseParser;
import org.apache.http.impl.io.HttpTransportMetricsImpl;
import org.apache.http.impl.io.SessionInputBufferImpl;
import org.concordion.api.*;
import org.concordion.api.listener.AssertEqualsListener;
import org.concordion.api.listener.AssertFailureEvent;
import org.concordion.api.listener.AssertSuccessEvent;
import org.concordion.internal.util.Announcer;

import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.Response;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.AbstractMap;
import java.util.List;

class HttpCommand extends AbstractCommand {
  private final Announcer<AssertEqualsListener> listeners = Announcer.to(AssertEqualsListener.class);
  private final HttpCaller caller;
  private HttpExpectation expectation;
  private HttpRequest httpRequest;
  private Element expectedStatusElement;
  private List<Element> expectedHeaderElements;
  private Element expectedBodyElement;

  public HttpCommand(HttpCaller caller) {
    this.caller = caller;
  }

  @Override
  public void setUp(CommandCall commandCall, Evaluator evaluator, ResultRecorder resultRecorder) {
    String formattedRequest = formatValue(commandCall.getChildren().get(0).getElement().getText());
    httpRequest = parseRequest(formattedRequest);
    Element expectationElement = commandCall.getChildren().get(1).getElement();
    expectation = parseExpectedResponse(formatValue(expectationElement.getText()));

    formatResponseExpectation(expectationElement);
  }

  @Override
  public void execute(CommandCall commandCall, Evaluator evaluator, ResultRecorder resultRecorder) {
    Response callResult = caller.call(httpRequest);

    if (callResult.getStatus() == expectation.status) {
      success(resultRecorder, expectedStatusElement);
    } else {
      failure(resultRecorder, expectedStatusElement, "" + expectation.status, "" + callResult.getStatus());
    }


    for (int i = 0; i < expectation.headers.size(); i++) {
      AbstractMap.SimpleEntry<String, String> header = expectation.headers.get(i);
      if (!callResult.getHeaders().containsKey(header.getKey())) {
        failure(resultRecorder, expectedHeaderElements.get(i), header.getKey() + ": " + header.getValue(), "");
      } else {
        Object actual = callResult.getHeaders().getFirst(header.getKey());
        if (header.getValue() != null && !actual.equals(header.getValue())) {
          failure(resultRecorder, expectedHeaderElements.get(i).getFirstChildElement("span"), header.getValue(), actual.toString());
        } else {
          success(resultRecorder, expectedHeaderElements.get(i));
        }
      }
    }
    String resultBody = callResult.readEntity(String.class);
    evaluator.setVariable("#body", resultBody);

    if (expectation.body != null) {
      if (expectation.body.equals(resultBody)) {
        success(resultRecorder, expectedBodyElement);
      } else {
        failure(resultRecorder, expectedBodyElement, expectation.body, resultBody);
      }
    }

  }

  public void addListener(AssertEqualsListener listener) {
    this.listeners.addListener(listener);
  }

  private void failure(ResultRecorder resultRecorder, Element element, String expected, String actual) {
    resultRecorder.record(Result.FAILURE);
    listeners.announce().failureReported(new AssertFailureEvent(element, expected, actual));
  }

  private void success(ResultRecorder resultRecorder, Element element) {
    resultRecorder.record(Result.SUCCESS);
    listeners.announce().successReported(new AssertSuccessEvent(element));
  }

  private void formatResponseExpectation(Element expectationElement) {
    Element parentElement = expectationElement.getParentElement();
    parentElement.removeChild(expectationElement);

    Element response = new Element("span").addAttribute("class", "response");

    expectedStatusElement = new Element("span").appendText("" + expectation.status).addAttribute("class", "respStatus");
    response.appendChild(expectedStatusElement);
    response.appendText("\n");
    expectedHeaderElements = Lists.newArrayList();
    for (AbstractMap.SimpleEntry<String, String> header : expectation.headers) {
      Element headerEl = new Element("span").addAttribute("class", "respHeader");
      expectedHeaderElements.add(headerEl);
      headerEl.appendText(header.getKey() + ": ");
      headerEl.appendChild(new Element("span").appendText(header.getValue()).addAttribute("class", "respHeaderValue"));
      response.appendChild(headerEl);
      response.appendText("\n");
    }
    response.appendText("\n");
    if (expectation.hasBody()) {
      expectedBodyElement = new Element("span").appendText(expectation.body).addAttribute("class", "respBody");
    }
    response.appendChild(expectedBodyElement);
    parentElement.appendChild(response);
  }

  private String formatValue(String text) {
    StringBuilder prefix = new StringBuilder();
    for (char ch : text.toCharArray()) {
      if (ch == '\n' || ch == '\r') {
        prefix.delete(0, prefix.length());
        continue;
      }
      if (ch != ' ' && ch != '\t') {
        break;
      }

      prefix.append(ch);
    }

    return text.replace("\n" + prefix, "\n").trim();
  }

  private HttpExpectation parseExpectedResponse(String value) {
    SessionInputBufferImpl buffer = new SessionInputBufferImpl(new HttpTransportMetricsImpl(), value.length());
    buffer.bind(new ByteArrayInputStream(value.getBytes(StandardCharsets.UTF_8)));
    DefaultHttpResponseParser defaultHttpResponseParser = new DefaultHttpResponseParser(buffer);

    HttpResponse httpResponse = null;
    try {
      httpResponse = defaultHttpResponseParser.parse();
    } catch (IOException e) {
      e.printStackTrace();
    } catch (HttpException e) {
      e.printStackTrace();
    }

    StatusLine statusLine = httpResponse.getStatusLine();

    int statusCode = statusLine.getStatusCode();
    List<AbstractMap.SimpleEntry<String, String>> headers = Lists.newArrayList();


    for (Header header : httpResponse.getAllHeaders()) {
      headers.add(new AbstractMap.SimpleEntry<>(header.getName(), header.getValue()));
    }


    String body = null;
    try {
      if (buffer.hasBufferedData()) {
        body = "";

        while (buffer.hasBufferedData()) {
          body += (char) buffer.read();
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    }


    return new HttpExpectation(statusCode, body, headers);
  }

  private HttpRequest parseRequest(String value) {
    SessionInputBufferImpl buffer = new SessionInputBufferImpl(new HttpTransportMetricsImpl(), value.length());
    buffer.bind(new ByteArrayInputStream(value.getBytes(StandardCharsets.UTF_8)));
    DefaultHttpRequestParser defaultHttpRequestParser = new DefaultHttpRequestParser(buffer);

    org.apache.http.HttpRequest httpRequest = null;
    try {
      httpRequest = defaultHttpRequestParser.parse();
    } catch (IOException e) {
      e.printStackTrace();
    } catch (HttpException e) {
      e.printStackTrace();
    }

    String method = httpRequest.getRequestLine().getMethod();
    String url = httpRequest.getRequestLine().getUri();

    MultivaluedHashMap<String, Object> headers = new MultivaluedHashMap<>();
    for (Header header : httpRequest.getAllHeaders()) {
      headers.add(header.getName(), header.getValue());
    }
    return new HttpRequest(method, url, headers);
  }
}
