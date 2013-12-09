package nl.knaw.huygens.timbuctoo.rest.filters;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import javax.ws.rs.WebApplicationException;

import nl.knaw.huygens.timbuctoo.rest.filters.VREAuthorizationFilterFactory.VREAuthorizationResourceFilter;
import nl.knaw.huygens.timbuctoo.rest.util.CustomHeaders;
import nl.knaw.huygens.timbuctoo.vre.VREManager;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.sun.jersey.api.client.ClientResponse.Status;
import com.sun.jersey.spi.container.ContainerRequest;

public class VREAuthorizationResourceFilterTest {
  private static final String VRE_ID = "testVRE";
  private VREAuthorizationResourceFilter instance;
  private VREManager vreManager;

  @Before
  public void setUp() {
    vreManager = mock(VREManager.class);
    instance = new VREAuthorizationResourceFilter(vreManager);
  }

  @After
  public void tearDown() {
    vreManager = null;
    instance = null;
  }

  @Test
  public void testFilterValidVREId() {
    ContainerRequest request = setupRequestForDomainEntities(VRE_ID);

    setUpVREManager(VRE_ID, true);

    instance.filter(request);

    verify(vreManager, only()).doesVREExist(VRE_ID);
  }

  @Test(expected = WebApplicationException.class)
  public void testFilterNoVREIdSent() {
    ContainerRequest request = setupRequestForDomainEntities(null);

    try {
      instance.filter(request);
    } catch (WebApplicationException ex) {
      assertEquals(Status.UNAUTHORIZED.getStatusCode(), ex.getResponse().getStatus());
      verifyZeroInteractions(vreManager);
      throw ex;
    }
  }

  @Test(expected = WebApplicationException.class)
  public void testFilterUnknownVRE() {
    ContainerRequest request = setupRequestForDomainEntities(VRE_ID);
    setUpVREManager(VRE_ID, false);

    try {
      instance.filter(request);
    } catch (WebApplicationException ex) {
      assertEquals(Status.FORBIDDEN.getStatusCode(), ex.getResponse().getStatus());
      verify(vreManager, only()).doesVREExist(VRE_ID);
      throw ex;
    }
  }

  private ContainerRequest setupRequestForDomainEntities(String vreId) {
    ContainerRequest request = mock(ContainerRequest.class);
    when(request.getHeaderValue(CustomHeaders.VRE_ID_KEY)).thenReturn(vreId);
    return request;
  }

  private void setUpVREManager(String vreId, boolean vreExists) {
    when(vreManager.doesVREExist(vreId)).thenReturn(vreExists);
  }

}
