package nl.knaw.huygens.timbuctoo.server.rest;

import io.dropwizard.testing.junit.ResourceTestRule;
import nl.knaw.huygens.concordion.extensions.HttpExpectation;
import nl.knaw.huygens.concordion.extensions.HttpRequest;
import nl.knaw.huygens.concordion.extensions.HttpResult;
import org.apache.commons.lang3.StringUtils;
import org.concordion.api.FullOGNL;
import org.concordion.integration.junit4.ConcordionRunner;
import org.json.JSONException;
import org.junit.ClassRule;
import org.junit.runner.RunWith;
import org.skyscreamer.jsonassert.JSONCompare;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.skyscreamer.jsonassert.JSONCompareResult;

import javax.ws.rs.client.Client;
import javax.ws.rs.core.Response;
import java.nio.file.Path;
import java.nio.file.Paths;

import static java.util.concurrent.TimeUnit.HOURS;

@FullOGNL
@RunWith(ConcordionRunner.class)
public class AuthenticationV2_1EndpointFixture extends AbstractV2_1EndpointFixture {

  @ClassRule
  public static final ResourceTestRule resources;

  static {
    Path basePath = Paths.get("src", "spec", "resources");
    Path loginsFile = basePath.resolve("logins.json");
    JsonBasedAuthenticator authenticator = new JsonBasedAuthenticator(loginsFile);

    Path usersFile = basePath.resolve("users.json");
    JsonBasedUserStore userStore = new JsonBasedUserStore(usersFile);

    LoggedInUserStore loggedInUserStore = new LoggedInUserStore(authenticator, userStore, new Timeout(8, HOURS));


    resources = ResourceTestRule.builder()
                                .addResource(new AuthenticationV2_1EndPoint(loggedInUserStore))
                                .addResource(new UserV2_1Endpoint(loggedInUserStore))
                                .build();
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

    Response response = super.doHttpCommand(httpRequest);

    return String.format("%s %s", response.getStatus(), response.getStatusInfo());
  }

  public String doLoginWithoutHeader() {
    HttpRequest httpRequest = new HttpRequest("POST", "/v2.1/authenticate");

    Response response = super.doHttpCommand(httpRequest);

    return String.format("%s %s", response.getStatus(), response.getStatusInfo());
  }

  public boolean hasContent(String value) {
    return !StringUtils.isBlank(value);
  }

  @Override
  protected Client getClient() {
    return resources.client();
  }

}
