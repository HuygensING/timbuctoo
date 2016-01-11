package nl.knaw.huygens.timbuctoo.server.rest;

import io.dropwizard.testing.junit.ResourceTestRule;
import nl.knaw.huygens.concordion.extensions.HttpExpectation;
import nl.knaw.huygens.concordion.extensions.HttpResult;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.concordion.api.FullOGNL;
import org.concordion.integration.junit4.ConcordionRunner;
import org.json.JSONException;
import org.junit.ClassRule;
import org.junit.runner.RunWith;
import org.skyscreamer.jsonassert.JSONCompare;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.skyscreamer.jsonassert.JSONCompareResult;

import javax.ws.rs.client.Client;

@FullOGNL
@RunWith(ConcordionRunner.class)
public class FacetedSearchV2_1EndpointFixture extends AbstractV2_1EndpointFixture {

  @ClassRule
  public static ResourceTestRule resourcesTestRule = ResourceTestRule.builder()
    .addResource(FacetedSearchV2_1Endpoint.class)
    .build();

  public String isFullyQualified(String url) {

    if (StringUtils.isBlank(url) || !url.startsWith("http://") || !url.startsWith("https://")) {
      return "not a fully qualified HTTP url";
    } else {
      return "a fully qualified HTTP url";
    }
  }

  public String dontCheck(HttpExpectation expectation, HttpResult reality) {
    return "";
  }

  @Override
  public String validate(HttpExpectation expectation, HttpResult reality) {
    if (expectation.hasBody()) {
      try {
        JSONCompareResult result = JSONCompare.compareJSON(
          expectation.body,
          reality.getBody(),
          new RegexJsonComparator(JSONCompareMode.LENIENT)
        );

        return result.getMessage();
      } catch (JSONException e) {
        return ExceptionUtils.getStackTrace(e);
      }
    } else {
      return "";
    }
  }

  @Override
  protected Client getClient() {
    return resourcesTestRule.client();
  }
}
