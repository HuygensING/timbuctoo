package nl.knaw.huygens.timbuctoo.rest;

import io.dropwizard.testing.junit.ResourceTestRule;
import org.concordion.integration.junit4.ConcordionRunner;
import org.junit.Rule;
import org.junit.runner.RunWith;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@RunWith(ConcordionRunner.class)
public class RootEndpointFixture {
  @Rule
  public final ResourceTestRule resources = ResourceTestRule.builder().addResource(new RootEndpoint()).build();

  public int getStatusCode() {
    Response response = resources.client().target("/").request().accept(MediaType.TEXT_HTML).get();

    return response.getStatus();
  }

}
