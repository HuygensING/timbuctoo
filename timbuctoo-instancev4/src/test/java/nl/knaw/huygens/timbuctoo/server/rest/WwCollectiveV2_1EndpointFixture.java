package nl.knaw.huygens.timbuctoo.server.rest;

import com.google.common.collect.Lists;
import nl.knaw.huygens.concordion.extensions.HttpExpectation;
import nl.knaw.huygens.concordion.extensions.HttpRequest;
import nl.knaw.huygens.concordion.extensions.HttpResult;
import org.concordion.integration.junit4.ConcordionRunner;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.runner.RunWith;
import org.skyscreamer.jsonassert.JSONCompare;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.skyscreamer.jsonassert.JSONCompareResult;
import org.skyscreamer.jsonassert.RegularExpressionValueMatcher;
import org.skyscreamer.jsonassert.ValueMatcherException;
import org.skyscreamer.jsonassert.comparator.DefaultComparator;

import javax.ws.rs.core.Response;
import java.util.AbstractMap;
import java.util.List;

@RunWith(ConcordionRunner.class)
public class WwCollectiveV2_1EndpointFixture extends AbstractV2_1EndpointFixture {

  private String recordId;
  private String pid;

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

  public String retrievePid() throws JSONException {
    int attempts = 0;
    String path =  "/v2.1/domain/wwcollectives/" + recordId;
    List<AbstractMap.SimpleEntry<String, String>> headers = Lists.newArrayList();
    headers.add(new AbstractMap.SimpleEntry<String, String>("Accept", "application/json"));
    HttpRequest getRequest = new HttpRequest("GET", path, headers, null, null, Lists.newArrayList());

    while ((pid == null || pid.equalsIgnoreCase("null")) && attempts < 12) {
      Response response = doHttpCommand(getRequest);
      JSONObject data = new JSONObject(response.readEntity(String.class));
      pid = data.getString("^pid");

      attempts++;
      try {
        Thread.sleep(5000);
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
    }
    return pid;
  }


  public String validateIdFromLocationHeader(HttpExpectation expectation, HttpResult reality) {
    recordId = reality.getHeaders().get("location").replaceAll(".*\\/", "");
    if (!recordId.matches("^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$")) {
      return "not a valid UUID: " + recordId;
    }
    return "";
  }

  public String getRecordId() {
    return recordId;
  }

  public String getAuthenticationToken() {
    List<AbstractMap.SimpleEntry<String, String>> headers = Lists.newArrayList();
    headers.add(new AbstractMap.SimpleEntry<String, String>("Authorization",  "Basic dXNlcjpwYXNzd29yZA=="));

    HttpRequest loginRequest =
        new HttpRequest("POST", "/v2.1/authenticate", headers, null, null, Lists.newArrayList());

    Response response = doHttpCommand(loginRequest);

    return response.getHeaderString("x_auth_token");
  }
}
