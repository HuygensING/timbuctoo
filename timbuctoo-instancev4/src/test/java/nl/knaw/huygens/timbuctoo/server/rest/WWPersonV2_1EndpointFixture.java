package nl.knaw.huygens.timbuctoo.server.rest;

import io.dropwizard.testing.junit.ResourceTestRule;
import org.concordion.api.extension.Extension;
import org.concordion.integration.junit4.ConcordionRunner;
import org.junit.Rule;
import org.junit.runner.RunWith;

import javax.ws.rs.core.Response;

@RunWith(ConcordionRunner.class)
public class WWPersonV2_1EndpointFixture {
  @Extension
  public HttpCommandExtension commandExtension = new HttpCommandExtension(this::doHttpCommand);

  @Rule
  public final ResourceTestRule resources = ResourceTestRule.builder().addResource(new WWPersonCollectionV2_1EndPoint()).build();

  private Response doHttpCommand(HttpCommandExtension.HttpRequest httpRequest) {
    return resources.client().target(httpRequest.url).request() //
      .headers(httpRequest.headers)
      .method(httpRequest.method);
  }
}
