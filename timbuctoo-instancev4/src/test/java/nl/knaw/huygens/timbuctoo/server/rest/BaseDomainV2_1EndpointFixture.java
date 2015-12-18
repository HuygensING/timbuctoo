package nl.knaw.huygens.timbuctoo.server.rest;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import nl.knaw.huygens.concordion.extensions.HttpExpectation;
import nl.knaw.huygens.concordion.extensions.HttpRequest;
import nl.knaw.huygens.concordion.extensions.HttpResult;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;
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

public class BaseDomainV2_1EndpointFixture extends AbstractV2_1EndpointFixture {

  private String recordId;
  private String recordLocation;
  private String pid;
  private String authenticationToken;

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

  private final ObjectMapper objectMapper;

  public BaseDomainV2_1EndpointFixture() {
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

  @Override
  public String validate(HttpExpectation expectation, HttpResult reality) {
    if (expectation.body == null) {
      return "";
    }

    return validate(expectation.body, reality.getBody());
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

  public boolean recordHasPid() {
    return pid != null && !pid.equalsIgnoreCase("null");
  }

  public String retrievePid(String path)  {
    int attempts = 0;
    List<AbstractMap.SimpleEntry<String, String>> headers = Lists.newArrayList();
    headers.add(new AbstractMap.SimpleEntry<String, String>("Accept", "application/json"));
    HttpRequest getRequest = new HttpRequest("GET", path, headers, null, null, Lists.newArrayList());

    while ((pid == null || pid.equalsIgnoreCase("null")) && attempts < 24) {
      Response response = doHttpCommand(getRequest);
      try {
        JSONObject data = new JSONObject(response.readEntity(String.class));
        pid = data.getString("^pid");
      } catch (JSONException e) {
        // Expected exception when record is still updating
      }
      attempts++;
      try {
        Thread.sleep(2500);
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
    }
    return pid;
  }

  public String retrievePid()  {
    return retrievePid(recordLocation);
  }


  public String validateIdFromLocationHeader(HttpExpectation expectation, HttpResult reality) {
    recordLocation = reality.getHeaders().get("location").replaceAll("http://[^/]+/", "");
    recordId = reality.getHeaders().get("location").replaceAll(".*\\/", "");

    if (!recordId.matches("^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$")) {
      return "not a valid UUID: " + recordId;
    }
    return "";
  }

  public String validatePostWithEmptyBodyResponse(HttpExpectation expectation, HttpResult reality) {
    final String expected = "missing property '@type'";
    return reality.getBody().contains(expected) ?
        "" :
        "Expected response to contain: '" + expected + "', but got: '" + reality.getBody() + "'";
  }

  public String getRecordId() {
    return recordId;
  }

  public String getAuthenticationToken() {
    if (authenticationToken != null) {
      return authenticationToken;
    }
    List<AbstractMap.SimpleEntry<String, String>> headers = Lists.newArrayList();
    headers.add(new AbstractMap.SimpleEntry<String, String>("Authorization",  "Basic dXNlcjpwYXNzd29yZA=="));

    HttpRequest loginRequest =
        new HttpRequest("POST", "/v2.1/authenticate", headers, null, null, Lists.newArrayList());

    Response response = doHttpCommand(loginRequest);
    authenticationToken = response.getHeaderString("x_auth_token");
    return authenticationToken;
  }

  public boolean isValidPid(String result) throws JSONException {

    return !StringUtils.isBlank(result);
  }
}
