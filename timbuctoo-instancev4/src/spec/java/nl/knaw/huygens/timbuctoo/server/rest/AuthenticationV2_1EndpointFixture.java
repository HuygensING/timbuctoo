package nl.knaw.huygens.timbuctoo.server.rest;

import io.dropwizard.testing.junit.ResourceTestRule;
import nl.knaw.huygens.concordion.extensions.HttpExpectation;
import nl.knaw.huygens.concordion.extensions.HttpResult;
import org.apache.commons.lang3.StringUtils;
import org.concordion.integration.junit4.ConcordionRunner;
import org.json.JSONException;
import org.junit.ClassRule;
import org.junit.runner.RunWith;
import org.skyscreamer.jsonassert.JSONCompare;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.skyscreamer.jsonassert.JSONCompareResult;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;

@RunWith(ConcordionRunner.class)
public class AuthenticationV2_1EndpointFixture extends AbstractV2_1EndpointFixture {

  @ClassRule
  public static final ResourceTestRule resources =
    ResourceTestRule.builder().addResource(new AuthenticationEndPoint()).build();

  @Override
  public String validate(HttpExpectation expectation, HttpResult reality) {
    if (expectation.body == null) {
      return "";
    }

    return validateBody(expectation.body, reality.getBody());
  }

  private String validateBody(String expectationBody, String actualBody) {
    if (StringUtils.isBlank(actualBody)) {
      return "Actual body is empty";
    }
    try {
      JSONCompareResult jsonCompareResult =
        JSONCompare.compareJSON(expectationBody, actualBody, new RegexJsonComparator(JSONCompareMode.LENIENT));

      return jsonCompareResult.getMessage();
    } catch (JSONException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  protected Client getClient() {
    return ClientBuilder.newClient();
  }

}
