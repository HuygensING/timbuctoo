package nl.knaw.huygens.timbuctoo.server.rest;

import nl.knaw.huygens.concordion.extensions.HttpExpectation;
import nl.knaw.huygens.concordion.extensions.HttpResult;
import org.concordion.integration.junit4.ConcordionRunner;
import org.json.JSONException;
import org.junit.runner.RunWith;
import org.skyscreamer.jsonassert.JSONCompare;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.skyscreamer.jsonassert.JSONCompareResult;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;

@RunWith(ConcordionRunner.class)
public class AuthenticationV2_1EndpointFixture extends AbstractV2_1EndpointFixture {

  @Override
  public String validate(HttpExpectation expectation, HttpResult reality) {
    if (expectation.body == null) {
      return "";
    }

    return validateBody(expectation.body, reality.getBody());
  }

  private String validateBody(String expectationBody, String realityBody) {
    try {
      JSONCompareResult jsonCompareResult =
        JSONCompare.compareJSON(expectationBody, realityBody, new RegexJsonComparator(JSONCompareMode.LENIENT));

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
