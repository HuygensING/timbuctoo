package nl.knaw.huygens.timbuctoo.server.rest;

import nl.knaw.huygens.concordion.extensions.HttpExpectation;
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

@RunWith(ConcordionRunner.class)
public class AuthenticationV2_1EndpointFixture extends AbstractV2_1EndpointFixture {


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

}
