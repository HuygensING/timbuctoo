package nl.knaw.huygens.timbuctoo.rest.resources;

import static org.junit.Assert.assertEquals;
import nl.knaw.huygens.timbuctoo.config.Paths;

import org.junit.Test;

import com.sun.jersey.api.client.ClientResponse;

public class SiteMapResourceTest extends WebServiceTestSetup {

  @Test
  public void testGetSitemap() {
    setUpUserWithRoles(USER_ID, Lists.newArrayList("USER"));
    ClientResponse response = resource().path(Paths.SYSTEM_PREFIX).path("api").header("Authorization", "bearer 12333322abef").get(ClientResponse.class);
    assertEquals(ClientResponse.Status.OK, response.getClientResponseStatus());
  }

}
