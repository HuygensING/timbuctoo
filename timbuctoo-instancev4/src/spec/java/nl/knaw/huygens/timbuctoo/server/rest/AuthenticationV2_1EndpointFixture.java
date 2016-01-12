package nl.knaw.huygens.timbuctoo.server.rest;

import io.dropwizard.testing.ResourceHelpers;
import io.dropwizard.testing.junit.DropwizardAppRule;
import nl.knaw.huygens.concordion.extensions.HttpExpectation;
import nl.knaw.huygens.concordion.extensions.HttpRequest;
import nl.knaw.huygens.concordion.extensions.HttpResult;
import nl.knaw.huygens.timbuctoo.server.TimbuctooConfiguration;
import nl.knaw.huygens.timbuctoo.server.TimbuctooV4;
import org.apache.commons.lang3.StringUtils;
import org.concordion.api.FullOGNL;
import org.concordion.integration.junit4.ConcordionRunner;
import org.json.JSONException;
import org.junit.ClassRule;
import org.junit.runner.RunWith;
import org.skyscreamer.jsonassert.JSONCompare;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.skyscreamer.jsonassert.JSONCompareResult;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

@FullOGNL
@RunWith(ConcordionRunner.class)
public class AuthenticationV2_1EndpointFixture extends AbstractV2_1EndpointFixture {

  @ClassRule
  public static final DropwizardAppRule<TimbuctooConfiguration> APPLICATION;

  static {
    APPLICATION = new DropwizardAppRule<>(TimbuctooV4.class,
      ResourceHelpers.resourceFilePath("default_acceptance_test_config.yaml"));
  }

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

  public String doLoginWithInvalidUsernameAndPassword() {
    HttpRequest httpRequest = new HttpRequest("POST", "/v2.1/authenticate")
      // Authorization header for unknownUser:password
      .withHeader("Authorization", "Basic dW5rbm93blVzZXI6cGFzc3dvcmQ=");

    Response response = super.executeRequestUsingJaxRs(httpRequest);

    return String.format("%s %s", response.getStatus(), response.getStatusInfo());
  }

  public String doLoginWithoutHeader() {
    HttpRequest httpRequest = new HttpRequest("POST", "/v2.1/authenticate");

    Response response = super.executeRequestUsingJaxRs(httpRequest);

    return String.format("%s %s", response.getStatus(), response.getStatusInfo());
  }

  public boolean hasContent(String value) {
    return !StringUtils.isBlank(value);
  }

  @Override
  protected WebTarget returnUrlToMockedOrRealServer(String serverAddress) {
    String defaultAddress = String.format("http://localhost:%d", APPLICATION.getLocalPort());
    String address = serverAddress != null ? serverAddress : defaultAddress;

    return ClientBuilder.newClient().target(address);
  }

}
