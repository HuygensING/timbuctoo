package nl.knaw.huygens.timbuctoo.server.rest;

import io.dropwizard.testing.ResourceHelpers;
import io.dropwizard.testing.junit.DropwizardAppRule;
import nl.knaw.huygens.concordion.extensions.HttpExpectation;
import nl.knaw.huygens.concordion.extensions.HttpResult;
import nl.knaw.huygens.timbuctoo.server.TimbuctooConfiguration;
import nl.knaw.huygens.timbuctoo.server.TimbuctooV4;
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

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;

@FullOGNL
@RunWith(ConcordionRunner.class)
public class FacetedSearchV2_1EndpointFixture extends AbstractV2_1EndpointFixture {

  @ClassRule
  public static final DropwizardAppRule<TimbuctooConfiguration> APPLICATION;

  static {
    APPLICATION = new DropwizardAppRule<>(TimbuctooV4.class,
      ResourceHelpers.resourceFilePath("default_acceptance_test_config.yaml"));
  }

  public String isFullyQualified(String url) {

    if (StringUtils.isBlank(url) || (!url.startsWith("http://") && !url.startsWith("https://"))) {
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
  protected WebTarget returnUrlToMockedOrRealServer(String serverAddress) {
    String defaultAddress = String.format("http://localhost:%d", APPLICATION.getLocalPort());
    String address = serverAddress != null ? serverAddress : defaultAddress;

    return ClientBuilder.newClient().target(address);
  }
}
