package nl.knaw.huygens.concordion.extensions;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.impl.io.DefaultHttpRequestParser;
import org.apache.http.impl.io.DefaultHttpResponseParser;
import org.apache.http.impl.io.HttpTransportMetricsImpl;
import org.apache.http.impl.io.SessionInputBufferImpl;
import org.concordion.api.AbstractCommand;
import org.concordion.api.CommandCall;
import org.concordion.api.Element;
import org.concordion.api.Evaluator;
import org.concordion.api.Result;
import org.concordion.api.ResultRecorder;
import org.concordion.api.listener.AssertEqualsListener;
import org.concordion.api.listener.AssertFailureEvent;
import org.concordion.api.listener.AssertSuccessEvent;
import org.concordion.internal.util.Announcer;

import javax.ws.rs.core.Response;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.AbstractMap;
import java.util.List;

public class HttpCommand extends AbstractCommand {
  private final Announcer<AssertEqualsListener> listeners = Announcer.to(AssertEqualsListener.class);
  private final HttpCaller caller;
  private final String commandName;
  private final String namespace;
  private HttpExpectation expectation;
  private HttpRequest httpRequest;
  private Element expectedStatusElement;
  private List<Element> expectedHeaderElements;
  private Element expectedBodyElement;
  private String variableName;

  public HttpCommand(HttpCaller caller, String commandName, String namespace) {
    this.caller = caller;
    this.commandName = commandName;
    this.namespace = namespace;
  }

  @Override
  public void setUp(CommandCall commandCall, Evaluator evaluator, ResultRecorder resultRecorder) {
    stripCommandAttribute(commandCall.getElement());
    variableName = commandCall.getExpression();
    if (!variableName.isEmpty() && !variableName.startsWith("#")) {
      throw new RuntimeException(variableName + " should start with a #, to be a valid Concordion variable.");
    }

    Element requestElement = commandCall.getChildren().get(0).getElement();
    httpRequest = parseRequest(requestElement);
    formatRequestExpectation(requestElement);

    Element expectationElement = commandCall.getChildren().get(1).getElement();
    expectation = parseExpectedResponse(expectationElement);
    formatResponseExpectation(expectationElement);
  }

  private void stripCommandAttribute(Element element) {
    element.removeAttribute(commandName, namespace);
  }

  @Override
  public void execute(CommandCall commandCall, Evaluator evaluator, ResultRecorder resultRecorder) {
    Response callResult = caller.call(httpRequest);

    HttpResult httpResult = new HttpResult(callResult);

    if (httpResult.getStatus() == expectation.status) {
      success(resultRecorder, expectedStatusElement);
    } else {
      failure(resultRecorder, expectedStatusElement, "" + expectation.status, "" + httpResult.getStatus());
    }


    for (int i = 0; i < expectation.headers.size(); i++) {
      AbstractMap.SimpleEntry<String, String> header = expectation.headers.get(i);
      if (!httpResult.getHeaders().containsKey(header.getKey().toLowerCase())) {
        failure(resultRecorder, expectedHeaderElements.get(i), header.getKey() + ": " + header.getValue(), "");
      } else {
        Object actual = httpResult.getHeaders().get(header.getKey().toLowerCase());
        if (StringUtils.isBlank(header.getValue())) {
          success(resultRecorder, expectedHeaderElements.get(i));
          expectedHeaderElements.get(i).appendText("" + actual);
        } else if (actual.equals(header.getValue())) {
          success(resultRecorder, expectedHeaderElements.get(i));
        } else {
          failure(resultRecorder, expectedHeaderElements.get(i).getFirstChildElement("span"), header.getValue(), actual.toString());
        }
      }
    }

    if (expectation.body != null) {
      String resultBody = httpResult.getBody();
      if (expectation.body.equals(resultBody)) {
        success(resultRecorder, expectedBodyElement);
      } else {
        failure(resultRecorder, expectedBodyElement, expectation.body, resultBody);
      }
    }

    if (!variableName.isEmpty()) {
      evaluator.setVariable(variableName, httpResult);
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

    Element responseHeader = new Element("div").addAttribute("class", "responseCaption").appendText("Response:");
    expectationElement.appendSister(responseHeader);
    parentElement.removeChild(expectationElement);

    Element response = new Element("div").addAttribute("class", "responseContent");
    responseHeader.appendSister(response);

    Element responsePre = new Element("pre");
    response.appendChild(responsePre);

    responsePre.appendChild(new Element("span").addAttribute("class", "defaultValue").appendText("HTTP/1.1"));
    responsePre.appendText(" ");
    expectedStatusElement = new Element("span").appendText("" + expectation.status).addAttribute("class", "respStatus");
    responsePre.appendChild(expectedStatusElement);
    expectedHeaderElements = Lists.newArrayList();
    for (AbstractMap.SimpleEntry<String, String> header : expectation.headers) {
      responsePre.appendText("\n");
      Element headerEl = new Element("span").addAttribute("class", "respHeader");
      expectedHeaderElements.add(headerEl);
      headerEl.appendText(header.getKey() + ": ");
      headerEl.appendChild(new Element("span").appendText(header.getValue()).addAttribute("class", "respHeaderValue"));
      responsePre.appendChild(headerEl);
    }
    if (expectation.hasBody()) {
      responsePre.appendText("\n\n");
      expectedBodyElement = new Element("span").appendText(expectation.body).addAttribute("class", "respBody");
      responsePre.appendChild(expectedBodyElement);
    }
  }

  private void formatRequestExpectation(Element requestElement) {
    Element parentElement = requestElement.getParentElement();

    Element requestHeader = new Element("div").addAttribute("class", "requestCaption").appendText("Request:");
    requestElement.appendSister(requestHeader);
    parentElement.removeChild(requestElement);

    Element request = new Element("div").addAttribute("class", "requestContent");
    requestHeader.appendSister(request);

    Element requestPre = new Element("pre");
    request.appendChild(requestPre);

    requestPre.appendChild(new Element("span").appendText(httpRequest.method + " "));
    requestPre.appendChild(new Element("b").appendText(httpRequest.url + " "));
    requestPre.appendChild(new Element("span").addAttribute("class", "defaultValue").appendText("HTTP/1.1"));
    for (AbstractMap.SimpleEntry<String, String> header : httpRequest.headers) {
      requestPre.appendText("\n" + header.getKey() + ": " + header.getValue());
    }
  }

  private String getTextAndRemoveIndent(Element element) {
    String text = element.getText();
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

  private HttpExpectation parseExpectedResponse(Element element) {
    String contents = getTextAndRemoveIndent(element);
    SessionInputBufferImpl buffer = new SessionInputBufferImpl(new HttpTransportMetricsImpl(), contents.length());
    buffer.bind(new ByteArrayInputStream(contents.getBytes(StandardCharsets.UTF_8)));
    DefaultHttpResponseParser defaultHttpResponseParser = new DefaultHttpResponseParser(buffer);

    int statusCode = -1;
    String body = null;
    List<AbstractMap.SimpleEntry<String, String>> headers = Lists.newArrayList();
    try {
      HttpResponse httpResponse = defaultHttpResponseParser.parse();
      StatusLine statusLine = httpResponse.getStatusLine();
      statusCode = statusLine.getStatusCode();

      for (Header header : httpResponse.getAllHeaders()) {
        headers.add(new AbstractMap.SimpleEntry<>(header.getName(), header.getValue()));
      }

      if (buffer.hasBufferedData()) {
        body = "";

        while (buffer.hasBufferedData()) {
          body += (char) buffer.read();
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    } catch (HttpException e) {
      e.printStackTrace();
    }

    return new HttpExpectation(statusCode, body, headers);
  }

  private HttpRequest parseRequest(Element element, Evaluator evaluator) {
    String contents = getTextAndRemoveIndent(element);
    SessionInputBufferImpl buffer = new SessionInputBufferImpl(new HttpTransportMetricsImpl(), contents.length());
    buffer.bind(new ByteArrayInputStream(contents.getBytes(StandardCharsets.UTF_8)));
    DefaultHttpRequestParser defaultHttpRequestParser = new DefaultHttpRequestParser(buffer);

    String method = "";
    String url = "";
    List<AbstractMap.SimpleEntry<String, String>> headers = Lists.newArrayList();
    String body = null;
    try {
      org.apache.http.HttpRequest httpRequest = defaultHttpRequestParser.parse();
      method = httpRequest.getRequestLine().getMethod();
      url = httpRequest.getRequestLine().getUri();
      if (url.startsWith("#")) {
        url = "" + evaluator.evaluate(url);
      }

      for (Header header : httpRequest.getAllHeaders()) {
        headers.add(new AbstractMap.SimpleEntry<>(header.getName(), header.getValue()));
      }

      if (buffer.hasBufferedData()) {
        body = "";

        while (buffer.hasBufferedData()) {
          body += (char) buffer.read();
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    } catch (HttpException e) {
      e.printStackTrace();
    }

    return new HttpRequest(method, url, headers, body);
  }
}
