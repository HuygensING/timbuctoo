package nl.knaw.huygens.repository.resources;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import nl.knaw.huygens.repository.server.security.OAuthAuthorizationServerConnector;

import org.junit.Before;
import org.junit.Test;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.core.PackagesResourceConfig;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.test.framework.AppDescriptor;
import com.sun.jersey.test.framework.WebAppDescriptor;

public class SiteMapResourceTest extends WebServiceTestSetup {

  @Before
  public void setUpAuthorizationServerConnectorMock() {
    oAuthAuthorizationServerConnector = injector.getInstance(OAuthAuthorizationServerConnector.class);
  }

  public void setupSecurityContext(boolean isUserAllowed) {
    when(securityContext.isUserInRole(anyString())).thenReturn(isUserAllowed);
  }

  @Override
  protected AppDescriptor configure() {
    WebAppDescriptor webAppDescriptor = new WebAppDescriptor.Builder().build();
    webAppDescriptor.getInitParams().put(PackagesResourceConfig.PROPERTY_PACKAGES, "nl.knaw.huygens.repository.resources;com.fasterxml.jackson.jaxrs.json;nl.knaw.huygens.repository.providers");
    webAppDescriptor.getInitParams().put(ResourceConfig.PROPERTY_RESOURCE_FILTER_FACTORIES,
        "nl.knaw.huygens.repository.server.security.AnnotatedSecurityFilterFactory;com.sun.jersey.api.container.filter.RolesAllowedResourceFilterFactory");

    return webAppDescriptor;
  }

  @Test
  public void testGetSitemap() {
    SecurityContext securityContext = mock(SecurityContext.class);
    when(securityContext.isUserInRole(anyString())).thenReturn(true);
    when(oAuthAuthorizationServerConnector.authenticate(anyString())).thenReturn(securityContext);

    WebResource resource = super.resource();

    ClientResponse response = resource.path("/api").get(ClientResponse.class);

    assertEquals(ClientResponse.Status.OK, response.getClientResponseStatus());
  }

  @Test
  public void testGetSitemapNotLoggedIn() {
    WebResource resource = super.resource();
    when(oAuthAuthorizationServerConnector.authenticate(anyString())).thenThrow(new WebApplicationException(Response.Status.UNAUTHORIZED));

    ClientResponse response = resource.path("/api").get(ClientResponse.class);

    assertEquals(ClientResponse.Status.UNAUTHORIZED, response.getClientResponseStatus());
  }
}
