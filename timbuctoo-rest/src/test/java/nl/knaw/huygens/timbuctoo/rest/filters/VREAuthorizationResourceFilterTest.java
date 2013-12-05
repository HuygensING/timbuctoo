package nl.knaw.huygens.timbuctoo.rest.filters;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.ws.rs.WebApplicationException;

import nl.knaw.huygens.timbuctoo.config.Paths;
import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.rest.filters.VREAuthorizationFilterFactory.VREAuthorizationResourceFilter;
import nl.knaw.huygens.timbuctoo.rest.model.projecta.ProjectADomainEntity;
import nl.knaw.huygens.timbuctoo.rest.util.CustomHeaders;
import nl.knaw.huygens.timbuctoo.vre.Scope;
import nl.knaw.huygens.timbuctoo.vre.VRE;
import nl.knaw.huygens.timbuctoo.vre.VREManager;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;

import com.sun.jersey.api.client.ClientResponse.Status;
import com.sun.jersey.spi.container.ContainerRequest;

public class VREAuthorizationResourceFilterTest {
  private static final Class<ProjectADomainEntity> DEFAULT_TYPE = ProjectADomainEntity.class;
  private static final String VRE_ID = "testVRE";
  private VREAuthorizationResourceFilter instance;
  private VREManager vreManager;
  private TypeRegistry typeRegistry;

  @Before
  public void setUp() {
    vreManager = mock(VREManager.class);
    typeRegistry = new TypeRegistry("timbuctoo.rest.model timbuctoo.rest.model.projecta");
    instance = new VREAuthorizationResourceFilter(vreManager, typeRegistry);
  }

  @Test
  public void testFilterDomainEntityInVREScope() {
    String id = "PADE00000000001";
    ContainerRequest request = setupRequest(VRE_ID, DEFAULT_TYPE, id);

    Scope scope = setUpScopeForItem(true);
    VRE vre = setUpVRE(VRE_ID, scope);

    instance.filter(request);

    verify(vreManager, only()).getVREById(VRE_ID);
    verify(vre, only()).getScope();
    verify(scope, only()).inScope(DEFAULT_TYPE, id);
  }

  @Test
  public void testFilterDomainEntityCollectionInVREScope() {
    ContainerRequest request = setupRequest(VRE_ID, DEFAULT_TYPE, null);

    Scope scope = setupScopeForType(true);
    VRE vre = setUpVRE(VRE_ID, scope);

    instance.filter(request);

    verify(vreManager, only()).getVREById(VRE_ID);
    verify(vre, only()).getScope();
    verify(scope, only()).isTypeInScope(DEFAULT_TYPE);
  }

  @Test
  public void testFilterNoVREIdSent() {
    ContainerRequest request = setupRequest(null, DEFAULT_TYPE, null);

    try {
      instance.filter(request);
    } catch (WebApplicationException ex) {
      assertEquals(Status.UNAUTHORIZED.getStatusCode(), ex.getResponse().getStatus());
      verify(vreManager, never()).getVREById(VRE_ID);
    }
  }

  @Test
  public void testFilterUnknownVRE() {
    ContainerRequest request = setupRequest(VRE_ID, DEFAULT_TYPE, null);
    when(vreManager.getVREById(VRE_ID)).thenReturn(null);

    try {
      instance.filter(request);
    } catch (WebApplicationException ex) {
      assertEquals(Status.FORBIDDEN.getStatusCode(), ex.getResponse().getStatus());
      verify(vreManager, only()).getVREById(VRE_ID);
    }

  }

  @Test
  public void testFilterDomainEntityNotInSope() {
    String id = "PADE00000000001";
    ContainerRequest request = setupRequest(VRE_ID, DEFAULT_TYPE, id);

    Scope scope = setUpScopeForItem(true);
    VRE vre = setUpVRE(VRE_ID, scope);

    try {
      instance.filter(request);
    } catch (WebApplicationException ex) {
      assertEquals(Status.FORBIDDEN.getStatusCode(), ex.getResponse().getStatus());

      verify(vreManager, only()).getVREById(VRE_ID);
      verify(vre, only()).getScope();
      verify(scope, only()).inScope(DEFAULT_TYPE, id);
    }
  }

  @Test
  public void testFilterDomainEntityCollectionNotInScope() {
    ContainerRequest request = setupRequest(VRE_ID, DEFAULT_TYPE, null);

    Scope scope = setupScopeForType(false);
    VRE vre = setUpVRE(VRE_ID, scope);

    try {
      instance.filter(request);
    } catch (WebApplicationException ex) {
      assertEquals(Status.FORBIDDEN.getStatusCode(), ex.getResponse().getStatus());

      verify(vreManager, only()).getVREById(VRE_ID);
      verify(vre, only()).getScope();
      verify(scope, only()).isTypeInScope(DEFAULT_TYPE);
    }
  }

  @Test
  @Ignore
  public void testFilterUserInfo() {
    fail("Yet to be implemented.");
  }

  @Test
  @Ignore
  public void testFilterSystemEntityCollection() {
    fail("Yet to be implemented");
  }

  protected ContainerRequest setupRequest(String vreId, Class<ProjectADomainEntity> type, String id) {
    ContainerRequest request = mock(ContainerRequest.class);
    when(request.getHeaderValue(CustomHeaders.VRE_ID_KEY)).thenReturn(vreId);
    when(request.getPath()).thenReturn(createPath(type, id));
    return request;
  }

  protected Scope setUpScopeForItem(boolean inScope) {
    Scope scope = mock(Scope.class);
    when(scope.inScope(Mockito.<Class<? extends DomainEntity>> any(), anyString())).thenReturn(inScope);
    return scope;
  }

  protected Scope setupScopeForType(boolean inScope) {
    Scope scope = mock(Scope.class);
    when(scope.isTypeInScope(Mockito.<Class<? extends DomainEntity>> any())).thenReturn(inScope);
    return scope;
  }

  protected VRE setUpVRE(String vreId, Scope scope) {
    VRE vre = mock(VRE.class);
    when(vre.getScope()).thenReturn(scope);
    when(vreManager.getVREById(vreId)).thenReturn(vre);
    return vre;
  }

  private String createPath(Class<? extends DomainEntity> type, String id) {
    return Paths.DOMAIN_PREFIX + "/" + (id != null ? typeRegistry.getXNameForType(type) + "/" + id : typeRegistry.getXNameForType(type));
  }
}
