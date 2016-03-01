package nl.knaw.huygens.timbuctoo.server.endpoints.v2;

import org.json.JSONArray;
import org.json.JSONException;
import org.skyscreamer.jsonassert.JSONCompareResult;
import org.skyscreamer.jsonassert.LocationAwareValueMatcher;
import org.skyscreamer.jsonassert.comparator.JSONComparator;

import java.text.MessageFormat;

public class OmissionsAllowedArrayMatcher<T> implements LocationAwareValueMatcher<T> {
  private final JSONComparator comparator;

  public OmissionsAllowedArrayMatcher(JSONComparator comparator) {
    this.comparator = comparator;
  }

  public boolean equal(T o1, T o2) {
    throw new RuntimeException("I was assuming that this one is never called if the override below is provided");
  }

  public boolean equal(String prefix, T actual, T expected, JSONCompareResult result) {
    if (!(actual instanceof JSONArray)) {
      throw new IllegalArgumentException("ArrayValueMatcher applied to non-array actual value");
    } else if (!(expected instanceof JSONArray)) {
      throw new IllegalArgumentException("ArrayValueMatcher applied to non-array expected value");
    } else {
      try {
        JSONArray actualArray = (JSONArray) actual;
        JSONArray expectedArray = (JSONArray) expected;
        int expectedLen = expectedArray.length();

        for (int i = 0; i < expectedLen; i++) {
          String elementPrefix = MessageFormat.format("{0}[{1}]", prefix, i);
          if (!expectedArray.isNull(i)) {
            Object expectedElement = expectedArray.get(i);
            Object actualElement = actualArray.get(i);
            this.comparator.compareValues(elementPrefix, expectedElement, actualElement, result);
          }
        }
        return true;
      } catch (JSONException e) {
        return false;
      }
    }
  }
}
