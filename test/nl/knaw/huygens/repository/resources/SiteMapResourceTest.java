package nl.knaw.huygens.repository.resources;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import javax.ws.rs.core.SecurityContext;

import org.junit.Test;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

public class SiteMapResourceTest extends WebServiceTestSetup {

  @Test
  public void testGetSitemap() {
    SecurityContext securityContext = mock(SecurityContext.class);
    when(securityContext.isUserInRole(anyString())).thenReturn(true);

    WebResource resource = super.resource();

    ClientResponse response = resource.path("/api").header("Authorization", "bearer 12333322abef").get(ClientResponse.class);

    assertEquals(ClientResponse.Status.OK, response.getClientResponseStatus());
  }

  @Test
  public void testGetSitemapNotLoggedIn() {
    WebResource resource = super.resource();

    ClientResponse response = resource.path("/api").get(ClientResponse.class);

    assertEquals(ClientResponse.Status.UNAUTHORIZED, response.getClientResponseStatus());
  }
}
