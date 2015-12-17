package nl.knaw.huygens.timbuctoo.server.rest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import nl.knaw.huygens.concordion.extensions.HttpExpectation;
import nl.knaw.huygens.concordion.extensions.HttpRequest;
import nl.knaw.huygens.concordion.extensions.HttpResult;
import org.concordion.integration.junit4.ConcordionRunner;
import org.json.JSONException;
import org.junit.runner.RunWith;
import org.skyscreamer.jsonassert.JSONCompare;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.skyscreamer.jsonassert.JSONCompareResult;
import org.skyscreamer.jsonassert.RegularExpressionValueMatcher;
import org.skyscreamer.jsonassert.ValueMatcherException;
import org.skyscreamer.jsonassert.comparator.DefaultComparator;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@RunWith(ConcordionRunner.class)
public class WwDocumentV2_1EndpointFixture extends AbstractV2_1EndpointFixture {
  private final ObjectMapper objectMapper;

  private String recordId;

  public WwDocumentV2_1EndpointFixture() {
    this.objectMapper = new ObjectMapper();
  }

  public int getNumberOfItems(HttpResult result) {
    JsonNode jsonNode = getBody(result);
    return Lists.newArrayList(jsonNode.elements()).size();
  }

  private JsonNode getBody(HttpResult result) {
    try {
      return objectMapper.readTree(result.getBody().getBytes());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public boolean doesNotContainResult(HttpResult resultToTest, HttpResult resultToBeContained) {
    List<String> idsToTest = getIds(resultToTest);
    List<String> idsToBeContained = getIds(resultToBeContained);

    return !idsToTest.containsAll(idsToBeContained);
  }

  private List<String> getIds(HttpResult result) {
    JsonNode body = getBody(result);
    ArrayList<String> ids = Lists.newArrayList();
    for (Iterator<JsonNode> elements = body.elements(); elements.hasNext(); ) {
      ids.add(elements.next().get("_id").textValue());
    }

    return ids;
  }

  public String validateIdFromLocationHeader(HttpExpectation expectation, HttpResult reality) {
    recordId = reality.getHeaders().get("location").replaceAll(".*\\/", "");
    if (!recordId.matches("^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$")) {
      return "not a valid UUID: " + recordId;
    }
    return "";
  }

  @Override
  public String validate(HttpExpectation expectation, HttpResult reality) {
    if (expectation.body == null) {
      return "";
    }

    return validate(expectation.body, reality.getBody());
  }

  private static class RegexJsonComparator extends DefaultComparator {

    public RegexJsonComparator(JSONCompareMode mode) {
      super(mode);
    }

    @Override
    public void compareValues(String prefix, Object expectedValue, Object actualValue, JSONCompareResult result)
            throws JSONException {

      if (expectedValue instanceof String && ((String) expectedValue).startsWith("/") &&
              ((String) expectedValue).endsWith("/")) {
        RegularExpressionValueMatcher<Object> matcher = new RegularExpressionValueMatcher<>();
        try {
          matcher.equal(actualValue, ((String) expectedValue).substring(1, ((String) expectedValue).length() - 1));
        } catch (ValueMatcherException e) {
          result.fail(prefix, e);
        }
      } else {
        super.compareValues(prefix, expectedValue, actualValue, result);
      }
    }
  }

  private String validate(String expectationBody, String realityBody) {
    try {
      JSONCompareResult jsonCompareResult =
              JSONCompare.compareJSON(expectationBody, realityBody, new RegexJsonComparator(JSONCompareMode.LENIENT));

      return jsonCompareResult.getMessage();
    } catch (JSONException e) {
      throw new RuntimeException(e);
    }
  }

  public String validatePostWithEmptyBodyResponse(HttpExpectation expectation, HttpResult reality) {
    final String expected = "missing property '@type'";
    return reality.getBody().contains(expected) ?
            "" :
            "Expected response to contain: '" + expected + "', but got: '" + reality.getBody() + "'";
  }

  public String getAuthenticationToken() {
    List<AbstractMap.SimpleEntry<String, String>> headers = Lists.newArrayList();
    headers.add(new AbstractMap.SimpleEntry<String, String>("Authorization",  "Basic dXNlcjpwYXNzd29yZA=="));

    HttpRequest loginRequest =
            new HttpRequest("POST", "/v2.1/authenticate", headers, null, null, Lists.newArrayList());

    Response response = doHttpCommand(loginRequest);

    return response.getHeaderString("x_auth_token");
  }

  public String retrievePid() {
    String pid = null;
    HttpRequest getRequest =
        new HttpRequest("GET", "/v2.1/domain/wwdocuments/" + recordId,
            Lists.newArrayList(), null, null, Lists.newArrayList());

    Response response = doHttpCommand(getRequest);

    return pid;
  }

  public String getRecordId() {
    return recordId;
  }
}
