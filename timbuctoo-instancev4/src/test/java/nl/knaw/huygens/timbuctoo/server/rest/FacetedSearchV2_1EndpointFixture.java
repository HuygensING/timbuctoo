package nl.knaw.huygens.timbuctoo.server.rest;

import nl.knaw.huygens.concordion.extensions.HttpExpectation;
import nl.knaw.huygens.concordion.extensions.HttpResult;
import org.concordion.api.FullOGNL;
import org.concordion.integration.junit4.ConcordionRunner;
import org.json.JSONException;
import org.junit.runner.RunWith;
import org.skyscreamer.jsonassert.JSONCompare;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.skyscreamer.jsonassert.JSONCompareResult;

@FullOGNL
@RunWith(ConcordionRunner.class)
public class FacetedSearchV2_1EndpointFixture extends AbstractV2_1EndpointFixture {
  public String isFullyQualified(String url) {
    if (url.startsWith("http://") || url.startsWith("https://")) {
      return "a fully qualified HTTP url";
    } else {
      return "not a fully qualified HTTP url";
    }
  }

  @Override
  public String validate(HttpExpectation expectation, HttpResult reality) {
    if (expectation.body == null) {
      return "";
    } else {
      try {
        JSONCompareResult result =
          JSONCompare.compareJSON(expectation.body, reality.getBody(), JSONCompareMode.LENIENT);
        return result.getMessage();
      } catch (JSONException e) {
        throw new RuntimeException(e);
      }
    }
  }
}
