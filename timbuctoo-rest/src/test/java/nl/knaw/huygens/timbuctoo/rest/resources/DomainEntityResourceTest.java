package nl.knaw.huygens.timbuctoo.rest.resources;

/*
 * #%L
 * Timbuctoo REST api
 * =======
 * Copyright (C) 2012 - 2015 Huygens ING
 * =======
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.ClientResponse.Status;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.WebResource;
import nl.knaw.huygens.timbuctoo.config.Paths;
import nl.knaw.huygens.timbuctoo.config.TypeNames;
import nl.knaw.huygens.timbuctoo.messages.ActionType;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.util.Change;
import nl.knaw.huygens.timbuctoo.model.util.RelationBuilder;
import nl.knaw.huygens.timbuctoo.persistence.PersistenceRequest;
import nl.knaw.huygens.timbuctoo.persistence.request.PersistenceRequestFactory;
import nl.knaw.huygens.timbuctoo.storage.NoSuchEntityException;
import nl.knaw.huygens.timbuctoo.storage.StorageIterator;
import nl.knaw.huygens.timbuctoo.storage.StorageIteratorStub;
import nl.knaw.huygens.timbuctoo.storage.UpdateException;
import nl.knaw.huygens.timbuctoo.vre.VRE;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;
import test.rest.model.BaseDomainEntity;
import test.rest.model.projecta.OtherDomainEntity;
import test.rest.model.projecta.ProjectADomainEntity;
import test.rest.model.projecta.ProjectARelation;

import javax.validation.ConstraintViolation;
import javax.validation.Path;
import javax.validation.Validator;
import javax.validation.metadata.ConstraintDescriptor;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.List;

import static com.google.common.net.HttpHeaders.AUTHORIZATION;
import static nl.knaw.huygens.timbuctoo.config.Paths.PID_PATH;
import static nl.knaw.huygens.timbuctoo.config.Paths.UPDATE_PID_PATH;
import static nl.knaw.huygens.timbuctoo.config.TypeNames.getExternalName;
import static nl.knaw.huygens.timbuctoo.rest.util.CustomHeaders.VRE_ID_KEY;
import static nl.knaw.huygens.timbuctoo.rest.util.QueryParameters.REVISION_KEY;
import static nl.knaw.huygens.timbuctoo.security.UserRoles.ADMIN_ROLE;
import static nl.knaw.huygens.timbuctoo.security.UserRoles.USER_ROLE;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

public class DomainEntityResourceTest extends WebServiceTestSetup {

  protected static final Class<ProjectADomainEntity> DEFAULT_TYPE = ProjectADomainEntity.class;
  protected static final String DEFAULT_RESOURCE = TypeNames.getExternalName(DEFAULT_TYPE);

  protected static final Class<BaseDomainEntity> BASE_TYPE = BaseDomainEntity.class;
  protected static final String BASE_RESOURCE = TypeNames.getExternalName(BASE_TYPE);

  protected static final String DEFAULT_ID = "TEST000000000001";
  public static final String UNKNOWN_TYPE_RESOURCE = "unknowntypes";


  protected WebResource createResource(String... pathElements) {
    return addPathToWebResource(resource().path(getAPIVersion()).path(Paths.DOMAIN_PREFIX), pathElements);
  }

  protected ChangeHelper getChangeHelper() {
    return injector.getInstance(ChangeHelper.class);
  }

  @SuppressWarnings("unchecked")
  protected void whenJsonProviderReadFromThenReturn(Object value) throws Exception {
    when(getJsonProvider().readFrom(any(Class.class), any(Type.class), any(Annotation[].class), any(MediaType.class), any(MultivaluedMap.class), any(InputStream.class))).thenReturn(value);
  }

  @Test
  public void testGetEntityExisting() throws Exception {
    ProjectADomainEntity entity = new ProjectADomainEntity(DEFAULT_ID);
    when(repository.getEntityOrDefaultVariationWithRelations(DEFAULT_TYPE, DEFAULT_ID)).thenReturn(entity);

    ClientResponse response = createResource(DEFAULT_RESOURCE, DEFAULT_ID) //
        .get(ClientResponse.class);
    verifyResponseStatus(response, Status.OK);

    ProjectADomainEntity actualDoc = response.getEntity(DEFAULT_TYPE);
    assertNotNull(actualDoc);
    assertEquals(entity.getId(), actualDoc.getId());
  }

  @Test
  public void testGetEntityWithRevision() throws Exception {
    ProjectADomainEntity entity = new ProjectADomainEntity(DEFAULT_ID);
    int revision = 2;
    when(repository.getRevisionWithRelations(DEFAULT_TYPE, DEFAULT_ID, revision)).thenReturn(entity);

    ClientResponse response = createResource(DEFAULT_RESOURCE, DEFAULT_ID) //
        .queryParam(REVISION_KEY, "2") //
        .get(ClientResponse.class);
    verifyResponseStatus(response, Status.OK);

    ProjectADomainEntity actualDoc = response.getEntity(DEFAULT_TYPE);

    assertNotNull(actualDoc);
    assertEquals(entity.getId(), actualDoc.getId());
    verify(repository).getRevisionWithRelations(DEFAULT_TYPE, DEFAULT_ID, revision);
  }

  @Test
  public void testGetEntityNonExistingInstance() {
    when(repository.getEntityOrDefaultVariation(DEFAULT_TYPE, "TST0000000001")).thenReturn(null);

    ClientResponse response = createResource(DEFAULT_RESOURCE, "TST0000000001") //
        .get(ClientResponse.class);
    verifyResponseStatus(response, Status.NOT_FOUND);
  }

  @Test
  public void testGetEntityNonExistingType() {
    ClientResponse response = createResource("unknown", "TST0000000001") //
        .get(ClientResponse.class);
    verifyResponseStatus(response, Status.NOT_FOUND);
  }

  @Test
  public void testGetEntities() {
    List<ProjectADomainEntity> expectedList = Lists.newArrayList(new ProjectADomainEntity("TEST001"), new ProjectADomainEntity("TEST002"));
    StorageIterator<ProjectADomainEntity> iterator = StorageIteratorStub.newInstance(expectedList);
    when(repository.getDomainEntities(DEFAULT_TYPE)).thenReturn(iterator);

    GenericType<List<ProjectADomainEntity>> genericType = new GenericType<List<ProjectADomainEntity>>() {
    };

    ClientResponse response = createResource(DEFAULT_RESOURCE) //
        .get(ClientResponse.class);
    verifyResponseStatus(response, Status.OK);

    List<ProjectADomainEntity> actualList = response.getEntity(genericType);
    assertEquals(expectedList.size(), actualList.size());
  }

  // --- PUT -------------------------------------------------------------------

  @Test
  public void testPutAsUser() throws Exception {
    testPut(USER_ROLE);
  }

  @Test
  public void testPutAsAdmin() throws Exception {
    testPut(ADMIN_ROLE);
  }

  private void testPut(String userRole) throws Exception {
    setupUserWithRoles(VRE_ID, USER_ID, userRole);

    VRE vre = mock(VRE.class);
    when(vre.inScope(DEFAULT_TYPE, DEFAULT_ID)).thenReturn(true);
    makeVREAvailable(vre, VRE_ID);

    ProjectADomainEntity entity = new ProjectADomainEntity(DEFAULT_ID);
    entity.setPid("65262031-c5c2-44f9-b90e-11f9fc7736cf");

    when(repository.getEntityOrDefaultVariation(DEFAULT_TYPE, DEFAULT_ID)).thenReturn(entity);
    whenJsonProviderReadFromThenReturn(entity);

    ClientResponse response = createResource(DEFAULT_RESOURCE, DEFAULT_ID) //
        .type(MediaType.APPLICATION_JSON_TYPE) //
        .header(AUTHORIZATION, CREDENTIALS) //
        .header(VRE_ID_KEY, VRE_ID).put(ClientResponse.class, entity);
    verifyResponseStatus(response, Status.NO_CONTENT);

    verify(getChangeHelper()).notifyChange(ActionType.MOD, DEFAULT_TYPE, entity, DEFAULT_ID);
  }

  @Test
  public void testPutItemNotInScope() throws Exception {
    setupUserWithRoles(VRE_ID, USER_ID, USER_ROLE);

    VRE vre = mock(VRE.class);
    when(vre.inScope(DEFAULT_TYPE, DEFAULT_ID)).thenReturn(false);
    makeVREAvailable(vre, VRE_ID);

    ProjectADomainEntity entity = new ProjectADomainEntity(DEFAULT_ID);
    entity.setPid("65262031-c5c2-44f9-b90e-11f9fc7736cf");

    when(repository.getEntityOrDefaultVariation(DEFAULT_TYPE, DEFAULT_ID)).thenReturn(entity);
    whenJsonProviderReadFromThenReturn(entity);

    ClientResponse response = createResource(DEFAULT_RESOURCE, DEFAULT_ID) //
        .type(MediaType.APPLICATION_JSON_TYPE) //
        .header(AUTHORIZATION, CREDENTIALS) //
        .header(VRE_ID_KEY, VRE_ID).put(ClientResponse.class, entity);
    verifyResponseStatus(response, Status.FORBIDDEN);

    verifyZeroInteractions(getChangeHelper());
  }

  @Test
  public void testPutExistingDocumentWithoutPID() throws Exception {
    setupUserWithRoles(VRE_ID, USER_ID, USER_ROLE);

    VRE vre = mock(VRE.class);
    when(vre.inScope(DEFAULT_TYPE, DEFAULT_ID)).thenReturn(true);
    makeVREAvailable(vre, VRE_ID);

    ProjectADomainEntity entity = new ProjectADomainEntity(DEFAULT_ID);
    when(repository.getEntityOrDefaultVariation(DEFAULT_TYPE, DEFAULT_ID)).thenReturn(entity);
    whenJsonProviderReadFromThenReturn(entity);

    ClientResponse response = createResource(DEFAULT_RESOURCE, DEFAULT_ID) //
        .type(MediaType.APPLICATION_JSON_TYPE) //
        .header(AUTHORIZATION, CREDENTIALS) //
        .header(VRE_ID_KEY, VRE_ID).put(ClientResponse.class, entity);
    verifyResponseStatus(response, Status.FORBIDDEN);

    verifyZeroInteractions(getChangeHelper());
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testPutInvalidDocument() throws Exception {
    setupUserWithRoles(VRE_ID, USER_ID, USER_ROLE);

    VRE vre = mock(VRE.class);
    when(vre.inScope(DEFAULT_TYPE, DEFAULT_ID)).thenReturn(true);
    makeVREAvailable(vre, VRE_ID);

    ProjectADomainEntity entity = new ProjectADomainEntity(DEFAULT_ID);
    whenJsonProviderReadFromThenReturn(entity);

    Validator validator = injector.getInstance(Validator.class);
    // Mockito could not mock the ConstraintViolation, it entered an infinite loop.
    ConstraintViolation<ProjectADomainEntity> violation = new ConstraintViolation<ProjectADomainEntity>() {

      @Override
      public String getMessage() {
        return null;
      }

      @Override
      public String getMessageTemplate() {
        return null;
      }

      @Override
      public ProjectADomainEntity getRootBean() {
        return null;
      }

      @Override
      public Class<ProjectADomainEntity> getRootBeanClass() {
        return null;
      }

      @Override
      public Object getLeafBean() {
        return null;
      }

      @Override
      public Path getPropertyPath() {
        return null;
      }

      @Override
      public Object getInvalidValue() {
        return null;
      }

      @Override
      public ConstraintDescriptor<?> getConstraintDescriptor() {
        return null;
      }
    };

    when(validator.validate(entity)).thenReturn(Sets.<ConstraintViolation<ProjectADomainEntity>>newHashSet(violation));

    ClientResponse response = createResource(DEFAULT_RESOURCE, DEFAULT_ID) //
        .type(MediaType.APPLICATION_JSON_TYPE) //
        .header(AUTHORIZATION, CREDENTIALS) //
        .header(VRE_ID_KEY, VRE_ID).put(ClientResponse.class, entity);
    verifyResponseStatus(response, Status.BAD_REQUEST);

    verifyZeroInteractions(getChangeHelper());
  }

  @Test
  public void testPutWhenRepositoryThrowsAnUpdateException() throws Exception {
    setupUserWithRoles(VRE_ID, USER_ID, USER_ROLE);

    VRE vre = mock(VRE.class);
    when(vre.inScope(DEFAULT_TYPE, DEFAULT_ID)).thenReturn(true);
    makeVREAvailable(vre, VRE_ID);

    ProjectADomainEntity entity = new ProjectADomainEntity(DEFAULT_ID);
    entity.setPid("65262031-c5c2-44f9-b90e-11f9fc7736cf");

    when(repository.getEntityOrDefaultVariation(DEFAULT_TYPE, DEFAULT_ID)).thenReturn(entity);
    doThrow(UpdateException.class).when(repository).updateDomainEntity(Matchers.<Class<ProjectADomainEntity>>any(), any(ProjectADomainEntity.class), any(Change.class));
    whenJsonProviderReadFromThenReturn(entity);

    ClientResponse response = createResource(DEFAULT_RESOURCE, DEFAULT_ID) //
        .type(MediaType.APPLICATION_JSON_TYPE) //
        .header(AUTHORIZATION, CREDENTIALS) //
        .header(VRE_ID_KEY, VRE_ID).put(ClientResponse.class, entity);
    verifyResponseStatus(response, Status.CONFLICT);

    verifyZeroInteractions(getChangeHelper());
  }

  @Test
  public void testPutWhenRepositoryThrowsAnNoSuchEnityException() throws Exception {
    setupUserWithRoles(VRE_ID, USER_ID, USER_ROLE);

    VRE vre = mock(VRE.class);
    when(vre.inScope(DEFAULT_TYPE, DEFAULT_ID)).thenReturn(true);
    makeVREAvailable(vre, VRE_ID);

    ProjectADomainEntity entity = new ProjectADomainEntity(DEFAULT_ID);
    entity.setPid("65262031-c5c2-44f9-b90e-11f9fc7736cf");

    when(repository.getEntityOrDefaultVariation(DEFAULT_TYPE, DEFAULT_ID)).thenReturn(entity);
    doThrow(NoSuchEntityException.class).when(repository).updateDomainEntity(Matchers.<Class<ProjectADomainEntity>>any(), any(ProjectADomainEntity.class), any(Change.class));
    whenJsonProviderReadFromThenReturn(entity);

    ClientResponse response = createResource(DEFAULT_RESOURCE, DEFAULT_ID) //
        .type(MediaType.APPLICATION_JSON_TYPE) //
        .header(AUTHORIZATION, CREDENTIALS) //
        .header(VRE_ID_KEY, VRE_ID).put(ClientResponse.class, entity);
    verifyResponseStatus(response, Status.NOT_FOUND);

    verifyZeroInteractions(getChangeHelper());
  }

  @Test
  public void testPutNonExistingDocument() throws Exception {
    setupUserWithRoles(VRE_ID, USER_ID, USER_ROLE);

    VRE vre = mock(VRE.class);
    when(vre.inScope(DEFAULT_TYPE, VRE_ID)).thenReturn(true);
    makeVREAvailable(vre, VRE_ID);

    String id = "NULL000000000001";
    ProjectADomainEntity entity = new ProjectADomainEntity(id);
    entity.setPid("65262031-c5c2-44f9-b90e-11f9fc7736cf");
    whenJsonProviderReadFromThenReturn(entity);

    ClientResponse response = createResource(DEFAULT_RESOURCE, id) //
        .type(MediaType.APPLICATION_JSON_TYPE) //
        .header(AUTHORIZATION, CREDENTIALS) //
        .header(VRE_ID_KEY, VRE_ID).put(ClientResponse.class, entity);
    verifyResponseStatus(response, Status.NOT_FOUND);

    verifyZeroInteractions(getChangeHelper());
  }

  @Test
  public void testPutNonExistingType() throws Exception {
    setupUserWithRoles(VRE_ID, USER_ID, USER_ROLE);

    VRE vre = mock(VRE.class);
    when(vre.inScope(DEFAULT_TYPE, VRE_ID)).thenReturn(true);
    makeVREAvailable(vre, VRE_ID);

    ProjectADomainEntity entity = new ProjectADomainEntity(DEFAULT_ID);

    ClientResponse response = createResource("unknown", DEFAULT_ID) //
        .type(MediaType.APPLICATION_JSON_TYPE) //
        .header(AUTHORIZATION, CREDENTIALS) //
        .header(VRE_ID_KEY, VRE_ID).put(ClientResponse.class, entity);
    verifyResponseStatus(response, Status.NOT_FOUND);

    verifyZeroInteractions(getChangeHelper());
  }

  @Test
  public void testPutWrongType() throws Exception {
    setupUserWithRoles(VRE_ID, USER_ID, USER_ROLE);

    VRE vre = mock(VRE.class);
    when(vre.inScope(OtherDomainEntity.class, DEFAULT_ID)).thenReturn(true);
    makeVREAvailable(vre, VRE_ID);

    ProjectADomainEntity entity = new ProjectADomainEntity(DEFAULT_ID);

    ClientResponse response = createResource("otherdomainentities", DEFAULT_ID) //
        .type(MediaType.APPLICATION_JSON_TYPE) //
        .header(AUTHORIZATION, CREDENTIALS) //
        .header(VRE_ID_KEY, VRE_ID).put(ClientResponse.class, entity);
    verifyResponseStatus(response, Status.BAD_REQUEST);

    verifyZeroInteractions(getChangeHelper());
  }

  @Test
  public void testPutOnSuperClass() throws Exception {
    setupUserWithRoles(VRE_ID, USER_ID, USER_ROLE);

    VRE vre = mock(VRE.class);
    when(vre.inScope(OtherDomainEntity.class, DEFAULT_ID)).thenReturn(true);
    makeVREAvailable(vre, VRE_ID);

    BaseDomainEntity entity = new BaseDomainEntity(DEFAULT_ID);

    ClientResponse response = createResource("otherdomainentities", DEFAULT_ID) //
        .type(MediaType.APPLICATION_JSON_TYPE) //
        .header(AUTHORIZATION, CREDENTIALS) //
        .header(VRE_ID_KEY, VRE_ID).put(ClientResponse.class, entity);
    verifyResponseStatus(response, Status.BAD_REQUEST);

    verifyZeroInteractions(getChangeHelper());
  }

  @Test
  public void testPutOnCollection() throws Exception {
    BaseDomainEntity entity = new BaseDomainEntity(DEFAULT_ID);

    ClientResponse response = createResource("otherdomainentities") //
        .type(MediaType.APPLICATION_JSON_TYPE).header(AUTHORIZATION, CREDENTIALS).header(VRE_ID_KEY, VRE_ID).put(ClientResponse.class, entity);
    verifyResponseStatus(response, Status.METHOD_NOT_ALLOWED);

    verifyZeroInteractions(getChangeHelper());
  }

  @Test
  public void testPutRelation() throws Exception {
    Class<ProjectARelation> type = ProjectARelation.class;
    String id = "RELA000000000001";
    String sourceType = TypeNames.getInternalName(BASE_TYPE);
    String sourceId = "TEST000000000001";
    String targetType = TypeNames.getInternalName(BASE_TYPE);
    String targetId = "TEST000000000002";

    setupUserWithRoles(VRE_ID, USER_ID, USER_ROLE);

    VRE vre = mock(VRE.class);
    when(vre.inScope(type, id)).thenReturn(true);
    makeVREAvailable(vre, VRE_ID);

    ProjectARelation entity = RelationBuilder.newInstance(type) //
        .withId(id) //
        .withSourceType(sourceType) //
        .withSourceId(sourceId) //
        .withTargetType(targetType) //
        .withTargetId(targetId) //
        .withPid("65262031-c5c2-44f9-b90e-11f9fc7736cf") //
        .build();
    when(repository.getEntityOrDefaultVariation(type, id)).thenReturn(entity);
    whenJsonProviderReadFromThenReturn(entity);

    ClientResponse response = createResource(getExternalName(type), id) //
        .type(MediaType.APPLICATION_JSON_TYPE) //
        .header(AUTHORIZATION, CREDENTIALS) //
        .header(VRE_ID_KEY, VRE_ID).put(ClientResponse.class, entity);
    verifyResponseStatus(response, Status.NO_CONTENT);

    // handling of the relation entity itself
    verify(getChangeHelper()).notifyChange(ActionType.MOD, type, entity, id);
  }

  // --- POST ------------------------------------------------------------------

  @Test
  public void testPostAsUser() throws Exception {
    testPost(USER_ROLE);
  }

  @Test
  public void testPostAsAdmin() throws Exception {
    testPost(ADMIN_ROLE);
  }

  private void testPost(String userRole) throws Exception {
    setupUserWithRoles(VRE_ID, USER_ID, userRole);

    VRE vre = mock(VRE.class);
    when(vre.inScope(DEFAULT_TYPE)).thenReturn(true);
    makeVREAvailable(vre, VRE_ID);

    ProjectADomainEntity entity = new ProjectADomainEntity(DEFAULT_ID, "test");
    when(repository.addDomainEntity(Matchers.<Class<ProjectADomainEntity>>any(), any(DEFAULT_TYPE), any(Change.class))).thenReturn(DEFAULT_ID);
    whenJsonProviderReadFromThenReturn(entity);

    ClientResponse response = createResource(DEFAULT_RESOURCE) //
        .type(MediaType.APPLICATION_JSON_TYPE) //
        .header(AUTHORIZATION, CREDENTIALS) //
        .header(VRE_ID_KEY, VRE_ID).post(ClientResponse.class, entity);
    verifyResponseStatus(response, Status.CREATED);

    String location = response.getHeaders().getFirst("Location");
    assertThat(location, containsString(Paths.DOMAIN_PREFIX + "/projectadomainentities/" + DEFAULT_ID));
    verify(getChangeHelper()).notifyChange(ActionType.ADD, DEFAULT_TYPE, entity, DEFAULT_ID);
  }

  @Test
  public void testPostCollectionNotInScope() throws Exception {
    setupUserWithRoles(VRE_ID, USER_ID, USER_ROLE);

    VRE vre = mock(VRE.class);
    when(vre.inScope(DEFAULT_TYPE)).thenReturn(false);
    makeVREAvailable(vre, VRE_ID);

    ProjectADomainEntity entity = new ProjectADomainEntity(DEFAULT_ID, "test");
    when(repository.addDomainEntity(Matchers.<Class<ProjectADomainEntity>>any(), any(DEFAULT_TYPE), any(Change.class))).thenReturn(DEFAULT_ID);
    whenJsonProviderReadFromThenReturn(entity);

    ClientResponse response = createResource(DEFAULT_RESOURCE) //
        .type(MediaType.APPLICATION_JSON_TYPE) //
        .header(AUTHORIZATION, CREDENTIALS) //
        .header(VRE_ID_KEY, VRE_ID).post(ClientResponse.class, entity);
    verifyResponseStatus(response, Status.FORBIDDEN);

    verifyZeroInteractions(getChangeHelper());
  }

  //Request handled by the framework.
  @Test
  public void testPostWrongType() throws Exception {
    setupUserWithRoles(VRE_ID, USER_ID, USER_ROLE);

    setVREExist(VRE_ID, true);

    ProjectADomainEntity entity = new ProjectADomainEntity(DEFAULT_ID, "test");
    whenJsonProviderReadFromThenReturn(entity);

    ClientResponse response = createResource("otherdomainentities") //
        .type(MediaType.APPLICATION_JSON_TYPE) //
        .header(AUTHORIZATION, CREDENTIALS) //
        .header(VRE_ID_KEY, VRE_ID).post(ClientResponse.class, entity);
    verifyResponseStatus(response, Status.BAD_REQUEST);

    verifyZeroInteractions(getChangeHelper());
  }

  @Test
  public void testPostSpecificDocument() throws Exception {
    ProjectADomainEntity entity = new ProjectADomainEntity(DEFAULT_ID, "test");

    ClientResponse response = createResource("otherentitys", DEFAULT_ID) //
        .type(MediaType.APPLICATION_JSON_TYPE) //
        .header(AUTHORIZATION, CREDENTIALS) //
        .header(VRE_ID_KEY, VRE_ID).post(ClientResponse.class, entity);
    verifyResponseStatus(response, Status.METHOD_NOT_ALLOWED);

    verifyZeroInteractions(getChangeHelper());
  }

  @Test
  public void testPostRelation() throws Exception {
    Class<ProjectARelation> type = ProjectARelation.class;
    String id = "RELA000000000001";
    String sourceType = TypeNames.getInternalName(BASE_TYPE);
    String sourceId = "TEST000000000001";
    String targetType = TypeNames.getInternalName(BASE_TYPE);
    String targetId = "TEST000000000002";

    setupUserWithRoles(VRE_ID, USER_ID, USER_ROLE);

    VRE vre = mock(VRE.class);
    when(vre.inScope(type)).thenReturn(true);
    makeVREAvailable(vre, VRE_ID);

    ProjectARelation entity = RelationBuilder.newInstance(type) //
        .withId(id) //
        .withSourceType(sourceType) //
        .withSourceId(sourceId) //
        .withTargetType(targetType) //
        .withTargetId(targetId) //
        .build();
    when(repository.addDomainEntity(Matchers.<Class<ProjectARelation>>any(), any(type), any(Change.class))).thenReturn(id);
    whenJsonProviderReadFromThenReturn(entity);

    ClientResponse response = createResource(getExternalName(type)) //
        .type(MediaType.APPLICATION_JSON_TYPE) //
        .header(AUTHORIZATION, CREDENTIALS) //
        .header(VRE_ID_KEY, VRE_ID) //
        .post(ClientResponse.class, entity);
    verifyResponseStatus(response, Status.CREATED);

    String path = Paths.DOMAIN_PREFIX + "/" + getExternalName(type) + "/" + id;
    assertThat(response.getHeaders().getFirst("Location"), containsString(path));
    verify(getChangeHelper()).notifyChange(ActionType.ADD, type, entity, id);

  }

  // ---------------------------------------------------------------------------

  @Test
  public void testDeleteAsUser() throws Exception {
    testDelete(USER_ROLE);
  }

  @Test
  public void testDeleteAsAdmin() throws Exception {
    testDelete(ADMIN_ROLE);
  }

  private void testDelete(String userRole) throws Exception {
    setupUserWithRoles(VRE_ID, USER_ID, userRole);

    VRE vre = mock(VRE.class);
    when(vre.inScope(BASE_TYPE, DEFAULT_ID)).thenReturn(true);
    makeVREAvailable(vre, VRE_ID);

    BaseDomainEntity entity = new BaseDomainEntity(DEFAULT_ID);
    entity.setPid("65262031-c5c2-44f9-b90e-11f9fc7736cf");
    when(repository.getEntityOrDefaultVariation(BASE_TYPE, DEFAULT_ID)).thenReturn(entity);

    ClientResponse response = createResource(BASE_RESOURCE, DEFAULT_ID) //
        .type(MediaType.APPLICATION_JSON_TYPE) //
        .header(AUTHORIZATION, CREDENTIALS) //
        .header(VRE_ID_KEY, VRE_ID).delete(ClientResponse.class);
    verifyResponseStatus(response, Status.NO_CONTENT);

    verify(getChangeHelper()).notifyChange(ActionType.DEL, BASE_TYPE, entity, DEFAULT_ID);
  }

  @Test
  public void testDeleteProjectSpecificType() throws Exception {
    setupUserWithRoles(VRE_ID, USER_ID, USER_ROLE);

    VRE vre = mock(VRE.class);
    when(vre.inScope(DEFAULT_TYPE, DEFAULT_ID)).thenReturn(true);
    makeVREAvailable(vre, VRE_ID);

    ClientResponse response = createResource(DEFAULT_RESOURCE, DEFAULT_ID) //
        .type(MediaType.APPLICATION_JSON_TYPE) //
        .header(AUTHORIZATION, CREDENTIALS) //
        .header(VRE_ID_KEY, VRE_ID).delete(ClientResponse.class);
    verifyResponseStatus(response, Status.BAD_REQUEST);

    verifyZeroInteractions(getChangeHelper());
    verify(repository, times(0)).getEntityOrDefaultVariation(DEFAULT_TYPE, DEFAULT_ID);
  }

  @Test
  public void testDeleteDocumentWithoutPID() throws Exception {
    setupUserWithRoles(VRE_ID, USER_ID, USER_ROLE);

    BaseDomainEntity entity = new BaseDomainEntity(DEFAULT_ID);
    when(repository.getEntityOrDefaultVariation(BASE_TYPE, DEFAULT_ID)).thenReturn(entity);

    ClientResponse response = createResource(BASE_RESOURCE, DEFAULT_ID) //
        .type(MediaType.APPLICATION_JSON_TYPE) //
        .header(AUTHORIZATION, CREDENTIALS) //
        .header(VRE_ID_KEY, VRE_ID).delete(ClientResponse.class);
    verifyResponseStatus(response, Status.FORBIDDEN);

    verifyZeroInteractions(getChangeHelper());
  }

  @Test
  public void testDeleteEntityThatDoesNotExist() throws Exception {
    setupUserWithRoles(VRE_ID, USER_ID, USER_ROLE);

    setVREExist(VRE_ID, true);
    when(repository.getEntityOrDefaultVariation(BASE_TYPE, DEFAULT_ID)).thenReturn(null);

    ClientResponse response = createResource(BASE_RESOURCE, DEFAULT_ID) //
        .type(MediaType.APPLICATION_JSON_TYPE) //
        .header(AUTHORIZATION, CREDENTIALS) //
        .header(VRE_ID_KEY, VRE_ID).delete(ClientResponse.class);
    verifyResponseStatus(response, Status.NOT_FOUND);

    verifyZeroInteractions(getChangeHelper());
  }

  @Test
  public void testDeleteTypeDoesNotExist() throws Exception {
    setupUserWithRoles(VRE_ID, USER_ID, USER_ROLE);

    setVREExist(VRE_ID, true);

    ClientResponse response = createResource("unknownEntities", DEFAULT_ID) //
        .type(MediaType.APPLICATION_JSON_TYPE) //
        .header(AUTHORIZATION, CREDENTIALS) //
        .header(VRE_ID_KEY, VRE_ID).delete(ClientResponse.class);
    verifyResponseStatus(response, Status.NOT_FOUND);

    verifyZeroInteractions(getChangeHelper());
  }

  @Test
  public void testDeleteCollection() throws Exception {
    ClientResponse response = createResource(BASE_RESOURCE) //
        .type(MediaType.APPLICATION_JSON_TYPE) //
        .header(AUTHORIZATION, CREDENTIALS) //
        .header(VRE_ID_KEY, VRE_ID).delete(ClientResponse.class);
    verifyResponseStatus(response, Status.METHOD_NOT_ALLOWED);

    verifyZeroInteractions(getChangeHelper(), repository);
  }

  // Security tests

  @Test
  public void testGetEntityNotLoggedIn() throws Exception {
    ProjectADomainEntity expectedDoc = new ProjectADomainEntity(DEFAULT_ID);
    when(repository.getEntityOrDefaultVariationWithRelations(DEFAULT_TYPE, DEFAULT_ID)).thenReturn(expectedDoc);

    ClientResponse response = createResource(DEFAULT_RESOURCE, DEFAULT_ID) //
        .get(ClientResponse.class);
    verifyResponseStatus(response, Status.OK);
  }

  @Test
  public void testGetEntityEmptyAuthorizationKey() throws Exception {
    ProjectADomainEntity entity = new ProjectADomainEntity(DEFAULT_ID);
    when(repository.getEntityOrDefaultVariationWithRelations(DEFAULT_TYPE, DEFAULT_ID)).thenReturn(entity);

    ClientResponse response = createResource(DEFAULT_RESOURCE, DEFAULT_ID) //
        .get(ClientResponse.class);
    verifyResponseStatus(response, Status.OK);
  }

  @Test
  public void testPutUserNotInRole() throws Exception {
    setupUserWithRoles(VRE_ID, USER_ID);

    ProjectADomainEntity entity = new ProjectADomainEntity(DEFAULT_ID);
    whenJsonProviderReadFromThenReturn(entity);

    ClientResponse response = createResource(DEFAULT_RESOURCE, DEFAULT_ID) //
        .type(MediaType.APPLICATION_JSON_TYPE) //
        .header(AUTHORIZATION, CREDENTIALS) //
        .header(VRE_ID_KEY, VRE_ID).put(ClientResponse.class, entity);
    verifyResponseStatus(response, Status.FORBIDDEN);

    verifyZeroInteractions(getChangeHelper());
  }

  @Test
  public void testPutUserNotLoggedIn() throws Exception {
    ProjectADomainEntity entity = new ProjectADomainEntity(DEFAULT_ID);
    whenJsonProviderReadFromThenReturn(entity);

    setUserNotLoggedIn();

    ClientResponse response = createResource(DEFAULT_RESOURCE, DEFAULT_ID) //
        .type(MediaType.APPLICATION_JSON_TYPE) //
        .header(VRE_ID_KEY, VRE_ID) //
        .put(ClientResponse.class, entity);
    verifyResponseStatus(response, Status.UNAUTHORIZED);

    verifyZeroInteractions(getChangeHelper());
  }

  @Test
  public void testPostUserNotInRole() throws Exception {
    setupUserWithRoles(VRE_ID, USER_ID);

    ProjectADomainEntity entity = new ProjectADomainEntity(DEFAULT_ID, "test");
    whenJsonProviderReadFromThenReturn(entity);

    ClientResponse response = createResource(DEFAULT_RESOURCE) //
        .type(MediaType.APPLICATION_JSON_TYPE) //
        .header(AUTHORIZATION, CREDENTIALS).header(VRE_ID_KEY, VRE_ID) //
        .post(ClientResponse.class, entity);
    verifyResponseStatus(response, Status.FORBIDDEN);

    verifyZeroInteractions(getChangeHelper());
  }

  @Test
  public void testPostUserNotLoggedIn() throws Exception {
    ProjectADomainEntity entity = new ProjectADomainEntity(DEFAULT_ID, "test");
    whenJsonProviderReadFromThenReturn(entity);

    setUserNotLoggedIn();

    ClientResponse response = createResource(DEFAULT_RESOURCE) //
        .type(MediaType.APPLICATION_JSON_TYPE) //
        .header(VRE_ID_KEY, VRE_ID) //
        .post(ClientResponse.class, entity);
    verifyResponseStatus(response, Status.UNAUTHORIZED);

    verifyZeroInteractions(getChangeHelper());
  }

  @Test
  public void testDeleteNotLoggedIn() throws Exception {
    setUserNotLoggedIn();

    ClientResponse response = createResource(BASE_RESOURCE, DEFAULT_ID) //
        .type(MediaType.APPLICATION_JSON_TYPE) //
        .header(VRE_ID_KEY, VRE_ID) //
        .delete(ClientResponse.class);
    verifyResponseStatus(response, Status.UNAUTHORIZED);

    verifyZeroInteractions(getChangeHelper());
  }

  @Test
  public void testDeleteUserNotInRole() throws Exception {
    setupUserWithRoles(VRE_ID, USER_ID);

    ClientResponse response = createResource(BASE_RESOURCE, DEFAULT_ID) //
        .type(MediaType.APPLICATION_JSON_TYPE) //
        .header(AUTHORIZATION, CREDENTIALS).header(VRE_ID_KEY, VRE_ID) //
        .delete(ClientResponse.class);
    verifyResponseStatus(response, Status.FORBIDDEN);

    verifyZeroInteractions(getChangeHelper());
  }

  // Test put PID.

  @Test
  public void testPutPID() throws Exception {
    setupUserWithRoles(VRE_ID, USER_ID, ADMIN_ROLE);

    VRE vre = mock(VRE.class);
    when(vre.inScope(BASE_TYPE)).thenReturn(true);
    makeVREAvailable(vre, VRE_ID);

    PersistenceRequestFactory persistenceRequestFactory = injector.getInstance(PersistenceRequestFactory.class);
    when(persistenceRequestFactory.forEntity(any(ActionType.class), any(), anyString())).thenReturn(mock(PersistenceRequest.class));

    String id1 = "ID1";
    String id2 = "ID2";
    String id3 = "ID3";

    when(repository.getAllIdsWithoutPID(DEFAULT_TYPE)).thenReturn(Lists.newArrayList(id1, id2, id3));

    ClientResponse response = doPutPIDRequest(DEFAULT_RESOURCE);
    verifyResponseStatus(response, Status.NO_CONTENT);

    // verify
    verify(persistenceRequestFactory).forEntity(ActionType.ADD, DEFAULT_TYPE, id1);
    verify(persistenceRequestFactory).forEntity(ActionType.ADD, DEFAULT_TYPE, id2);
    verify(persistenceRequestFactory).forEntity(ActionType.ADD, DEFAULT_TYPE, id3);

    verify(getChangeHelper(), times(3)).sendPersistMessage(any(PersistenceRequest.class));
  }

  @Test
  public void testPutPIDOnBaseEntity() throws Exception {
    setupUserWithRoles(VRE_ID, USER_ID, ADMIN_ROLE);

    VRE vre = mock(VRE.class);
    when(vre.inScope(BASE_TYPE)).thenReturn(true);
    makeVREAvailable(vre, VRE_ID);

    ClientResponse response = doPutPIDRequest(BASE_RESOURCE);
    verifyResponseStatus(response, Status.BAD_REQUEST);

    verify(repository, never()).getAllIdsWithoutPID(Mockito.<Class<? extends DomainEntity>>any());
    verifyZeroInteractions(getChangeHelper());
  }

  @Test
  public void testPutPIDBaseClassNotInScope() throws Exception {
    setupUserWithRoles(VRE_ID, USER_ID, ADMIN_ROLE);

    VRE vre = mock(VRE.class);
    when(vre.inScope(BASE_TYPE)).thenReturn(false);
    makeVREAvailable(vre, VRE_ID);

    ClientResponse response = doPutPIDRequest(DEFAULT_RESOURCE);
    verifyResponseStatus(response, Status.FORBIDDEN);

    verify(repository, never()).getAllIdsWithoutPID(Mockito.<Class<? extends DomainEntity>>any());
    verifyZeroInteractions(getChangeHelper());
  }

  @Test
  public void testPutPIDTypeDoesNotExist() throws Exception {
    setupUserWithRoles(VRE_ID, USER_ID, ADMIN_ROLE);

    setVREExist(VRE_ID, true);

    ClientResponse response = doPutPIDRequest(UNKNOWN_TYPE_RESOURCE);
    verifyResponseStatus(response, Status.NOT_FOUND);

    verify(repository, never()).getAllIdsWithoutPID(Mockito.<Class<? extends DomainEntity>>any());
    verifyZeroInteractions(getChangeHelper());
  }

  @Test
  public void testPutPIDUserNotAllowed() throws Exception {
    setupUserWithRoles(VRE_ID, USER_ID, USER_ROLE);

    setVREExist(VRE_ID, true);

    ClientResponse response = doPutPIDRequest(UNKNOWN_TYPE_RESOURCE);
    verifyResponseStatus(response, Status.FORBIDDEN);

    verify(repository, never()).getAllIdsWithoutPID(Mockito.<Class<? extends DomainEntity>>any());
    verifyZeroInteractions(getChangeHelper());
  }

  private ClientResponse doPutPIDRequest(String collectionName) {
    return createResource(collectionName, PID_PATH) //
        .header(AUTHORIZATION, CREDENTIALS) //
        .header(VRE_ID_KEY, VRE_ID) //
        .put(ClientResponse.class);
  }


  // update pids

  @Test
  public void updatePIDsUpdatesThePIDOfACertainType() {
    // setup
    setupUserWithRoles(VRE_ID, USER_ID, ADMIN_ROLE);

    PersistenceRequest persistenceRequest = mock(PersistenceRequest.class);
    PersistenceRequestFactory persistenceRequestFactory = injector.getInstance(PersistenceRequestFactory.class);
    when(persistenceRequestFactory.forCollection(any(), any())).thenReturn(persistenceRequest);

    VRE vre = mock(VRE.class);
    when(vre.inScope(BASE_TYPE)).thenReturn(true);
    makeVREAvailable(vre, VRE_ID);

    // action
    ClientResponse response = doUpdatePIDRequest(DEFAULT_RESOURCE);


    // verify
    verifyResponseStatus(response, Status.NO_CONTENT);
    verify(persistenceRequestFactory).forCollection(ActionType.MOD, DEFAULT_TYPE);
    verify(getChangeHelper()).sendPersistMessage(persistenceRequest);
  }



  @Test
  public void updatePIDsReturnsNotFoundIfTheTypeIsUnknown() {
    // setup
    setupUserWithRoles(VRE_ID, USER_ID, ADMIN_ROLE);

    setVREExist(VRE_ID, true);

    // action
    ClientResponse response = doUpdatePIDRequest(UNKNOWN_TYPE_RESOURCE);

    // verify
    verifyResponseStatus(response, Status.NOT_FOUND);
    verifyZeroInteractions(getChangeHelper());
  }

  @Test
  public void updatePIDsReturnsForbiddenWhenTheUserIsNotAnAdmin() {
    // setup
    setupUserWithRoles(VRE_ID, USER_ID, USER_ROLE);

    VRE vre = mock(VRE.class);
    makeVREAvailable(vre, VRE_ID);

    // action
    ClientResponse response = doUpdatePIDRequest(UNKNOWN_TYPE_RESOURCE);


    // verify
    verifyResponseStatus(response, Status.FORBIDDEN);
    verifyZeroInteractions(getChangeHelper());
  }

  @Test
  public void updatePIDsReturnsForbiddenWhenTheVREIsNotAllowedToEditTheBaseType() {
    // setup
    setupUserWithRoles(VRE_ID, USER_ID, ADMIN_ROLE);

    VRE vre = mock(VRE.class);
    when(vre.inScope(BASE_TYPE)).thenReturn(false);
    makeVREAvailable(vre, VRE_ID);

    // action
    ClientResponse response = doUpdatePIDRequest(DEFAULT_RESOURCE);


    // verify
    verifyResponseStatus(response, Status.FORBIDDEN);
    verifyZeroInteractions(getChangeHelper());
  }

  private ClientResponse doUpdatePIDRequest(String collection) {
    return  createResource(collection, UPDATE_PID_PATH) //
        .header(AUTHORIZATION, CREDENTIALS) //
        .header(VRE_ID_KEY, VRE_ID) //
        .put(ClientResponse.class);
  }

}
