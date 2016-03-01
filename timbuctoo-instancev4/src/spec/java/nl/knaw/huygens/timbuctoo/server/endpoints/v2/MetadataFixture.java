package nl.knaw.huygens.timbuctoo.server.endpoints.v2;

import io.dropwizard.testing.ResourceHelpers;
import io.dropwizard.testing.junit.DropwizardAppRule;
import nl.knaw.huygens.concordion.extensions.HttpExpectation;
import nl.knaw.huygens.concordion.extensions.HttpResult;
import nl.knaw.huygens.timbuctoo.server.TimbuctooConfiguration;
import nl.knaw.huygens.timbuctoo.server.TimbuctooV4;
import org.concordion.integration.junit4.ConcordionRunner;
import org.json.JSONException;
import org.junit.ClassRule;
import org.junit.runner.RunWith;
import org.skyscreamer.jsonassert.Customization;
import org.skyscreamer.jsonassert.JSONCompare;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.skyscreamer.jsonassert.JSONCompareResult;
import org.skyscreamer.jsonassert.comparator.CustomComparator;
import org.skyscreamer.jsonassert.comparator.DefaultComparator;
import org.skyscreamer.jsonassert.comparator.JSONComparator;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;

@RunWith(ConcordionRunner.class)
public class MetadataFixture extends AbstractV2_1EndpointFixture {

  @ClassRule
  public static final DropwizardAppRule<TimbuctooConfiguration> APPLICATION;

  static {
    APPLICATION = new DropwizardAppRule<>(TimbuctooV4.class,
      ResourceHelpers.resourceFilePath("acceptance_test_config.yaml"));
  }

  @Override
  protected WebTarget returnUrlToMockedOrRealServer(String serverAddress) {
    String defaultAddress = String.format("http://localhost:%d", APPLICATION.getLocalPort());
    String address = serverAddress != null ? serverAddress : defaultAddress;

    return ClientBuilder.newClient().target(address);
  }

  @Override
  protected String validate(HttpExpectation expectation, HttpResult reality) {
    if (expectation.body == null) {
      return "";
    }

    return validate(expectation.body, reality.getBody());
  }

  private String validate(String expectationBody, String realityBody) {
    try {
      JSONComparator comparator = new DefaultComparator(JSONCompareMode.LENIENT);
      Customization customization = new Customization("wwpersons", new OmissionsAllowedArrayMatcher<>(comparator));
      CustomComparator customComparator = new CustomComparator(JSONCompareMode.LENIENT, customization);

      JSONCompareResult jsonCompareResult = JSONCompare.compareJSON(expectationBody, realityBody, customComparator);

      return jsonCompareResult.getMessage();
    } catch (AssertionError e) {
      return e.getMessage();
    } catch (JSONException e) {
      throw new RuntimeException(e);
    }
  }

}
