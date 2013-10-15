package nl.knaw.huygens.timbuctoo.rest.resources;

import static org.junit.Assert.assertEquals;
import nl.knaw.huygens.timbuctoo.config.Paths;

import org.junit.Test;

import com.google.common.collect.Lists;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

public class SiteMapResourceTest extends WebServiceTestSetup {

  @Test
  public void testGetSitemap() {
    setUpUserRoles(USER_ID, Lists.newArrayList("USER"));
    WebResource resource = super.resource();

    ClientResponse response = resource.path(Paths.SYSTEM_PREFIX).path("api").header("Authorization", "bearer 12333322abef").get(ClientResponse.class);

    assertEquals(ClientResponse.Status.OK, response.getClientResponseStatus());
  }

}
