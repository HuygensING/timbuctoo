package nl.knaw.huygens.timbuctoo.rest.filters;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import javax.ws.rs.WebApplicationException;

import nl.knaw.huygens.timbuctoo.rest.filters.VREAuthorizationFilterFactory.VREAuthorizationResourceFilter;
import nl.knaw.huygens.timbuctoo.rest.util.CustomHeaders;
import nl.knaw.huygens.timbuctoo.vre.VRE;
import nl.knaw.huygens.timbuctoo.vre.VREManager;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.sun.jersey.api.client.ClientResponse.Status;
import com.sun.jersey.api.representation.Form;
import com.sun.jersey.spi.container.ContainerRequest;

public class VREAuthorizationResourceFilterTest {
  private static final String VRE_ID = "testVRE";
  private VREAuthorizationResourceFilter instance;
  private VREManager vreManager;
  private Form form;

  @Before
  public void setUp() {
    form = mock(Form.class);
    vreManager = mock(VREManager.class);
    instance = new VREAuthorizationResourceFilter(vreManager);
  }

  @After
  public void tearDown() {
    form = null;
    vreManager = null;
    instance = null;
  }

  @Test
  public void testFilterValidVREId() {
    ContainerRequest request = setupRequestForDomainEntities(VRE_ID, form);

    VRE vre = setUpVRE(VRE_ID);

    instance.filter(request);

    verify(vreManager, only()).getVREById(VRE_ID);
    verify(form, times(1)).add(CustomHeaders.VRE_KEY, vre);
    verifyZeroInteractions(vre);
  }

  @Test
  public void testFilterNoVREIdSent() {
    ContainerRequest request = setupRequestForDomainEntities(null, form);

    try {
      instance.filter(request);
    } catch (WebApplicationException ex) {
      assertEquals(Status.UNAUTHORIZED.getStatusCode(), ex.getResponse().getStatus());
      verifyZeroInteractions(vreManager, form);
    }
  }

  @Test
  public void testFilterUnknownVRE() {
    ContainerRequest request = setupRequestForDomainEntities(VRE_ID, form);
    when(vreManager.getVREById(VRE_ID)).thenReturn(null);

    try {
      instance.filter(request);
    } catch (WebApplicationException ex) {
      assertEquals(Status.FORBIDDEN.getStatusCode(), ex.getResponse().getStatus());
      verify(vreManager, only()).getVREById(VRE_ID);
      verifyZeroInteractions(form);
    }
  }

  private ContainerRequest setupRequestForDomainEntities(String vreId, Form form) {
    ContainerRequest request = mock(ContainerRequest.class);
    when(request.getHeaderValue(CustomHeaders.VRE_ID_KEY)).thenReturn(vreId);
    when(request.getFormParameters()).thenReturn(form);
    return request;
  }

  private VRE setUpVRE(String vreId) {
    VRE vre = mock(VRE.class);
    when(vreManager.getVREById(vreId)).thenReturn(vre);
    return vre;
  }

}
