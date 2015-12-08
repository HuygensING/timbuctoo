package nl.knaw.huygens.timbuctoo.server.rest;

import io.dropwizard.testing.junit.ResourceTestRule;
import org.concordion.api.extension.Extension;
import org.concordion.integration.junit4.ConcordionRunner;
import org.junit.Rule;
import org.junit.runner.RunWith;

import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.Response;

@RunWith(ConcordionRunner.class)
public class WWPersonV2_1Fixture {
  @Extension
  public HttpCommandExtension commandExtension = new HttpCommandExtension(this::doHttpCommand);

  @Rule
  public final ResourceTestRule resources = ResourceTestRule.builder().addResource(WWPersonsEndPoint.class).build();

  private Response doHttpCommand(HttpCommandExtension.HttpRequest httpRequest) {
    Invocation.Builder response = resources.client().target(httpRequest.url).request();
    response.headers(httpRequest.headers);
    return response.method(httpRequest.method);
  }
}
