package nl.knaw.huygens.timbuctoo.rest.filters;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import javax.ws.rs.WebApplicationException;

import nl.knaw.huygens.timbuctoo.config.Paths;
import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.VREAuthorization;
import nl.knaw.huygens.timbuctoo.rest.filters.VREAuthorizationFilterFactory.VREAuthorizationResourceFilter;
import nl.knaw.huygens.timbuctoo.rest.model.projecta.ProjectADomainEntity;
import nl.knaw.huygens.timbuctoo.rest.util.CustomHeaders;
import nl.knaw.huygens.timbuctoo.storage.StorageManager;
import nl.knaw.huygens.timbuctoo.vre.Scope;
import nl.knaw.huygens.timbuctoo.vre.VRE;
import nl.knaw.huygens.timbuctoo.vre.VREManager;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.sun.jersey.api.client.ClientResponse.Status;
import com.sun.jersey.spi.container.ContainerRequest;

public class VREAuthorizationResourceFilterTest {
  private static final String DEFAULT_USER_ID = "USER00000000012";
  private static final String USER_PATH = Paths.SYSTEM_PREFIX + "/" + Paths.USER_PATH;
  private static final String DEFAULT_ID = "PADE00000000001";
  private static final Class<ProjectADomainEntity> DEFAULT_TYPE = ProjectADomainEntity.class;
  private static final String VRE_ID = "testVRE";
  private VREAuthorizationResourceFilter instance;
  private VREManager vreManager;
  private TypeRegistry typeRegistry;
  private StorageManager storageManager;

  @Before
  public void setUp() {
    vreManager = mock(VREManager.class);
    typeRegistry = new TypeRegistry("timbuctoo.rest.model timbuctoo.rest.model.projecta");
    storageManager = mock(StorageManager.class);
    instance = new VREAuthorizationResourceFilter(vreManager, typeRegistry, storageManager);
  }

  @Test
  public void testFilterDomainEntityInVREScope() {
    ContainerRequest request = setupRequestForDomainEntities(VRE_ID, DEFAULT_TYPE, DEFAULT_ID);

    Scope scope = setUpScopeForItem(true);
    VRE vre = setUpVRE(VRE_ID, scope);

    instance.filter(request);

    verify(vreManager, only()).getVREById(VRE_ID);
    verify(vre, only()).getScope();
    verify(scope, only()).inScope(DEFAULT_TYPE, DEFAULT_ID);
    verifyZeroInteractions(storageManager);
  }

  @Test
  public void testFilterDomainEntityCollectionInVREScope() {
    ContainerRequest request = setupRequestForDomainEntities(VRE_ID, DEFAULT_TYPE, null);

    Scope scope = setupScopeForType(true);
    VRE vre = setUpVRE(VRE_ID, scope);

    instance.filter(request);

    verify(vreManager, only()).getVREById(VRE_ID);
    verify(vre, only()).getScope();
    verify(scope, only()).isTypeInScope(DEFAULT_TYPE);
    verifyZeroInteractions(storageManager);
  }

  @Test
  public void testFilterNoVREIdSent() {
    ContainerRequest request = setupRequestForDomainEntities(null, DEFAULT_TYPE, null);

    try {
      instance.filter(request);
    } catch (WebApplicationException ex) {
      assertEquals(Status.UNAUTHORIZED.getStatusCode(), ex.getResponse().getStatus());
      verifyZeroInteractions(vreManager, storageManager);
    }
  }

  @Test
  public void testFilterUnknownVRE() {
    ContainerRequest request = setupRequestForDomainEntities(VRE_ID, DEFAULT_TYPE, null);
    when(vreManager.getVREById(VRE_ID)).thenReturn(null);

    try {
      instance.filter(request);
    } catch (WebApplicationException ex) {
      assertEquals(Status.FORBIDDEN.getStatusCode(), ex.getResponse().getStatus());
      verify(vreManager, only()).getVREById(VRE_ID);
      verifyZeroInteractions(storageManager);
    }

  }

  @Test
  public void testFilterDomainEntityNotInSope() {
    ContainerRequest request = setupRequestForDomainEntities(VRE_ID, DEFAULT_TYPE, DEFAULT_ID);

    Scope scope = setUpScopeForItem(true);
    VRE vre = setUpVRE(VRE_ID, scope);

    try {
      instance.filter(request);
    } catch (WebApplicationException ex) {
      assertEquals(Status.FORBIDDEN.getStatusCode(), ex.getResponse().getStatus());

      verify(vreManager, only()).getVREById(VRE_ID);
      verify(vre, only()).getScope();
      verify(scope, only()).inScope(DEFAULT_TYPE, DEFAULT_ID);
      verifyZeroInteractions(storageManager);
    }
  }

  @Test
  public void testFilterDomainEntityCollectionNotInScope() {
    ContainerRequest request = setupRequestForDomainEntities(VRE_ID, DEFAULT_TYPE, null);

    Scope scope = setupScopeForType(false);
    VRE vre = setUpVRE(VRE_ID, scope);

    try {
      instance.filter(request);
    } catch (WebApplicationException ex) {
      assertEquals(Status.FORBIDDEN.getStatusCode(), ex.getResponse().getStatus());

      verify(vreManager, only()).getVREById(VRE_ID);
      verify(vre, only()).getScope();
      verify(scope, only()).isTypeInScope(DEFAULT_TYPE);
      verifyZeroInteractions(storageManager);
    }
  }

  @Test
  public void testFilterUserInfoUserKnownInVRE() {
    ContainerRequest request = mock(ContainerRequest.class);
    when(request.getHeaderValue(CustomHeaders.VRE_ID_KEY)).thenReturn(VRE_ID);
    when(request.getPath()).thenReturn(USER_PATH + "/" + DEFAULT_USER_ID);

    VREAuthorization example = createVREAuthorizationExample(DEFAULT_USER_ID);
    when(storageManager.findEntity(VREAuthorization.class, example)).thenReturn(example);

    VRE vre = setUpVRE(VRE_ID, null);

    instance.filter(request);

    verify(vreManager).getVREById(VRE_ID);
    verifyZeroInteractions(vre);
    verify(storageManager, only()).findEntity(VREAuthorization.class, example);
  }

  @Test
  public void testFilterUserCollection() {
    ContainerRequest request = mock(ContainerRequest.class);
    when(request.getHeaderValue(CustomHeaders.VRE_ID_KEY)).thenReturn(VRE_ID);
    when(request.getPath()).thenReturn(USER_PATH);

    VRE vre = setUpVRE(VRE_ID, null);

    instance.filter(request);

    verify(vreManager).getVREById(VRE_ID);

    verifyZeroInteractions(storageManager, vre);
  }

  @Test
  public void testFilterUserInfoUserNotKnownInVRE() {
    ContainerRequest request = mock(ContainerRequest.class);
    when(request.getHeaderValue(CustomHeaders.VRE_ID_KEY)).thenReturn(VRE_ID);
    when(request.getPath()).thenReturn(USER_PATH + "/" + DEFAULT_USER_ID);

    VREAuthorization example = createVREAuthorizationExample(DEFAULT_USER_ID);
    when(storageManager.findEntity(VREAuthorization.class, example)).thenReturn(example);

    Scope scope = setUpScopeForItem(true);
    setUpVRE(VRE_ID, scope);

    try {
      instance.filter(request);
    } catch (WebApplicationException ex) {
      assertEquals(Status.FORBIDDEN.getStatusCode(), ex.getResponse().getStatus());

      verify(vreManager).getVREById(VRE_ID);
      verify(storageManager, only()).findEntity(VREAuthorization.class, example);
    }
  }

  @Test
  public void testFilterSystemEntity() {
    ContainerRequest request = mock(ContainerRequest.class);
    when(request.getHeaderValue(CustomHeaders.VRE_ID_KEY)).thenReturn(VRE_ID);
    when(request.getPath()).thenReturn(Paths.SYSTEM_PREFIX + "/" + "relationtypes");

    Scope scope = setUpScopeForItem(true);
    VRE vre = setUpVRE(VRE_ID, scope);

    try {
      instance.filter(request);
    } catch (WebApplicationException ex) {

      assertEquals(Status.FORBIDDEN.getStatusCode(), ex.getResponse().getStatus());
      verify(vreManager, only()).getVREById(VRE_ID);
      verifyZeroInteractions(vre);
    }
  }

  protected ContainerRequest setupRequestForDomainEntities(String vreId, Class<ProjectADomainEntity> type, String id) {
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

  protected VREAuthorization createVREAuthorizationExample(String userId) {
    VREAuthorization example = new VREAuthorization();
    example.setUserId(userId);
    example.setVreId(VRE_ID);
    return example;
  }

  private String createPath(Class<? extends DomainEntity> type, String id) {
    return Paths.DOMAIN_PREFIX + "/" + (id != null ? typeRegistry.getXNameForType(type) + "/" + id : typeRegistry.getXNameForType(type));
  }
}
