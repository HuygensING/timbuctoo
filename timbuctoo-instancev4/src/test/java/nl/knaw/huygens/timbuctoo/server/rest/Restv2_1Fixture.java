package nl.knaw.huygens.timbuctoo.server.rest;

import io.dropwizard.testing.junit.ResourceTestRule;
import org.concordion.api.extension.ConcordionExtension;
import org.concordion.api.extension.Extension;
import org.concordion.integration.junit4.ConcordionRunner;
import org.junit.Rule;
import org.junit.runner.RunWith;

import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.Response;

@RunWith(ConcordionRunner.class)
public class Restv2_1Fixture {

  @Extension
  public ConcordionExtension extension = new HttpCommandExtension(this::doHttpCommand);

  @Rule
  public final ResourceTestRule resources = ResourceTestRule.builder().addResource(new RootEndpoint()).build();

  public Response doHttpCommand(HttpCommandExtension.HttpRequest call) {
    Invocation.Builder response = resources.client().target(call.url).request();
    response.headers(call.headers);
    return response.method(call.method);
  }

  public String concordionWorks() {
    return "concordion";
  }
}
