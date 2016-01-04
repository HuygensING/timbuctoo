package nl.knaw.huygens.timbuctoo.server.rest;

import org.json.JSONException;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.skyscreamer.jsonassert.JSONCompareResult;
import org.skyscreamer.jsonassert.RegularExpressionValueMatcher;
import org.skyscreamer.jsonassert.ValueMatcherException;
import org.skyscreamer.jsonassert.comparator.DefaultComparator;

class RegexJsonComparator extends DefaultComparator {

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
