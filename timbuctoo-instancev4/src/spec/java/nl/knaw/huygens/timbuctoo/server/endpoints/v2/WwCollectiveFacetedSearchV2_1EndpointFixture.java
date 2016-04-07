package nl.knaw.huygens.timbuctoo.server.endpoints.v2;

import org.apache.commons.lang3.StringUtils;
import org.concordion.api.FullOGNL;
import org.concordion.integration.junit4.ConcordionRunner;
import org.junit.ClassRule;
import org.junit.runner.RunWith;

import io.dropwizard.testing.ResourceHelpers;
import io.dropwizard.testing.junit.DropwizardAppRule;
import nl.knaw.huygens.timbuctoo.server.TimbuctooConfiguration;
import nl.knaw.huygens.timbuctoo.server.TimbuctooV4;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

@FullOGNL
@RunWith(ConcordionRunner.class)
public class WwCollectiveFacetedSearchV2_1EndpointFixture extends AbstractV2_1EndpointFixture {
	  @ClassRule
	  public static final DropwizardAppRule<TimbuctooConfiguration> APPLICATION;

	  static {
	    APPLICATION = new DropwizardAppRule<>(TimbuctooV4.class,
	            ResourceHelpers.resourceFilePath("acceptance_test_config.yaml"));
	  }

  public String isFullyQualified(String url) {
    if (StringUtils.isBlank(url) || (!url.startsWith("http://") && !url.startsWith("https://"))) {
      return "not a fully qualified HTTP url";
    } else {
      return "a fully qualified HTTP url";
    }
  }

  public String doRequestWithMalformedUuid() {
    Response response = returnUrlToMockedOrRealServer(null)
        .path("v2.1").path("search").path("malformedUUID")
        .request()
        .get();

    return String.format("%s %s", response.getStatus(), response.getStatusInfo());
  }

  public String doGetOfUnknown() {
    Response response = returnUrlToMockedOrRealServer(null)
        .path("v2.1").path("search").path("dd8181cf-30b0-47e3-9787-61d09579306e")
        .request()
        .get();

    return String.format("%s %s", response.getStatus(), response.getStatusInfo());
  }

  @Override
  protected WebTarget returnUrlToMockedOrRealServer(String serverAddress) {
    String defaultAddress = String.format("http://localhost:%d", APPLICATION.getLocalPort());
    String address = serverAddress != null ? serverAddress : defaultAddress;

    return ClientBuilder.newClient().target(address);
  }
}
