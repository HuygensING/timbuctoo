package nl.knaw.huygens.concordion.extensions;

import org.json.JSONException;
import org.skyscreamer.jsonassert.JSONCompare;
import org.skyscreamer.jsonassert.JSONCompareResult;

/**
 * A Comparator that matches values to a regex, and arrays using an or structure.
 */
public class JsonSpecValidator {

  public static String equals(String actualJson, String expectedJson) {
    try {
      JSONCompareResult result = JSONCompare.compareJSON(expectedJson, actualJson, new JsonSpecComparator());
      if (result.passed()) {
        return "";
      } else {
        return result.getMessage();
      }
    } catch (JSONException e) {
      return e.getMessage();
    }
  }

}
